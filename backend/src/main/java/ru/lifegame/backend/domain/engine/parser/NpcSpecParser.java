package ru.lifegame.backend.domain.engine.parser;

import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.engine.spec.NpcSpec.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.util.*;

public class NpcSpecParser {

    public NpcSpec parse(File xmlFile) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
        Element root = doc.getDocumentElement();

        String id = root.getAttribute("id");
        String type = root.getAttribute("type");
        String category = root.getAttribute("category");
        String displayName = getTextContent(root, "display-name");

        Map<String, Integer> personality = parseTraits(root, "personality");
        Map<String, Integer> moodInitial = parseMoodInitial(root);
        boolean memoryEnabled = "true".equals(getAttributeFromChild(root, "memory", "enabled"));
        int shortTermSize = parseIntAttribute(root, "memory", "short-term-size", 10);

        List<ScheduleSlot> schedule = parseSchedule(root);
        List<ActionSpec> actions = parseActions(root);
        List<ReactionSpec> reactions = parseReactions(root);
        List<String> questLines = parseQuestLines(root);

        return new NpcSpec(id, type, category, displayName, personality, moodInitial,
            memoryEnabled, shortTermSize, schedule, actions, reactions, questLines);
    }

    private List<ScheduleSlot> parseSchedule(Element root) {
        List<ScheduleSlot> slots = new ArrayList<>();
        NodeList nodes = root.getElementsByTagName("slot");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            Element parent = (Element) el.getParentNode();
            if ("schedule".equals(parent.getTagName())) {
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
            Element parent = (Element) el.getParentNode();
            if ("actions".equals(parent.getTagName())) {
                String actionId = el.getAttribute("id");
                double baseScore = Double.parseDouble(el.getAttribute("base-score"));
                String eventType = el.getAttribute("event-type");
                List<ConditionSpec> conditions = parseConditions(el);
                List<OptionSpec> options = parseOptions(el);
                actions.add(new ActionSpec(actionId, baseScore, eventType, conditions, options));
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
                el.getAttribute("pattern-type"),
                parseIntAttr(el, "threshold", 0),
                getTextContent(el, "dialogue"),
                parseEffects(el)
            ));
        }
        return reactions;
    }

    private List<ConditionSpec> parseConditions(Element parent) {
        List<ConditionSpec> conditions = new ArrayList<>();
        NodeList nodes = parent.getElementsByTagName("condition");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            conditions.add(new ConditionSpec(
                el.getAttribute("type"),
                el.getAttribute("target"),
                el.getAttribute("operator"),
                el.getAttribute("value")
            ));
        }
        return conditions;
    }

    private List<OptionSpec> parseOptions(Element parent) {
        List<OptionSpec> options = new ArrayList<>();
        NodeList nodes = parent.getElementsByTagName("option");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            options.add(new OptionSpec(
                el.getAttribute("id"),
                el.getAttribute("text"),
                el.getAttribute("result"),
                parseIntAttr(el, "energy", 0),
                parseIntAttr(el, "stress", 0),
                parseIntAttr(el, "mood", 0),
                parseIntAttr(el, "money", 0),
                el.getAttribute("relationship-target"),
                parseIntAttr(el, "relationship-delta", 0)
            ));
        }
        return options;
    }

    private List<EffectSpec> parseEffects(Element parent) {
        List<EffectSpec> effects = new ArrayList<>();
        NodeList nodes = parent.getElementsByTagName("effect");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            effects.add(new EffectSpec(el.getAttribute("target"), el.getAttribute("stat"), parseIntAttr(el, "delta", 0)));
        }
        return effects;
    }

    private List<String> parseQuestLines(Element root) {
        String text = getTextContent(root, "quest-lines");
        if (text == null || text.isBlank()) return List.of();
        return Arrays.stream(text.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private Map<String, Integer> parseTraits(Element root, String tagName) {
        Map<String, Integer> traits = new HashMap<>();
        NodeList nodes = root.getElementsByTagName("trait");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (tagName.equals(((Element) el.getParentNode()).getTagName())) {
                traits.put(el.getAttribute("name"), Integer.parseInt(el.getAttribute("value")));
            }
        }
        return traits;
    }

    private Map<String, Integer> parseMoodInitial(Element root) {
        Map<String, Integer> mood = new HashMap<>();
        NodeList nodes = root.getElementsByTagName("mood-initial");
        if (nodes.getLength() > 0) {
            Element el = (Element) nodes.item(0);
            NamedNodeMap attrs = el.getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                Attr attr = (Attr) attrs.item(i);
                mood.put(attr.getName(), Integer.parseInt(attr.getValue()));
            }
        }
        return mood;
    }

    private String getTextContent(Element root, String tagName) {
        NodeList nodes = root.getElementsByTagName(tagName);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent().trim() : "";
    }

    private String getAttributeFromChild(Element root, String childTag, String attr) {
        NodeList nodes = root.getElementsByTagName(childTag);
        return nodes.getLength() > 0 ? ((Element) nodes.item(0)).getAttribute(attr) : "";
    }

    private int parseIntAttribute(Element root, String childTag, String attr, int defaultVal) {
        String val = getAttributeFromChild(root, childTag, attr);
        return val.isEmpty() ? defaultVal : Integer.parseInt(val);
    }

    private int parseIntAttr(Element el, String attr, int defaultVal) {
        String val = el.getAttribute(attr);
        return val.isEmpty() ? defaultVal : Integer.parseInt(val);
    }
}
