package ru.lifegame.backend.domain.npc;

import ru.lifegame.backend.domain.npc.spec.NpcSpec;
import ru.lifegame.backend.domain.npc.spec.NpcSpec.ScheduleSlot;
import ru.lifegame.backend.domain.npc.spec.NpcSpec.ActionSpec;
import ru.lifegame.backend.domain.npc.spec.NpcSpec.ConditionSpec;
import ru.lifegame.backend.domain.npc.spec.NpcSpec.OptionSpec;
import ru.lifegame.backend.domain.npc.spec.NpcSpec.ReactionSpec;
import ru.lifegame.backend.domain.npc.spec.NpcSpec.EffectSpec;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.*;

/**
 * Loads NPC specifications from XML files in narrative/npc-behavior/.
 * Parses the real XML format:
 *   <npc-behavior entity-id="alexander">
 *     <schedules><schedule time="morning">...</schedule></schedules>
 *     <reactions><reaction trigger="...">...</reaction></reactions>
 *     <personality><trait name="..." value="..."/></personality>
 *   </npc-behavior>
 */
public class NpcSpecLoader {

    private static final String NPC_SPEC_PATTERN = "classpath:narrative/npc-behavior/*.xml";

    private static final Set<String> PET_IDS = Set.of(
            "sam", "garfield", "cirilla", "persi", "klop", "lada"
    );
    private static final Set<String> NAMED_IDS = Set.of(
            "alexander", "aijan", "thelma", "duke", "voland"
    );

