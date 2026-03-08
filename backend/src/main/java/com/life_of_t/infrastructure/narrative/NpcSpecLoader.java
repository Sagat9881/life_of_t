package com.life_of_t.infrastructure.narrative;

import com.life_of_t.domain.npc.spec.ConditionSpec;
import com.life_of_t.domain.npc.spec.NpcSpec;
import com.life_of_t.domain.npc.spec.NpcSpec.*;
import com.life_of_t.domain.npc.spec.ScoredAction;
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

    /**
     * Scan and parse all NPC XML specs from narrative directory.
     */
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
        String category = root.getAttributeOrDefault("category", "human");

        String displayName = getTextContent(root, "display-name", id);

        // Personality traits
        Map<String, Integer> personality = new LinkedHashMap<>();
        NodeList traits = root.getElementsByTagName("trait");
        for (int i = 0; i < traits.getLength(); i++) {
            Element trait = (Element) traits.item(i);
            if (trait.getParentNode().getNodeName().equals("personality")) {
                personality.put(trait.getAttribute("name"),
                        Integer.parseInt(trait.getAttribute("value")));
            }
        }

        // Mood initial values
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

        // Memory config
        boolean memoryEnabled = true;
        int shortTermSize = 10;
        NodeList memNodes = root.getElementsByTagName("memory");
        if (memNodes.getLength() > 0) {
            Element memEl = (Element) memNodes.item(0);
            memoryEnabled = Boolean.parseBoolean(memEl.getAttribute("enabled"));
            String sts = memEl.getAttribute("short-term-size");
            if (!sts.isEmpty()) shortTermSize = Integer.parseInt(sts);
        }

        // Schedule slots
        List<ScheduleSlotSpec> scheduleSlots = new ArrayList<>();
        NodeList slots = root.getElementsByTagName("slot");
        for (int i = 0; i < slots.getLength(); i++) {
            Element slot = (Element) slots.item(i);
            if (slot.getParentNode().getNodeName().equals("schedule")) {
                scheduleSlots.add(new ScheduleSlotSpec(
                        Integer.parseInt(slot.getAttribute("start")),
                        Integer.parseInt(slot.getAttribute("end")),
                        slot.getAttribute("activity"),
                        slot.getAttribute("location"),
                        slot.getAttributeOrDefault("animation", slot.getAttribute("activity"))
                ));
            }
        }

        // Actions (scored for Utility AI)
        List<ScoredAction> actions = new ArrayList<>();
        NodeList actionNodes = root.getElementsByTagName("action");
        for (int i = 0; i < actionNodes.getLength(); i++) {
            Element actionEl = (Element) actionNodes.item(i);
            if (!actionEl.getParentNode().getNodeName().equals("actions")) continue;

            String actionId = actionEl.getAttribute("id");
            double baseScore = Double.parseDouble(
                    actionEl.getAttributeOrDefault("base-score", "0.5"));
            String eventType = actionEl.getAttributeOrDefault("event-type", "NPC_INITIATED");

            // Conditions
            List<ConditionSpec> conditions = parseConditions(actionEl);

            // Options
            List<ScoredAction.ActionOption> options = new ArrayList<>();
            NodeList optionNodes = actionEl.getElementsByTagName("option");
            for (int j = 0; j < optionNodes.getLength(); j++) {
                Element optEl = (Element) optionNodes.item(j);
                Map<String, Integer> statChanges = new LinkedHashMap<>();
                for (String stat : List.of("energy", "stress", "mood", "money")) {
                    String val = optEl.getAttribute(stat);
                    if (!val.isEmpty()) statChanges.put(stat, Integer.parseInt(val));
                }
                String relTarget = optEl.getAttribute("relationship-target");
                int relDelta = 0;
                String relDeltaStr = optEl.getAttribute("relationship-delta");
                if (!relDeltaStr.isEmpty()) relDelta = Integer.parseInt(relDeltaStr);

                options.add(new ScoredAction.ActionOption(
                        optEl.getAttribute("id"),
                        optEl.getAttribute("text"),
                        optEl.getAttribute("result"),
                        statChanges,
                        relTarget, relDelta
                ));
            }

            actions.add(new ScoredAction(actionId, baseScore, eventType, conditions, options));
        }

        // Mood override actions
        List<MoodOverrideAction> moodOverrides = new ArrayList<>();
        NodeList overrideNodes = root.getElementsByTagName("mood-override");
        for (int i = 0; i < overrideNodes.getLength(); i++) {
            Element ov = (Element) overrideNodes.item(i);
            moodOverrides.add(new MoodOverrideAction(
                    ov.getAttribute("trigger-axis"),
                    ov.getAttribute("activity"),
                    ov.getAttribute("location"),
                    ov.getAttributeOrDefault("animation", ov.getAttribute("activity")),
                    Integer.parseInt(ov.getAttributeOrDefault("duration", "2"))
            ));
        }

        // Quest lines (optional)
        List<String> questLines = new ArrayList<>();
        NodeList qlNodes = root.getElementsByTagName("quest-lines");
        if (qlNodes.getLength() > 0) {
            String raw = qlNodes.item(0).getTextContent().trim();
            if (!raw.isEmpty()) {
                questLines = Arrays.stream(raw.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
            }
        }

        // Interaction action ID (for memory tracking)
        String interactionActionId = getTextContent(root, "interaction-action", "");

        return new NpcSpec(
                id, type, category, displayName,
                Map.copyOf(personality), Map.copyOf(moodInitial),
                memoryEnabled, shortTermSize,
                List.copyOf(scheduleSlots),
                List.copyOf(actions),
                List.copyOf(moodOverrides),
                List.copyOf(questLines),
                interactionActionId
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
                    condEl.getAttributeOrDefault("axis", condEl.getAttributeOrDefault("target", "")),
                    condEl.getAttributeOrDefault("operator", "gte"),
                    condEl.getAttributeOrDefault("value", "0"),
                    condEl.getAttributeOrDefault("check", ""),
                    condEl.getAttributeOrDefault("npc", ""),
                    condEl.getAttributeOrDefault("action", ""),
                    condEl.getAttributeOrDefault("min-count", "0")
            ));
        }
        return conditions;
    }

    private String getTextContent(Element root, String tagName, String defaultValue) {
        NodeList nodes = root.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return defaultValue;
    }
}
