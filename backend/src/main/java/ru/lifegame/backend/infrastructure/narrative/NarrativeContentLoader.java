package ru.lifegame.backend.infrastructure.narrative;

import org.springframework.stereotype.Component;
import ru.lifegame.backend.domain.npc.spec.NpcSpec;
import ru.lifegame.backend.domain.npc.spec.ScheduleSlot;
import ru.lifegame.backend.domain.npc.spec.ScoredAction;
import ru.lifegame.backend.domain.npc.spec.ConditionSpec;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipEdge;

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
 * Scans narrative/npc-behavior/*.xml at startup.
 * The backend knows ZERO concrete NPC names — everything comes from XML.
 */
@Component
public class NarrativeContentLoader {

    public List<NpcSpec> loadNpcSpecs(String resourcePath) {
        List<NpcSpec> specs = new ArrayList<>();
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (is == null) return specs;

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(is);
            doc.getDocumentElement().normalize();

            NodeList npcNodes = doc.getElementsByTagName("npc");
            for (int i = 0; i < npcNodes.getLength(); i++) {
                Element npcEl = (Element) npcNodes.item(i);
                specs.add(parseNpcElement(npcEl));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load NPC specs from: " + resourcePath, e);
        }
        return specs;
    }

    public List<NpcSpec> loadAllNpcSpecsFromDirectory(String directoryPath) {
        List<NpcSpec> allSpecs = new ArrayList<>();
        // In production, scan classpath directory for all .xml files
        // For now, load from a manifest or iterate known files
        try {
            InputStream manifest = getClass().getClassLoader().getResourceAsStream(directoryPath + "/manifest.txt");
            if (manifest == null) return allSpecs;

            Scanner scanner = new Scanner(manifest);
            while (scanner.hasNextLine()) {
                String fileName = scanner.nextLine().trim();
                if (!fileName.isEmpty() && fileName.endsWith(".xml")) {
                    allSpecs.addAll(loadNpcSpecs(directoryPath + "/" + fileName));
                }
            }
            scanner.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to scan NPC directory: " + directoryPath, e);
        }
        return allSpecs;
    }

