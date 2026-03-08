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
        var doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml);
        var root = doc.getDocumentElement();
        String id = root.getAttribute("id");
        String type = root.getAttribute("type");
        String category = root.getAttribute("category");
        String displayName = getTextContent(root, "display-name");
        Map<String, Integer> personality = parseTraits(root, "personality");
        Map<String, Integer> moodInitial = parseMoodInitial(root);
        boolean memoryEnabled = Boolean.parseBoolean(getAttr(root, "memory", "enabled", "false"));
        int shortTermSize = Integer.parseInt(getAttr(root, "memory", "short-term-size", "10"));
        List<ScheduleSlot> schedule = parseSchedule(root);
        List<ActionSpec> actions = parseActions(root);
        List<ReactionSpec> reactions = parseReactions(root);
        List<String> questLines = parseQuestLines(root);
        return new NpcSpec(id, type, category, displayName, personality, moodInitial, memoryEnabled, shortTermSize, schedule, actions, reactions, questLines);
    }

    private List<ScheduleSlot> parseSchedule(Element root) {
        List<ScheduleSlot> slots = new ArrayList<>();
        var scheduleNodes = root.getElementsByTagName("slot");
        for (int i = 0; i < scheduleNodes.getLength(); i++) {
            var el = (Element) scheduleNodes.item(i);
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
        var actionNodes = root.getElementsByTagName("action");
        for (int i = 0; i < actionNodes.getLength(); i++) {
            var el = (Element) actionNodes.item(i);
            if (el.getParentNode().getNodeName().equals("actions")) {
                actions.add(new ActionSpec(
                    el.getAttribute("id"),
                    Double.parseDouble(el.getAttribute("base-score")),
                    el.getAttribute("event-type"),
                    parseConditions(el),
                    parseOptions(el)
                ));
            }
        }
        return actions;
    }

    private List<ReactionSpec> parseReactions(Element root) {
        List<ReactionSpec> reactions = new ArrayList<>();
        var nodes = root.getElementsByTagName("reaction");
        for (int i = 0; i < nodes.getLength(); i++) {
            var el = (Element) nodes.item(i);
            reactions.add(new ReactionSpec(
                el.getAttribute("id"),
                el.getAttribute("trigger"),
                parseConditions(el),
                getTextContent(el, "dialogue"),
                new HashMap<>()
            ));
        }
        return reactions;
    }

    private List<ConditionSpec> parseConditions(Element parent) {
        List<ConditionSpec> conditions = new ArrayList<>();
        var nodes = parent.getElementsByTagName("condition");
        for (int i = 0; i < nodes.getLength(); i++) {
            var el = (Element) nodes.item(i);
            if (el.getParentNode().getNodeName().equals("conditions")) {
                conditions.add(new ConditionSpec(
                    el.getAttribute("type"),
                    el.getAttribute("target") != null ? el.getAttribute("target") : el.getAttribute("axis"),
                    el.getAttribute("operator"),
                    el.getAttribute("value") != null ? el.getAttribute("value") : el.getAttribute("check")
                ));
            }
        }
        return conditions;
    }

    private List<OptionSpec> parseOptions(Element parent) {
        List<OptionSpec> options = new ArrayList<>();
        var nodes = parent.getElementsByTagName("option");
        for (int i = 0; i < nodes.getLength(); i++) {
            var el = (Element) nodes.item(i);
            if (el.getParentNode().getNodeName().equals("options")) {
                options.add(new OptionSpec(
                    el.getAttribute("id"),
                    el.getAttribute("text"),
                    el.getAttribute("result"),
                    intAttr(el, "energy"), intAttr(el, "stress"), intAttr(el, "mood"), intAttr(el, "money"),
                    el.getAttribute("relationship-target"),
                    intAttr(el, "relationship-delta")
                ));
            }
        }
        return options;
    }

    private Map<String, Integer> parseTraits(Element root, String tagName) {
        Map<String, Integer> map = new HashMap<>();
        var parent = root.getElementsByTagName(tagName);
        if (parent.getLength() > 0) {
            var traits = ((Element) parent.item(0)).getElementsByTagName("trait");
            for (int i = 0; i < traits.getLength(); i++) {
                var el = (Element) traits.item(i);
                map.put(el.getAttribute("name"), Integer.parseInt(el.getAttribute("value")));
            }
        }
        return map;
    }

    private Map<String, Integer> parseMoodInitial(Element root) {
        Map<String, Integer> map = new HashMap<>();
        var nodes = root.getElementsByTagName("mood-initial");
        if (nodes.getLength() > 0) {
            var el = (Element) nodes.item(0);
            for (String axis : List.of("happiness", "anxiety", "loneliness", "irritability", "energy", "affection")) {
                String val = el.getAttribute(axis);
                if (val != null && !val.isEmpty()) map.put(axis, Integer.parseInt(val));
            }
        }
        return map;
    }

    private List<String> parseQuestLines(Element root) {
        var nodes = root.getElementsByTagName("quest-lines");
        if (nodes.getLength() > 0) {
            String text = nodes.item(0).getTextContent().trim();
            if (!text.isEmpty()) return Arrays.asList(text.split("\\s*,\\s*"));
        }
        return List.of();
    }

    private String getTextContent(Element root, String tag) {
        var nodes = root.getElementsByTagName(tag);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent().trim() : "";
    }

    private String getAttr(Element root, String tag, String attr, String def) {
        var nodes = root.getElementsByTagName(tag);
        if (nodes.getLength() > 0) {
            String val = ((Element) nodes.item(0)).getAttribute(attr);
            return (val != null && !val.isEmpty()) ? val : def;
        }
        return def;
    }

    private int intAttr(Element el, String name) {
        String val = el.getAttribute(name);
        if (val == null || val.isEmpty()) return 0;
        try { return Integer.parseInt(val); } catch (NumberFormatException e) { return 0; }
    }
}
