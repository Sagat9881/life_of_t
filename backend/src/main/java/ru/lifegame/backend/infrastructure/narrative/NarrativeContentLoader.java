package ru.lifegame.backend.infrastructure.narrative;

import org.springframework.stereotype.Component;
import ru.lifegame.backend.domain.npc.spec.NpcSpec;
import ru.lifegame.backend.domain.npc.spec.ScheduleSlot;
import ru.lifegame.backend.domain.npc.spec.ScoredAction;
import ru.lifegame.backend.domain.npc.spec.ConditionSpec;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipEdge;
import ru.lifegame.backend.domain.npc.graph.CrossNpcConditionSpec;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import java.io.InputStream;
import java.util.*;

/**
 * Loads all narrative content from XML specifications.
 * Scans classpath narrative/ directory for NPC, quest, and event XML files.
 * The backend engine knows ZERO concrete names — all content comes from XML.
 */
@Component
public class NarrativeContentLoader {

    public List<NpcSpec> loadNpcSpecs(String resourcePath) {
        List<NpcSpec> specs = new ArrayList<>();
        try {
            // In production, scan classpath directory for all *.xml files
            // For now, load individual resources passed in
            InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (is == null) return specs;

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(is);
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();
            if (!"npc".equals(root.getTagName())) return specs;

            specs.add(parseNpcElement(root));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load NPC spec from: " + resourcePath, e);
        }
        return specs;
    }

    public List<NpcSpec> loadAllNpcSpecs() {
        List<NpcSpec> allSpecs = new ArrayList<>();
        // Scan narrative/npc-behavior/*.xml from classpath
        String[] knownFiles = discoverNpcFiles();
        for (String file : knownFiles) {
            allSpecs.addAll(loadNpcSpecs("narrative/npc-behavior/" + file));
        }
        return allSpecs;
    }

