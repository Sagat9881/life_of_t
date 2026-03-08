package com.sagat9881.lifeoft.domain.npc.engine;

import com.sagat9881.lifeoft.domain.npc.spec.*;
import com.sagat9881.lifeoft.domain.npc.model.NpcSchedule;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.*;

/**
 * Parses NPC XML specifications from narrative/npc-behavior/ directory.
 * Produces NpcSpec objects that are purely data — no game logic.
 * Supports both named (full brain) and filler (light) NPC types.
 */
public class NpcSpecLoader {

    /**
     * Parse a single NPC XML specification from an InputStream.
     */
    public NpcSpec parse(InputStream xmlStream) {
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(xmlStream);
            doc.getDocumentElement().normalize();
            return parseNpc(doc.getDocumentElement());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse NPC spec XML", e);
        }
    }

    private NpcSpec parseNpc(Element root) {
        String id = root.getAttribute("id");
        String type = root.getAttribute("type"); // "named" or "filler"
        String category = root.getAttribute("category"); // "human", "animal"

        String displayName = getTextContent(root, "display-name", id);

        Map<String, Double> personality = parsePersonality(root);
        Map<String, Double> moodInitial = parseMoodInitial(root);
        boolean memoryEnabled = parseMemoryEnabled(root);
        int shortTermSize = parseShortTermSize(root);
        List<NpcSchedule.ScheduleSlot> scheduleSlots = parseSchedule(root);
        List<ScoredAction> actions = parseActions(root);

        return new NpcSpec(
                id, type, category, displayName,
                personality, moodInitial,
                memoryEnabled, shortTermSize,
                scheduleSlots, actions
        );
    }

    private Map<String, Double> parsePersonality(Element root) {
        Map<String, Double> traits = new LinkedHashMap<>();
        NodeList nodes = root.getElementsByTagName("personality");
        if (nodes.getLength() > 0) {
            Element personality = (Element) nodes.item(0);
            NodeList traitNodes = personality.getElementsByTagName("trait");
            for (int i = 0; i < traitNodes.getLength(); i++) {
                Element trait = (Element) traitNodes.item(i);
                traits.put(trait.getAttribute("name"),
                        Double.parseDouble(trait.getAttribute("value")));
            }
        }
        return traits;
    }

    private Map<String, Double> parseMoodInitial(Element root) {
        Map<String, Double> mood = new LinkedHashMap<>();
        NodeList nodes = root.getElementsByTagName("mood-initial");
        if (nodes.getLength() > 0) {
            Element el = (Element) nodes.item(0);
            for (String axis : List.of("happiness", "anxiety", "loneliness",
                    "irritability", "energy", "affection")) {
                if (el.hasAttribute(axis)) {
                    mood.put(axis, Double.parseDouble(el.getAttribute(axis)));
                }
            }
        }
        return mood;
    }

    private boolean parseMemoryEnabled(Element root) {
        NodeList nodes = root.getElementsByTagName("memory");
        if (nodes.getLength() > 0) {
            return "true".equals(((Element) nodes.item(0)).getAttribute("enabled"));
        }
        return false;
    }

    private int parseShortTermSize(Element root) {
        NodeList nodes = root.getElementsByTagName("memory");
        if (nodes.getLength() > 0) {
            String size = ((Element) nodes.item(0)).getAttribute("short-term-size");
            return size.isEmpty() ? 10 : Integer.parseInt(size);
        }
        return 10;
    }

    private List<NpcSchedule.ScheduleSlot> parseSchedule(Element root) {
        List<NpcSchedule.ScheduleSlot> slots = new ArrayList<>();
        NodeList scheduleNodes = root.getElementsByTagName("schedule");
        if (scheduleNodes.getLength() > 0) {
            Element schedule = (Element) scheduleNodes.item(0);
            NodeList slotNodes = schedule.getElementsByTagName("slot");
            for (int i = 0; i < slotNodes.getLength(); i++) {
                Element s = (Element) slotNodes.item(i);
                slots.add(new NpcSchedule.ScheduleSlot(
                        Integer.parseInt(s.getAttribute("start")),
                        Integer.parseInt(s.getAttribute("end")),
                        s.getAttribute("activity"),
                        s.getAttribute("location"),
                        s.getAttribute("animation")
                ));
            }
        }
        return slots;
    }

    private List<ScoredAction> parseActions(Element root) {
        List<ScoredAction> actions = new ArrayList<>();
        NodeList actionNodes = root.getElementsByTagName("actions");
        if (actionNodes.getLength() == 0) return actions;

        Element actionsEl = (Element) actionNodes.item(0);
        NodeList actionList = actionsEl.getElementsByTagName("action");
        for (int i = 0; i < actionList.getLength(); i++) {
            Element a = (Element) actionList.item(i);
            String actionId = a.getAttribute("id");
            double baseScore = parseDouble(a.getAttribute("base-score"), 0.5);
            String eventType = a.getAttribute("event-type");
            String description = getTextContent(a, "description", null);
            String animation = a.getAttribute("animation");
            String location = a.getAttribute("location");

            List<String> tags = parseTags(a);
            List<ConditionSpec> conditions = parseConditions(a);
            List<ScoredAction.ActionOption> options = parseOptions(a);

            actions.add(new ScoredAction(
                    actionId, baseScore, eventType, description,
                    animation, location, tags, conditions, options
            ));
        }
        return actions;
    }

    private List<ConditionSpec> parseConditions(Element actionEl) {
        List<ConditionSpec> conditions = new ArrayList<>();
        NodeList condNodes = actionEl.getElementsByTagName("conditions");
        if (condNodes.getLength() == 0) return conditions;

        Element condsEl = (Element) condNodes.item(0);
        NodeList condList = condsEl.getElementsByTagName("condition");
        for (int i = 0; i < condList.getLength(); i++) {
            Element c = (Element) condList.item(i);
            conditions.add(new ConditionSpec(
                    c.getAttribute("type"),
                    c.hasAttribute("target") ? c.getAttribute("target") : c.getAttribute("axis"),
                    c.hasAttribute("operator") ? c.getAttribute("operator") : c.getAttribute("check"),
                    c.hasAttribute("value") ? Double.parseDouble(c.getAttribute("value")) : null
            ));
        }
        return conditions;
    }

    private List<ScoredAction.ActionOption> parseOptions(Element actionEl) {
        List<ScoredAction.ActionOption> options = new ArrayList<>();
        NodeList optNodes = actionEl.getElementsByTagName("options");
        if (optNodes.getLength() == 0) return options;

        Element optsEl = (Element) optNodes.item(0);
        NodeList optList = optsEl.getElementsByTagName("option");
        for (int i = 0; i < optList.getLength(); i++) {
            Element o = (Element) optList.item(i);
            options.add(new ScoredAction.ActionOption(
                    o.getAttribute("id"),
                    o.getAttribute("text"),
                    o.getAttribute("result"),
                    parseIntAttr(o, "energy", 0),
                    parseIntAttr(o, "stress", 0),
                    parseIntAttr(o, "mood", 0),
                    parseIntAttr(o, "money", 0),
                    o.hasAttribute("relationship-target") ? o.getAttribute("relationship-target") : null,
                    parseIntAttr(o, "relationship-delta", 0)
            ));
        }
        return options;
    }

    private List<String> parseTags(Element actionEl) {
        NodeList tagNodes = actionEl.getElementsByTagName("tags");
        if (tagNodes.getLength() == 0) return List.of();
        String tagsStr = tagNodes.item(0).getTextContent().trim();
        if (tagsStr.isEmpty()) return List.of();
        return List.of(tagsStr.split(","));
    }

    private String getTextContent(Element parent, String tagName, String defaultVal) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return defaultVal;
    }

    private double parseDouble(String val, double defaultVal) {
        try { return Double.parseDouble(val); } catch (Exception e) { return defaultVal; }
    }

    private int parseIntAttr(Element el, String attr, int defaultVal) {
        if (!el.hasAttribute(attr)) return defaultVal;
        try { return Integer.parseInt(el.getAttribute(attr)); } catch (Exception e) { return defaultVal; }
    }
}
