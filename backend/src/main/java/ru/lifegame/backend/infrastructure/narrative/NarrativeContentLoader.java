package ru.lifegame.backend.infrastructure.narrative;

import org.springframework.stereotype.Component;
import ru.lifegame.backend.domain.npc.spec.NpcSpec;
import ru.lifegame.backend.domain.npc.spec.ScheduleSlot;
import ru.lifegame.backend.domain.npc.spec.ScoredAction;
import ru.lifegame.backend.domain.npc.spec.ConditionSpec;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipEdge;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import java.io.InputStream;
import java.util.*;

/**
 * Loads all narrative content from XML specifications.
 * Scans narrative/npc-behavior/*.xml on classpath.
 * Backend knows ZERO concrete NPC names — everything comes from XML.
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
        // In production, scan classpath directory for all *.xml files
        // For now, load from a manifest or known file list
        List<NpcSpec> allSpecs = new ArrayList<>();
        try {
            InputStream manifest = getClass().getClassLoader().getResourceAsStream(directoryPath + "/manifest.xml");
            if (manifest == null) return allSpecs;

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(manifest);
            doc.getDocumentElement().normalize();

            NodeList fileNodes = doc.getElementsByTagName("file");
            for (int i = 0; i < fileNodes.getLength(); i++) {
                String fileName = fileNodes.item(i).getTextContent().trim();
                allSpecs.addAll(loadNpcSpecs(directoryPath + "/" + fileName));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load NPC manifest from: " + directoryPath, e);
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
            throw new RuntimeException("Failed to load relationship edges from: " + resourcePath, e);
        }
        return edges;
    }

    private NpcSpec parseNpcElement(Element npcEl) {
        String id = npcEl.getAttribute("id");
        String type = npcEl.getAttribute("type");
        String category = npcEl.getAttribute("category");
        String displayName = getTextContent(npcEl, "display-name", id);

        Map<String, Integer> personality = parsePersonality(npcEl);
        Map<String, Integer> moodInitial = parseMoodInitial(npcEl);
        boolean memoryEnabled = parseBooleanAttr(npcEl, "memory", "enabled", "named".equals(type));
        int shortTermSize = parseIntNestedAttr(npcEl, "memory", "short-term-size", 10);
        List<ScheduleSlot> schedule = parseSchedule(npcEl);
        List<ScoredAction> actions = parseActions(npcEl);

        return new NpcSpec(id, type, category, displayName,
                personality, moodInitial, memoryEnabled, shortTermSize,
                schedule, actions);
    }

    private Map<String, Integer> parsePersonality(Element npcEl) {
        Map<String, Integer> traits = new LinkedHashMap<>();
        NodeList personalityNodes = npcEl.getElementsByTagName("personality");
        if (personalityNodes.getLength() > 0) {
            Element persEl = (Element) personalityNodes.item(0);
            NodeList traitNodes = persEl.getElementsByTagName("trait");
            for (int i = 0; i < traitNodes.getLength(); i++) {
                Element t = (Element) traitNodes.item(i);
                traits.put(t.getAttribute("name"), Integer.parseInt(t.getAttribute("value")));
            }
        }
        return traits;
    }

    private Map<String, Integer> parseMoodInitial(Element npcEl) {
        Map<String, Integer> mood = new LinkedHashMap<>();
        NodeList moodNodes = npcEl.getElementsByTagName("mood-initial");
        if (moodNodes.getLength() > 0) {
            Element moodEl = (Element) moodNodes.item(0);
            for (String axis : List.of("happiness", "anxiety", "loneliness", "irritability", "energy", "affection")) {
                String val = moodEl.getAttribute(axis);
                if (!val.isEmpty()) {
                    mood.put(axis, Integer.parseInt(val));
                }
            }
        }
        return mood;
    }

    private List<ScheduleSlot> parseSchedule(Element npcEl) {
        List<ScheduleSlot> slots = new ArrayList<>();
        NodeList scheduleNodes = npcEl.getElementsByTagName("schedule");
        if (scheduleNodes.getLength() > 0) {
            Element schedEl = (Element) scheduleNodes.item(0);
            NodeList slotNodes = schedEl.getElementsByTagName("slot");
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
        }
        return slots;
    }

    private List<ScoredAction> parseActions(Element npcEl) {
        List<ScoredAction> actions = new ArrayList<>();
        NodeList actionsNodes = npcEl.getElementsByTagName("actions");
        if (actionsNodes.getLength() > 0) {
            Element actionsEl = (Element) actionsNodes.item(0);
            NodeList actionNodes = actionsEl.getElementsByTagName("action");
            for (int i = 0; i < actionNodes.getLength(); i++) {
                Element a = (Element) actionNodes.item(i);
                String actionId = a.getAttribute("id");
                double baseScore = Double.parseDouble(a.getAttribute("base-score"));
                String eventType = a.hasAttribute("event-type") ? a.getAttribute("event-type") : "NPC_INITIATED";

                List<ConditionSpec> conditions = parseConditions(a);
                List<Map<String, String>> options = parseOptions(a);

                actions.add(new ScoredAction(actionId, baseScore, eventType, conditions, options));
            }
        }
        return actions;
    }

    private List<ConditionSpec> parseConditions(Element parent) {
        List<ConditionSpec> conditions = new ArrayList<>();
        NodeList condNodes = parent.getElementsByTagName("condition");
        for (int i = 0; i < condNodes.getLength(); i++) {
            Element c = (Element) condNodes.item(i);
            // Only direct children of <conditions>
            if (c.getParentNode() instanceof Element pe && "conditions".equals(pe.getTagName())) {
                conditions.add(new ConditionSpec(
                        c.getAttribute("type"),
                        c.hasAttribute("axis") ? c.getAttribute("axis") : c.getAttribute("target"),
                        c.hasAttribute("operator") ? c.getAttribute("operator") : "gte",
                        c.hasAttribute("value") ? c.getAttribute("value") : "0"
                ));
            }
        }
        return conditions;
    }

    private List<Map<String, String>> parseOptions(Element actionEl) {
        List<Map<String, String>> options = new ArrayList<>();
        NodeList optionNodes = actionEl.getElementsByTagName("option");
        for (int i = 0; i < optionNodes.getLength(); i++) {
            Element o = (Element) optionNodes.item(i);
            if (o.getParentNode() instanceof Element pe && "options".equals(pe.getTagName())) {
                Map<String, String> opt = new LinkedHashMap<>();
                var attrs = o.getAttributes();
                for (int j = 0; j < attrs.getLength(); j++) {
                    opt.put(attrs.item(j).getNodeName(), attrs.item(j).getNodeValue());
                }
                options.add(opt);
            }
        }
        return options;
    }

    private String getTextContent(Element parent, String tagName, String defaultVal) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent().trim() : defaultVal;
    }

    private boolean parseBooleanAttr(Element parent, String tagName, String attrName, boolean defaultVal) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            String val = ((Element) nodes.item(0)).getAttribute(attrName);
            if (!val.isEmpty()) return Boolean.parseBoolean(val);
        }
        return defaultVal;
    }

    private int parseIntNestedAttr(Element parent, String tagName, String attrName, int defaultVal) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            String val = ((Element) nodes.item(0)).getAttribute(attrName);
            if (!val.isEmpty()) return Integer.parseInt(val);
        }
        return defaultVal;
    }

    private int parseIntAttr(Element el, String attrName, int defaultVal) {
        String val = el.getAttribute(attrName);
        return val.isEmpty() ? defaultVal : Integer.parseInt(val);
    }
}