    public List<NpcSpec> loadAll() {
        List<NpcSpec> specs = new ArrayList<>();
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(NPC_SPEC_PATTERN);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            for (Resource resource : resources) {
                try (InputStream is = resource.getInputStream()) {
                    Document doc = builder.parse(is);
                    doc.getDocumentElement().normalize();
                    specs.add(parseNpc(doc.getDocumentElement()));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load NPC specs from XML", e);
        }
        return specs;
    }

    private NpcSpec parseNpc(Element root) {
        String id = attrOr(root, "entity-id", attrOr(root, "id", "unknown"));

        String category = attrOr(root, "category", inferCategory(id));
        String type = attrOr(root, "type", inferType(id));
        String displayName = getTextContent(root, "display-name", capitalize(id));

        Map<String, Integer> personality = parsePersonality(root);
        Map<String, Integer> moodInitial = parseMoodInitial(root);

        boolean memoryEnabled = true;
        int shortTermSize = 10;
        NodeList memNodes = root.getElementsByTagName("memory");
        if (memNodes.getLength() > 0) {
            Element memEl = (Element) memNodes.item(0);
            memoryEnabled = Boolean.parseBoolean(attrOr(memEl, "enabled", "true"));
            String sts = memEl.getAttribute("short-term-size");
            if (!sts.isEmpty()) shortTermSize = Integer.parseInt(sts);
        }

        List<ScheduleSlot> scheduleSlots = parseSchedules(root);
        List<ActionSpec> actions = parseActions(root);
        List<ReactionSpec> reactions = parseReactions(root);
        List<String> questLines = parseQuestLines(root);

        return new NpcSpec(
                id, type, category, displayName,
                Map.copyOf(personality), Map.copyOf(moodInitial),
                memoryEnabled, shortTermSize,
                List.copyOf(scheduleSlots),
                List.copyOf(actions),
                List.copyOf(reactions),
                List.copyOf(questLines)
        );
    }

    // ── Schedule parsing ──────────────────────────────────────────

    private List<ScheduleSlot> parseSchedules(Element root) {
        List<ScheduleSlot> slots = new ArrayList<>();
        NodeList scheduleNodes = root.getElementsByTagName("schedule");
        for (int i = 0; i < scheduleNodes.getLength(); i++) {
            Element schedEl = (Element) scheduleNodes.item(i);
            if (!schedEl.getParentNode().getNodeName().equals("schedules")) continue;

            String time = attrOr(schedEl, "time", "day");
            int[] hours = timeToHours(time);

            String location = "";
            String activity = time;
            String animation = "";

            // Extract location from first <action type="move">
            // Extract animation from first <action type="idle_animation">
            NodeList actionNodes = schedEl.getElementsByTagName("action");
            for (int j = 0; j < actionNodes.getLength(); j++) {
                Element actEl = (Element) actionNodes.item(j);
                String actType = actEl.getAttribute("type");
                if ("move".equals(actType) && location.isEmpty()) {
                    location = attrOr(actEl, "target", "");
                }
                if ("idle_animation".equals(actType) && animation.isEmpty()) {
                    animation = attrOr(actEl, "value", "");
                }
            }

            if (animation.isEmpty()) animation = activity;

            slots.add(new ScheduleSlot(hours[0], hours[1], activity, location, animation));
        }
        return slots;
    }

    private int[] timeToHours(String time) {
        return switch (time.toLowerCase()) {
            case "morning" -> new int[]{6, 12};
            case "day" -> new int[]{12, 18};
            case "evening" -> new int[]{18, 22};
            case "night" -> new int[]{22, 6};
            default -> new int[]{0, 24};
        };
    }

    // ── Action parsing ───────────────────────────────────────────

    /**
     * Parses NPC-initiated actions. In the real XML format, actions
     * live inside <schedule> blocks as <action type="dialogue-chance">.
     * We extract them as ActionSpec entries with dialogue text.
     */
    private List<ActionSpec> parseActions(Element root) {
        List<ActionSpec> actions = new ArrayList<>();
        int counter = 0;

        NodeList scheduleNodes = root.getElementsByTagName("schedule");
        for (int i = 0; i < scheduleNodes.getLength(); i++) {
            Element schedEl = (Element) scheduleNodes.item(i);
            if (!schedEl.getParentNode().getNodeName().equals("schedules")) continue;

            String time = attrOr(schedEl, "time", "day");
            NodeList actionNodes = schedEl.getElementsByTagName("action");

            for (int j = 0; j < actionNodes.getLength(); j++) {
                Element actEl = (Element) actionNodes.item(j);
                String actType = actEl.getAttribute("type");

                if ("dialogue-chance".equals(actType)) {
                    counter++;
                    String actionId = time + "_dialogue_" + counter;
                    double probability = parseDoubleAttr(actEl, "probability", 0.5);
                    String dialogue = getDirectChildText(actEl, "line-ru");

                    // Create an ActionSpec: probability as baseScore,
                    // dialogue stored in a single OptionSpec
                    List<OptionSpec> options = new ArrayList<>();
                    if (!dialogue.isEmpty()) {
                        options.add(new OptionSpec(
                                actionId + "_opt", dialogue, "",
                                0, 0, 0, 0, "", 0
                        ));
                    }

                    actions.add(new ActionSpec(
                            actionId, probability, "NPC_INITIATED",
                            List.of(), options
                    ));
                }
            }
        }
        return actions;
    }

    // ── Reaction parsing ─────────────────────────────────────────

    private List<ReactionSpec> parseReactions(Element root) {
        List<ReactionSpec> reactions = new ArrayList<>();
        NodeList nodes = root.getElementsByTagName("reaction");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (!el.getParentNode().getNodeName().equals("reactions")) continue;

            String trigger = attrOr(el, "trigger", "");
            String patternType = attrOr(el, "phase", "");
            int radius = parseIntAttr(el, "radius", 0);

            // Collect dialogue from child <action type="dialogue-chance"><line-ru>...
            String dialogue = "";
            NodeList childActions = el.getElementsByTagName("action");
            for (int j = 0; j < childActions.getLength(); j++) {
                Element childAct = (Element) childActions.item(j);
                if ("dialogue-chance".equals(childAct.getAttribute("type"))) {
                    String line = getDirectChildText(childAct, "line-ru");
                    if (!line.isEmpty()) {
                        dialogue = line;
                        break;
                    }
                }
            }

            // Collect animation effects
            List<EffectSpec> effects = new ArrayList<>();
            for (int j = 0; j < childActions.getLength(); j++) {
                Element childAct = (Element) childActions.item(j);
                if ("animation".equals(childAct.getAttribute("type"))) {
                    effects.add(new EffectSpec(
                            "self", "animation",
                            0 // animation doesn't have a delta
                    ));
                }
            }

            reactions.add(new ReactionSpec(
                    trigger, patternType, radius, dialogue, effects
            ));
        }
        return reactions;
    }

