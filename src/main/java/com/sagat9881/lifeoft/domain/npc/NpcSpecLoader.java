package com.sagat9881.lifeoft.domain.npc;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.*;

/**
 * Parses NPC XML specifications from narrative/npc-behavior/ directory.
 * Converts XML into NpcSpec domain objects.
 * The backend knows nothing about specific NPCs —
 * all content is defined in XML.
 */
public class NpcSpecLoader {

    /**
     * Parse a single NPC XML file from an InputStream.
     */
    public NpcSpec parse(InputStream xmlStream) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlStream);
            Element root = doc.getDocumentElement();

            String id = root.getAttribute("id");
            String type = root.getAttribute("type"); // "named" or "filler"
            String category = root.getAttribute("category"); // "human", "animal"
            String displayName = getTextContent(root, "display-name");

            Map<String, Integer> personalityTraits = parsePersonality(root);
            NpcMoodInitial moodInitial = parseMoodInitial(root);
            boolean memoryEnabled = parseMemoryEnabled(root);
            int shortTermSize = parseShortTermSize(root);
            List<NpcScheduleSlot> scheduleSlots = parseSchedule(root);
            List<ScoredAction> actions = parseActions(root);

            return new NpcSpec(
                    id, type, category, displayName,
                    personalityTraits, moodInitial,
                    memoryEnabled, shortTermSize,
                    scheduleSlots, actions
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse NPC spec XML", e);
        }
    }

    /**
     * Parse multiple NPC XMLs from a list of InputStreams.
     */
    public List<NpcSpec> parseAll(List<InputStream> xmlStreams) {
        List<NpcSpec> specs = new ArrayList<>();
        for (InputStream stream : xmlStreams) {
            specs.add(parse(stream));
        }
        return specs;
    }

    private Map<String, Integer> parsePersonality(Element root) {
        Map<String, Integer> traits = new LinkedHashMap<>();
        NodeList personalityNodes = root.getElementsByTagName("personality");
        if (personalityNodes.getLength() > 0) {
            Element personality = (Element) personalityNodes.item(0);
            NodeList traitNodes = personality.getElementsByTagName("trait");
            for (int i = 0; i < traitNodes.getLength(); i++) {
                Element trait = (Element) traitNodes.item(i);
                traits.put(trait.getAttribute("name"),
                        Integer.parseInt(trait.getAttribute("value")));
            }
        }
        return traits;
    }

    private NpcMoodInitial parseMoodInitial(Element root) {
        NodeList moodNodes = root.getElementsByTagName("mood-initial");
        if (moodNodes.getLength() > 0) {
            Element mood = (Element) moodNodes.item(0);
            return new NpcMoodInitial(
                    intAttr(mood, "happiness", 50),
                    intAttr(mood, "anxiety", 20),
                    intAttr(mood, "loneliness", 20),
                    intAttr(mood, "irritability", 10),
                    intAttr(mood, "energy", 70),
                    intAttr(mood, "affection", 50)
            );
        }
        return new NpcMoodInitial(50, 20, 20, 10, 70, 50);
    }

    private boolean parseMemoryEnabled(Element root) {
        NodeList memNodes = root.getElementsByTagName("memory");
        if (memNodes.getLength() > 0) {
            return "true".equals(((Element) memNodes.item(0)).getAttribute("enabled"));
        }
        return false;
    }

    private int parseShortTermSize(Element root) {
        NodeList memNodes = root.getElementsByTagName("memory");
        if (memNodes.getLength() > 0) {
            return intAttr((Element) memNodes.item(0), "short-term-size", 10);
        }
        return 10;
    }

    private List<NpcScheduleSlot> parseSchedule(Element root) {
        List<NpcScheduleSlot> slots = new ArrayList<>();
        NodeList scheduleNodes = root.getElementsByTagName("schedule");
        if (scheduleNodes.getLength() > 0) {
            Element schedule = (Element) scheduleNodes.item(0);
            NodeList slotNodes = schedule.getElementsByTagName("slot");
            for (int i = 0; i < slotNodes.getLength(); i++) {
                Element slot = (Element) slotNodes.item(i);
                slots.add(new NpcScheduleSlot(
                        Integer.parseInt(slot.getAttribute("start")),
                        Integer.parseInt(slot.getAttribute("end")),
                        slot.getAttribute("activity"),
                        slot.getAttribute("location"),
                        slot.getAttribute("animation")
                ));
            }
        }
        return slots;
    }

    private List<ScoredAction> parseActions(Element root) {
        List<ScoredAction> actions = new ArrayList<>();
        NodeList actionsNodes = root.getElementsByTagName("actions");
        if (actionsNodes.getLength() > 0) {
            Element actionsEl = (Element) actionsNodes.item(0);
            NodeList actionNodes = actionsEl.getElementsByTagName("action");
            for (int i = 0; i < actionNodes.getLength(); i++) {
                Element action = (Element) actionNodes.item(i);
                String actionId = action.getAttribute("id");
                double baseScore = Double.parseDouble(action.getAttribute("base-score"));
                String eventType = action.getAttribute("event-type");

                List<ConditionSpec> conditions = parseConditions(action);
                List<ActionOption> options = parseOptions(action);

                actions.add(new ScoredAction(actionId, baseScore, eventType, conditions, options));
            }
        }
        return actions;
    }

    private List<ConditionSpec> parseConditions(Element parent) {
        List<ConditionSpec> conditions = new ArrayList<>();
        NodeList condNodes = parent.getElementsByTagName("conditions");
        if (condNodes.getLength() > 0) {
            Element condsEl = (Element) condNodes.item(0);
            NodeList condList = condsEl.getElementsByTagName("condition");
            for (int i = 0; i < condList.getLength(); i++) {
                Element cond = (Element) condList.item(i);
                conditions.add(new ConditionSpec(
                        cond.getAttribute("type"),
                        attrOrNull(cond, "axis"),
                        attrOrNull(cond, "check"),
                        attrOrNull(cond, "operator"),
                        attrOrNull(cond, "value"),
                        attrOrNull(cond, "pattern"),
                        attrOrNull(cond, "target")
                ));
            }
        }
        return conditions;
    }

    private List<ActionOption> parseOptions(Element parent) {
        List<ActionOption> options = new ArrayList<>();
        NodeList optNodes = parent.getElementsByTagName("options");
        if (optNodes.getLength() > 0) {
            Element optsEl = (Element) optNodes.item(0);
            NodeList optList = optsEl.getElementsByTagName("option");
            for (int i = 0; i < optList.getLength(); i++) {
                Element opt = (Element) optList.item(i);
                options.add(new ActionOption(
                        opt.getAttribute("id"),
                        opt.getAttribute("text"),
                        attrOrNull(opt, "result"),
                        intAttr(opt, "energy", 0),
                        intAttr(opt, "stress", 0),
                        intAttr(opt, "mood", 0),
                        intAttr(opt, "money", 0),
                        attrOrNull(opt, "relationship-target"),
                        intAttr(opt, "relationship-delta", 0)
                ));
            }
        }
        return options;
    }

    private String getTextContent(Element root, String tagName) {
        NodeList nodes = root.getElementsByTagName(tagName);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent().trim() : "";
    }

    private int intAttr(Element el, String name, int defaultVal) {
        String val = el.getAttribute(name);
        return (val == null || val.isEmpty()) ? defaultVal : Integer.parseInt(val);
    }

    private String attrOrNull(Element el, String name) {
        String val = el.getAttribute(name);
        return (val == null || val.isEmpty()) ? null : val;
    }

    /**
     * Initial mood values parsed from XML.
     */
    public record NpcMoodInitial(
            int happiness, int anxiety, int loneliness,
            int irritability, int energy, int affection
    ) {}

    /**
     * A schedule slot parsed from XML.
     */
    public record NpcScheduleSlot(
            int startHour, int endHour,
            String activity, String location, String animation
    ) {}

    /**
     * An option within an NPC-initiated action.
     */
    public record ActionOption(
            String id, String text, String resultText,
            int energy, int stress, int mood, int money,
            String relationshipTarget, int relationshipDelta
    ) {}
}