    public List<NpcRelationshipEdge> loadRelationshipEdges(String resourcePath) {
        List<NpcRelationshipEdge> edges = new ArrayList<>();
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (is == null) return edges;

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(is);
            doc.getDocumentElement().normalize();

            NodeList edgeNodes = doc.getElementsByTagName("relationship");
            for (int i = 0; i < edgeNodes.getLength(); i++) {
                Element el = (Element) edgeNodes.item(i);
                edges.add(new NpcRelationshipEdge(
                    el.getAttribute("npc-a"),
                    el.getAttribute("npc-b"),
                    parseIntAttr(el, "respect", 50),
                    parseIntAttr(el, "tension", 0),
                    parseIntAttr(el, "familiarity", 50)
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load relationship edges: " + resourcePath, e);
        }
        return edges;
    }

    private NpcSpec parseNpcElement(Element npcEl) {
        String id = npcEl.getAttribute("id");
        String type = npcEl.getAttribute("type");
        String category = npcEl.getAttribute("category");
        String displayName = getTextContent(npcEl, "display-name", id);
        boolean isNamed = "named".equals(type);

        Map<String, Integer> personalityTraits = parseTraits(npcEl, "personality");
        Map<String, Integer> moodInitial = parseMoodInitial(npcEl);
        List<ScheduleSlot> schedule = parseSchedule(npcEl);
        List<ScoredAction> actions = parseScoredActions(npcEl);
        boolean memoryEnabled = parseMemoryEnabled(npcEl);
        int shortTermSize = parseShortTermSize(npcEl);

        return new NpcSpec(id, displayName, type, category, isNamed,
                personalityTraits, moodInitial, schedule, actions,
                memoryEnabled, shortTermSize);
    }

    private Map<String, Integer> parseTraits(Element parent, String sectionTag) {
        Map<String, Integer> traits = new LinkedHashMap<>();
        NodeList sections = parent.getElementsByTagName(sectionTag);
        if (sections.getLength() == 0) return traits;

        Element section = (Element) sections.item(0);
        NodeList traitNodes = section.getElementsByTagName("trait");
        for (int i = 0; i < traitNodes.getLength(); i++) {
            Element t = (Element) traitNodes.item(i);
            traits.put(t.getAttribute("name"), Integer.parseInt(t.getAttribute("value")));
        }
        return traits;
    }

    private Map<String, Integer> parseMoodInitial(Element npcEl) {
        Map<String, Integer> mood = new LinkedHashMap<>();
        NodeList moodNodes = npcEl.getElementsByTagName("mood-initial");
        if (moodNodes.getLength() == 0) {
            mood.put("happiness", 50);
            mood.put("anxiety", 20);
            mood.put("loneliness", 20);
            mood.put("irritability", 10);
            mood.put("energy", 70);
            mood.put("affection", 50);
            return mood;
        }
        Element el = (Element) moodNodes.item(0);
        mood.put("happiness", parseIntAttr(el, "happiness", 50));
        mood.put("anxiety", parseIntAttr(el, "anxiety", 20));
        mood.put("loneliness", parseIntAttr(el, "loneliness", 20));
        mood.put("irritability", parseIntAttr(el, "irritability", 10));
        mood.put("energy", parseIntAttr(el, "energy", 70));
        mood.put("affection", parseIntAttr(el, "affection", 50));
        return mood;
    }

    private List<ScheduleSlot> parseSchedule(Element npcEl) {
        List<ScheduleSlot> slots = new ArrayList<>();
        NodeList schedNodes = npcEl.getElementsByTagName("schedule");
        if (schedNodes.getLength() == 0) return slots;

        Element schedEl = (Element) schedNodes.item(0);
        NodeList slotNodes = schedEl.getElementsByTagName("slot");
        for (int i = 0; i < slotNodes.getLength(); i++) {
            Element s = (Element) slotNodes.item(i);
            slots.add(new ScheduleSlot(
                parseIntAttr(s, "start", 0),
                parseIntAttr(s, "end", 24),
                s.getAttribute("activity"),
                s.getAttribute("location"),
                s.getAttribute("animation")
            ));
        }
        return slots;
    }

    private List<ScoredAction> parseScoredActions(Element npcEl) {
        List<ScoredAction> actions = new ArrayList<>();
        NodeList actionsNodes = npcEl.getElementsByTagName("actions");
        if (actionsNodes.getLength() == 0) return actions;

        Element actionsEl = (Element) actionsNodes.item(0);
        NodeList actionNodes = actionsEl.getElementsByTagName("action");
        for (int i = 0; i < actionNodes.getLength(); i++) {
            Element a = (Element) actionNodes.item(i);
            String actionId = a.getAttribute("id");
            double baseScore = parseDoubleAttr(a, "base-score", 0.5);
            String eventType = a.getAttribute("event-type");

            List<ConditionSpec> conditions = parseConditions(a);

            List<Map<String, String>> options = new ArrayList<>();
            NodeList optNodes = a.getElementsByTagName("option");
            for (int j = 0; j < optNodes.getLength(); j++) {
                Element opt = (Element) optNodes.item(j);
                Map<String, String> optMap = new LinkedHashMap<>();
                var attrs = opt.getAttributes();
                for (int k = 0; k < attrs.getLength(); k++) {
                    optMap.put(attrs.item(k).getNodeName(), attrs.item(k).getNodeValue());
                }
                options.add(optMap);
            }

            actions.add(new ScoredAction(actionId, baseScore, eventType, conditions, options));
        }
        return actions;
    }

    private List<ConditionSpec> parseConditions(Element parent) {
        List<ConditionSpec> conditions = new ArrayList<>();
        NodeList condNodes = parent.getElementsByTagName("condition");
        for (int i = 0; i < condNodes.getLength(); i++) {
            Element c = (Element) condNodes.item(i);
            conditions.add(new ConditionSpec(
                c.getAttribute("type"),
                c.getAttribute("axis").isEmpty() ? c.getAttribute("check") : c.getAttribute("axis"),
                c.getAttribute("operator").isEmpty() ? "eq" : c.getAttribute("operator"),
                c.getAttribute("value").isEmpty() ? "true" : c.getAttribute("value")
            ));
        }
        return conditions;
    }

    private boolean parseMemoryEnabled(Element npcEl) {
        NodeList memNodes = npcEl.getElementsByTagName("memory");
        if (memNodes.getLength() == 0) return false;
        return "true".equals(((Element) memNodes.item(0)).getAttribute("enabled"));
    }

    private int parseShortTermSize(Element npcEl) {
        NodeList memNodes = npcEl.getElementsByTagName("memory");
        if (memNodes.getLength() == 0) return 5;
        return parseIntAttr((Element) memNodes.item(0), "short-term-size", 10);
    }

    private String getTextContent(Element parent, String tagName, String defaultValue) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) return defaultValue;
        return nodes.item(0).getTextContent().trim();
    }

    private int parseIntAttr(Element el, String attr, int defaultValue) {
        String val = el.getAttribute(attr);
        if (val == null || val.isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private double parseDoubleAttr(Element el, String attr, double defaultValue) {
        String val = el.getAttribute(attr);
        if (val == null || val.isEmpty()) return defaultValue;
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