    // ── Personality parsing ───────────────────────────────────────

    private Map<String, Integer> parsePersonality(Element root) {
        Map<String, Integer> personality = new LinkedHashMap<>();
        NodeList traits = root.getElementsByTagName("trait");
        for (int i = 0; i < traits.getLength(); i++) {
            Element trait = (Element) traits.item(i);
            if (trait.getParentNode().getNodeName().equals("personality")) {
                personality.put(trait.getAttribute("name"),
                        Integer.parseInt(trait.getAttribute("value")));
            }
        }
        return personality;
    }

    private Map<String, Integer> parseMoodInitial(Element root) {
        Map<String, Integer> moodInitial = new LinkedHashMap<>();
        NodeList moodNodes = root.getElementsByTagName("mood-initial");
        if (moodNodes.getLength() > 0) {
            Element moodEl = (Element) moodNodes.item(0);
            NamedNodeMap attrs = moodEl.getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                Attr attr = (Attr) attrs.item(i);
                moodInitial.put(attr.getName(), Integer.parseInt(attr.getValue()));
            }
        }
        // Default 6-axis mood if none specified
        if (moodInitial.isEmpty()) {
            moodInitial.put("happiness", 50);
            moodInitial.put("anxiety", 30);
            moodInitial.put("loneliness", 30);
            moodInitial.put("irritability", 20);
            moodInitial.put("energy", 60);
            moodInitial.put("affection", 50);
        }
        return moodInitial;
    }

    // ── Quest lines ──────────────────────────────────────────────

    private List<String> parseQuestLines(Element root) {
        NodeList qlNodes = root.getElementsByTagName("quest-lines");
        if (qlNodes.getLength() > 0) {
            String raw = qlNodes.item(0).getTextContent().trim();
            if (!raw.isEmpty()) {
                return Arrays.stream(raw.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
            }
        }
        return List.of();
    }

    // ── Inference helpers ─────────────────────────────────────────

    private String inferType(String id) {
        if (NAMED_IDS.contains(id.toLowerCase())) return "named";
        if (PET_IDS.contains(id.toLowerCase())) return "named"; // pets are named too
        return "filler";
    }

    private String inferCategory(String id) {
        if (PET_IDS.contains(id.toLowerCase())) return "pet";
        return "human";
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    // ── XML utility helpers ──────────────────────────────────────

    private String getDirectChildText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n.getParentNode() == parent) {
                return n.getTextContent().trim();
            }
        }
        return "";
    }

    private String getTextContent(Element root, String tagName, String defaultValue) {
        NodeList nodes = root.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return defaultValue;
    }

    private String attrOr(Element el, String attr, String defaultValue) {
        String val = el.getAttribute(attr);
        return (val != null && !val.isEmpty()) ? val : defaultValue;
    }

    private int parseIntAttr(Element el, String attr, int defaultValue) {
        String val = el.getAttribute(attr);
        if (val == null || val.isEmpty()) return defaultValue;
        try { return Integer.parseInt(val); }
        catch (NumberFormatException e) { return defaultValue; }
    }

    private double parseDoubleAttr(Element el, String attr, double defaultValue) {
        String val = el.getAttribute(attr);
        if (val == null || val.isEmpty()) return defaultValue;
        try { return Double.parseDouble(val); }
        catch (NumberFormatException e) { return defaultValue; }
    }
}
