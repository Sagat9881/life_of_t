package ru.lifegame.backend.domain.engine.parser;

import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.engine.spec.NpcSpec.*;
import ru.lifegame.backend.domain.engine.spec.ConditionSpec;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.InputStream;
import java.util.*;

public class NpcSpecParser {

    public NpcSpec parse(InputStream xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc = factory.newDocumentBuilder().parse(xml);
        Element root = doc.getDocumentElement();

        String id = root.getAttribute("id");
        String type = root.getAttribute("type");
        String category = root.getAttribute("category");
        String displayName = getTextContent(root, "display-name");

        Map<String, Integer> personality = parseTraits(root, "personality");
        Map<String, Integer> moodInitial = parseMoodInitial(root);
        boolean memoryEnabled = parseBooleanAttr(root, "memory", "enabled", false);
        int shortTermSize = parseIntAttr(root, "memory", "short-term-size", 10);

        List<ScheduleSlot> schedule = parseSchedule(root);
        List<ActionSpec> actions = parseActions(root);
        List<ReactionSpec> reactions = parseReactions(root);
        List<String> questLines = parseQuestLines(root);

        return new NpcSpec(id, type, category, displayName, personality, moodInitial, memoryEnabled, shortTermSize, schedule, actions, reactions, questLines);
    }

    private List<ScheduleSlot> parseSchedule(Element root) {
        List<ScheduleSlot> slots = new ArrayList<>();
        NodeList nodes = root.getElementsByTagName("slot");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (el.getParentNode().getNodeName().equals("schedule")) {
                slots.add(new ScheduleSlot(
                    Integer.parseInt(el.getAttribute("start")),
                    Integer.parseInt(el.getAttribute("end")),
                    el.getAttribute("activity"),
                    el.getAttribute("location"),
                    el.getAttribute("animation")
                ));
            }
        }
        return slots;
    }

    private List<ActionSpec> parseActions(Element root) {
        List<ActionSpec> actions = new ArrayList<>();
        NodeList nodes = root.getElementsByTagName("action");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (el.getParentNode().getNodeName().equals("actions")) {
                String aid = el.getAttribute("id");
                double baseScore = Double.parseDouble(el.getAttribute("base-score"));
                String eventType = el.hasAttribute("event-type") ? el.getAttribute("event-type") : "";
                List<ConditionSpec> conditions = parseConditions(el);
                List<NpcSpec.OptionSpec> options = parseOptions(el);
                actions.add(new ActionSpec(aid, baseScore, eventType, conditions, options));
            }
        }
        return actions;
    }

    private List<ReactionSpec> parseReactions(Element root) {
        List<ReactionSpec> reactions = new ArrayList<>();
        NodeList nodes = root.getElementsByTagName("reaction");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            reactions.add(new ReactionSpec(
                el.getAttribute("id"),
                el.getAttribute("trigger"),
                el.getAttribute("type"),
                parseConditions(el),
                getTextContent(el, "dialogue"),
                new HashMap<>()
            ));
        }
        return reactions;
    }

    private List<ConditionSpec> parseConditions(Element parent) {
        List<ConditionSpec> conditions = new ArrayList<>();
        NodeList nodes = parent.getElementsByTagName("condition");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (el.getParentNode().getNodeName().equals("conditions")) {
                conditions.add(new ConditionSpec(
                    el.getAttribute("type"),
                    el.hasAttribute("axis") ? el.getAttribute("axis") : el.getAttribute("target"),
                    el.getAttribute("operator"),
                    el.hasAttribute("value") ? el.getAttribute("value") : el.getAttribute("min")
                ));
            }
        }
        return conditions;
    }

    private List<NpcSpec.OptionSpec> parseOptions(Element parent) {
        List<NpcSpec.OptionSpec> options = new ArrayList<>();
        NodeList nodes = parent.getElementsByTagName("option");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            options.add(new NpcSpec.OptionSpec(
                el.getAttribute("id"),
                el.getAttribute("text"),
                el.getAttribute("result"),
                parseIntAttrDirect(el, "energy"),
                parseIntAttrDirect(el, "stress"),
                parseIntAttrDirect(el, "mood"),
                parseIntAttrDirect(el, "money"),
                el.getAttribute("relationship-target"),
                parseIntAttrDirect(el, "relationship-delta")
            ));
        }
        return options;
    }

    private Map<String, Integer> parseTraits(Element root, String parentTag) {
        Map<String, Integer> map = new HashMap<>();
        NodeList nodes = root.getElementsByTagName("trait");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (el.getParentNode().getNodeName().equals(parentTag)) {
                map.put(el.getAttribute("name"), Integer.parseInt(el.getAttribute("value")));
            }
        }
        return map;
    }

    private Map<String, Integer> parseMoodInitial(Element root) {
        Map<String, Integer> map = new HashMap<>();
        NodeList nodes = root.getElementsByTagName("mood-initial");
        if (nodes.getLength() > 0) {
            Element el = (Element) nodes.item(0);
            NamedNodeMap attrs = el.getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                Attr attr = (Attr) attrs.item(i);
                map.put(attr.getName(), Integer.parseInt(attr.getValue()));
            }
        }
        return map;
    }

    private List<String> parseQuestLines(Element root) {
        String text = getTextContent(root, "quest-lines");
        if (text == null || text.isBlank()) return List.of();
        return Arrays.stream(text.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private String getTextContent(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent().trim() : "";
    }

    private boolean parseBooleanAttr(Element root, String tag, String attr, boolean def) {
        NodeList nodes = root.getElementsByTagName(tag);
        if (nodes.getLength() > 0) return Boolean.parseBoolean(((Element)nodes.item(0)).getAttribute(attr));
        return def;
    }

    private int parseIntAttr(Element root, String tag, String attr, int def) {
        NodeList nodes = root.getElementsByTagName(tag);
        if (nodes.getLength() > 0) {
            String val = ((Element)nodes.item(0)).getAttribute(attr);
            if (!val.isEmpty()) return Integer.parseInt(val);
        }
        return def;
    }

    private int parseIntAttrDirect(Element el, String attr) {
        String val = el.getAttribute(attr);
        if (val == null || val.isEmpty()) return 0;
        return Integer.parseInt(val);
    }
}