    public List<NpcRelationshipEdge> loadRelationshipEdges() {
        List<NpcRelationshipEdge> edges = new ArrayList<>();
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("narrative/npc-relationships.xml");
            if (is == null) return edges;

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(is);
            NodeList edgeNodes = doc.getElementsByTagName("edge");

            for (int i = 0; i < edgeNodes.getLength(); i++) {
                Element el = (Element) edgeNodes.item(i);
                edges.add(new NpcRelationshipEdge(
                    el.getAttribute("from"),
                    el.getAttribute("to"),
                    parseIntAttr(el, "respect", 50),
                    parseIntAttr(el, "tension", 0),
                    parseIntAttr(el, "familiarity", 50)
                ));
            }
        } catch (Exception e) {
            // No relationship file — return empty
        }
        return edges;
    }

    public List<CrossNpcConditionSpec> loadCrossNpcTriggers() {
        List<CrossNpcConditionSpec> triggers = new ArrayList<>();
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("narrative/npc-cross-triggers.xml");
            if (is == null) return triggers;

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(is);
            NodeList triggerNodes = doc.getElementsByTagName("trigger");

            for (int i = 0; i < triggerNodes.getLength(); i++) {
                Element el = (Element) triggerNodes.item(i);
                triggers.add(new CrossNpcConditionSpec(
                    el.getAttribute("id"),
                    el.getAttribute("npc-a"),
                    el.getAttribute("npc-b"),
                    el.getAttribute("axis"),
                    el.getAttribute("operator"),
                    parseIntAttr(el, "value", 0),
                    el.getAttribute("event-id")
                ));
            }
        } catch (Exception e) {
            // No cross-trigger file — return empty
        }
        return triggers;
    }

    private NpcSpec parseNpcElement(Element root) {
        String id = root.getAttribute("id");
        String type = root.getAttribute("type");
        String category = root.getAttribute("category");
        String displayName = getTextContent(root, "display-name", id);

        Map<String, Integer> personality = parseTraits(root, "personality");
        Map<String, Integer> moodInitial = parseMoodInitial(root);
        boolean memoryEnabled = parseBoolAttr(root, "memory", "enabled", true);
        int shortTermSize = parseMemorySize(root, 10);

        List<ScheduleSlot> schedule = parseSchedule(root);
        List<ScoredAction> actions = parseActions(root);

        return new NpcSpec(id, type, category, displayName,
                personality, moodInitial, memoryEnabled, shortTermSize,
                schedule, actions);
    }

    private Map<String, Integer> parseTraits(Element root, String parentTag) {
        Map<String, Integer> traits = new LinkedHashMap<>();
        NodeList parents = root.getElementsByTagName(parentTag);
        if (parents.getLength() == 0) return traits;

        Element parent = (Element) parents.item(0);
        NodeList traitNodes = parent.getElementsByTagName("trait");
        for (int i = 0; i < traitNodes.getLength(); i++) {
            Element t = (Element) traitNodes.item(i);
            traits.put(t.getAttribute("name"), Integer.parseInt(t.getAttribute("value")));
        }
        return traits;
    }

    private Map<String, Integer> parseMoodInitial(Element root) {
        Map<String, Integer> mood = new LinkedHashMap<>();
        NodeList nodes = root.getElementsByTagName("mood-initial");
        if (nodes.getLength() == 0) {
            mood.put("happiness", 50);
            mood.put("anxiety", 20);
            mood.put("loneliness", 20);
            mood.put("irritability", 10);
            mood.put("energy", 70);
            mood.put("affection", 50);
            return mood;
        }
        Element el = (Element) nodes.item(0);
        mood.put("happiness", parseIntAttr(el, "happiness", 50));
        mood.put("anxiety", parseIntAttr(el, "anxiety", 20));
        mood.put("loneliness", parseIntAttr(el, "loneliness", 20));
        mood.put("irritability", parseIntAttr(el, "irritability", 10));
        mood.put("energy", parseIntAttr(el, "energy", 70));
        mood.put("affection", parseIntAttr(el, "affection", 50));
        return mood;
    }

    private List<ScheduleSlot> parseSchedule(Element root) {
        List<ScheduleSlot> slots = new ArrayList<>();
        NodeList scheduleNodes = root.getElementsByTagName("schedule");
        if (scheduleNodes.getLength() == 0) return slots;

        Element schedule = (Element) scheduleNodes.item(0);
        NodeList slotNodes = schedule.getElementsByTagName("slot");
        for (int i = 0; i < slotNodes.getLength(); i++) {
            Element s = (Element) slotNodes.item(i);
            slots.add(new ScheduleSlot(
                Integer.parseInt(s.getAttribute("start")),
                Integer.parseInt(s.getAttribute("end")),
                s.getAttribute("activity"),
                s.getAttribute("location"),
                s.getAttribute("animation")
            ));
        }
        return slots;
    }

    private List<ScoredAction> parseActions(Element root) {
        List<ScoredAction> actions = new ArrayList<>();
        NodeList actionsNodes = root.getElementsByTagName("actions");
        if (actionsNodes.getLength() == 0) return actions;

        Element actionsEl = (Element) actionsNodes.item(0);
        NodeList actionNodes = actionsEl.getElementsByTagName("action");
        for (int i = 0; i < actionNodes.getLength(); i++) {
            Element a = (Element) actionNodes.item(i);
            String actionId = a.getAttribute("id");
            double baseScore = Double.parseDouble(a.getAttributeNode("base-score") != null
                    ? a.getAttribute("base-score") : "0.5");
            String eventType = a.getAttribute("event-type");

            List<ConditionSpec> conditions = parseConditions(a);
            Map<String, Map<String, String>> options = parseOptions(a);

            actions.add(new ScoredAction(actionId, baseScore, eventType, conditions, options));
        }
        return actions;
    }

    private List<ConditionSpec> parseConditions(Element parent) {
        List<ConditionSpec> conditions = new ArrayList<>();
        NodeList condNodes = parent.getElementsByTagName("condition");
        for (int i = 0; i < condNodes.getLength(); i++) {
            Element c = (Element) condNodes.item(i);
            if (c.getParentNode().getNodeName().equals("conditions")) {
                conditions.add(new ConditionSpec(
                    c.getAttribute("type"),
                    c.hasAttribute("axis") ? c.getAttribute("axis") : c.getAttribute("check"),
                    c.getAttribute("operator"),
                    c.hasAttribute("value") ? c.getAttribute("value") : ""
                ));
            }
        }
        return conditions;
    }

    private Map<String, Map<String, String>> parseOptions(Element actionEl) {
        Map<String, Map<String, String>> options = new LinkedHashMap<>();
        NodeList optionNodes = actionEl.getElementsByTagName("option");
        for (int i = 0; i < optionNodes.getLength(); i++) {
            Element o = (Element) optionNodes.item(i);
            if (o.getParentNode().getNodeName().equals("options")) {
                Map<String, String> attrs = new LinkedHashMap<>();
                for (int j = 0; j < o.getAttributes().getLength(); j++) {
                    Node attr = o.getAttributes().item(j);
                    attrs.put(attr.getNodeName(), attr.getNodeValue());
                }
                options.put(o.getAttribute("id"), attrs);
            }
        }
        return options;
    }

    private String getTextContent(Element root, String tagName, String defaultVal) {
        NodeList nodes = root.getElementsByTagName(tagName);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent().trim() : defaultVal;
    }

    private int parseIntAttr(Element el, String attr, int defaultVal) {
        String val = el.getAttribute(attr);
        if (val == null || val.isEmpty()) return defaultVal;
        try { return Integer.parseInt(val); } catch (NumberFormatException e) { return defaultVal; }
    }

    private boolean parseBoolAttr(Element root, String parentTag, String attr, boolean defaultVal) {
        NodeList nodes = root.getElementsByTagName(parentTag);
        if (nodes.getLength() == 0) return defaultVal;
        String val = ((Element) nodes.item(0)).getAttribute(attr);
        if (val == null || val.isEmpty()) return defaultVal;
        return Boolean.parseBoolean(val);
    }

    private int parseMemorySize(Element root, int defaultVal) {
        NodeList nodes = root.getElementsByTagName("memory");
        if (nodes.getLength() == 0) return defaultVal;
        return parseIntAttr((Element) nodes.item(0), "short-term-size", defaultVal);
    }

    private String[] discoverNpcFiles() {
        // In production, scan classpath. For now, return empty — 
        // files will be discovered dynamically when narrative XMLs are added
        return new String[]{};
    }
}
