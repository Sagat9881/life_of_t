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
 * This is the only infrastructure class that reads NPC content —
 * the domain engine works entirely with NpcSpec abstractions.
 */
public class NpcSpecLoader {

    private static final String NPC_SPEC_PATTERN = "classpath:narrative/npc-behavior/*.xml";

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
        String id = root.getAttribute("id");
        String type = root.getAttribute("type");
        String category = getAttributeOrDefault(root, "category", "human");
        String displayName = getTextContent(root, "display-name", id);

        Map<String, Integer> personality = new LinkedHashMap<>();
        NodeList traits = root.getElementsByTagName("trait");
        for (int i = 0; i < traits.getLength(); i++) {
            Element trait = (Element) traits.item(i);
            if (trait.getParentNode().getNodeName().equals("personality")) {
                personality.put(trait.getAttribute("name"),
                        Integer.parseInt(trait.getAttribute("value")));
            }
        }

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

        boolean memoryEnabled = true;
        int shortTermSize = 10;
        NodeList memNodes = root.getElementsByTagName("memory");
        if (memNodes.getLength() > 0) {
            Element memEl = (Element) memNodes.item(0);
            memoryEnabled = Boolean.parseBoolean(memEl.getAttribute("enabled"));
            String sts = memEl.getAttribute("short-term-size");
            if (!sts.isEmpty()) shortTermSize = Integer.parseInt(sts);
        }

        List<ScheduleSlot> scheduleSlots = new ArrayList<>();
        NodeList slots = root.getElementsByTagName("slot");
        for (int i = 0; i < slots.getLength(); i++) {
            Element slot = (Element) slots.item(i);
            if (slot.getParentNode().getNodeName().equals("schedule")) {
                scheduleSlots.add(new ScheduleSlot(
                        Integer.parseInt(slot.getAttribute("start")),
                        Integer.parseInt(slot.getAttribute("end")),
                        slot.getAttribute("activity"),
                        slot.getAttribute("location"),
                        getAttributeOrDefault(slot, "animation", slot.getAttribute("activity"))
                ));
            }
        }

        List<ActionSpec> actions = new ArrayList<>();
        NodeList actionNodes = root.getElementsByTagName("action");
        for (int i = 0; i < actionNodes.getLength(); i++) {
            Element actionEl = (Element) actionNodes.item(i);
            if (!actionEl.getParentNode().getNodeName().equals("actions")) continue;

            String actionId = actionEl.getAttribute("id");
            double baseScore = Double.parseDouble(
                    getAttributeOrDefault(actionEl, "base-score", "0.5"));
            String eventType = getAttributeOrDefault(actionEl, "event-type", "NPC_INITIATED");

            List<ConditionSpec> conditions = parseConditions(actionEl);
            List<OptionSpec> options = parseOptions(actionEl);

            actions.add(new ActionSpec(actionId, baseScore, eventType, conditions, options));
        }

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

    private List<ConditionSpec> parseConditions(Element parent) {
        List<ConditionSpec> conditions = new ArrayList<>();
        NodeList condNodes = parent.getElementsByTagName("condition");
        for (int i = 0; i < condNodes.getLength(); i++) {
            Element condEl = (Element) condNodes.item(i);
            if (!condEl.getParentNode().getNodeName().equals("conditions")) continue;
            conditions.add(new ConditionSpec(
                    condEl.getAttribute("type"),
                    getAttributeOrDefault(condEl, "axis",
                            getAttributeOrDefault(condEl, "target", "")),
                    getAttributeOrDefault(condEl, "operator", "gte"),
                    getAttributeOrDefault(condEl, "value", "0")
            ));
        }
        return conditions;
    }

    private List<OptionSpec> parseOptions(Element parent) {
        List<OptionSpec> options = new ArrayList<>();
        NodeList optionNodes = parent.getElementsByTagName("option");
        for (int j = 0; j < optionNodes.getLength(); j++) {
            Element optEl = (Element) optionNodes.item(j);
            options.add(new OptionSpec(
                    optEl.getAttribute("id"),
                    optEl.getAttribute("text"),
                    optEl.getAttribute("result"),
                    parseIntAttr(optEl, "energy", 0),
                    parseIntAttr(optEl, "stress", 0),
                    parseIntAttr(optEl, "mood", 0),
                    parseIntAttr(optEl, "money", 0),
                    optEl.getAttribute("relationship-target"),
                    parseIntAttr(optEl, "relationship-delta", 0)
            ));
        }
        return options;
    }

    private List<ReactionSpec> parseReactions(Element root) {
        List<ReactionSpec> reactions = new ArrayList<>();
        NodeList nodes = root.getElementsByTagName("reaction");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            reactions.add(new ReactionSpec(
                    el.getAttribute("id"),
                    getAttributeOrDefault(el, "pattern-type", ""),
                    parseIntAttr(el, "threshold", 0),
                    getTextContent(el, "dialogue", ""),
                    parseEffects(el)
            ));
        }
        return reactions;
    }

    private List<EffectSpec> parseEffects(Element parent) {
        List<EffectSpec> effects = new ArrayList<>();
        NodeList nodes = parent.getElementsByTagName("effect");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            effects.add(new EffectSpec(
                    el.getAttribute("target"),
                    el.getAttribute("stat"),
                    parseIntAttr(el, "delta", 0)
            ));
        }
        return effects;
    }

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

    private String getTextContent(Element root, String tagName, String defaultValue) {
        NodeList nodes = root.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return defaultValue;
    }

    private String getAttributeOrDefault(Element el, String attr, String defaultValue) {
        String val = el.getAttribute(attr);
        return (val != null && !val.isEmpty()) ? val : defaultValue;
    }

    private int parseIntAttr(Element el, String attr, int defaultValue) {
        String val = el.getAttribute(attr);
        if (val == null || val.isEmpty()) return defaultValue;
        try { return Integer.parseInt(val); }
        catch (NumberFormatException e) { return defaultValue; }
    }
}
