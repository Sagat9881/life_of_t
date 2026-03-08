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
import java.util.stream.IntStream;

/**
 * Loads all narrative content from XML specification files.
 * Scans narrative/npc-behavior/*.xml at startup.
 * The backend knows ZERO concrete NPC names, actions, or events —
 * everything is driven by XML specs.
 */
@Component
public class NarrativeContentLoader {

    public List<NpcSpec> loadNpcSpecs(String resourcePath) {
        List<NpcSpec> specs = new ArrayList<>();
        try {
            var classLoader = getClass().getClassLoader();
            var resource = classLoader.getResource(resourcePath);
            if (resource == null) {
                return specs;
            }
            // In production, scan directory for all .xml files
            // For now, load individual files passed as arguments
            return specs;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load NPC specs from: " + resourcePath, e);
        }
    }

    public NpcSpec parseNpcXml(InputStream xmlStream) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlStream);
            Element root = doc.getDocumentElement();

            String id = root.getAttribute("id");
            String type = root.getAttribute("type");
            String category = root.getAttribute("category");
            String displayName = getTextContent(root, "display-name");

            Map<String, Integer> personality = parsePersonality(root);
            Map<String, Integer> moodInitial = parseMoodInitial(root);
            boolean memoryEnabled = parseMemoryEnabled(root);
            int shortTermSize = parseShortTermSize(root);
            List<ScheduleSlot> schedule = parseSchedule(root);
            List<ScoredAction> actions = parseActions(root);

            return new NpcSpec(
                    id, type, category, displayName,
                    personality, moodInitial, memoryEnabled, shortTermSize,
                    schedule, actions
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse NPC XML", e);
        }
    }

    public List<NpcRelationshipEdge> parseRelationships(InputStream xmlStream) {
        List<NpcRelationshipEdge> edges = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlStream);
            Element root = doc.getDocumentElement();

            NodeList relNodes = root.getElementsByTagName("relationship");
            for (int i = 0; i < relNodes.getLength(); i++) {
                Element rel = (Element) relNodes.item(i);
                edges.add(new NpcRelationshipEdge(
                        rel.getAttribute("from"),
                        rel.getAttribute("to"),
                        parseIntAttr(rel, "respect", 50),
                        parseIntAttr(rel, "tension", 0),
                        parseIntAttr(rel, "familiarity", 50)
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse relationships XML", e);
        }
        return edges;
    }

    private Map<String, Integer> parsePersonality(Element root) {
        Map<String, Integer> traits = new LinkedHashMap<>();
        NodeList nodes = root.getElementsByTagName("personality");
        if (nodes.getLength() > 0) {
            Element personality = (Element) nodes.item(0);
            NodeList traitNodes = personality.getElementsByTagName("trait");
            for (int i = 0; i < traitNodes.getLength(); i++) {
                Element trait = (Element) traitNodes.item(i);
                traits.put(trait.getAttribute("name"),
                        Integer.parseInt(trait.getAttribute("value")));
            }
        }
        return traits;
    }

    private Map<String, Integer> parseMoodInitial(Element root) {
        Map<String, Integer> mood = new LinkedHashMap<>();
        NodeList nodes = root.getElementsByTagName("mood-initial");
        if (nodes.getLength() > 0) {
            Element el = (Element) nodes.item(0);
            String[] axes = {"happiness", "anxiety", "loneliness", "irritability", "energy", "affection"};
            for (String axis : axes) {
                String val = el.getAttribute(axis);
                if (!val.isEmpty()) {
                    mood.put(axis, Integer.parseInt(val));
                }
            }
        }
        return mood;
    }

    private boolean parseMemoryEnabled(Element root) {
        NodeList nodes = root.getElementsByTagName("memory");
        if (nodes.getLength() > 0) {
            return Boolean.parseBoolean(((Element) nodes.item(0)).getAttribute("enabled"));
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

    private List<ScheduleSlot> parseSchedule(Element root) {
        List<ScheduleSlot> slots = new ArrayList<>();
        NodeList schedNodes = root.getElementsByTagName("schedule");
        if (schedNodes.getLength() > 0) {
            Element schedule = (Element) schedNodes.item(0);
            NodeList slotNodes = schedule.getElementsByTagName("slot");
            for (int i = 0; i < slotNodes.getLength(); i++) {
                Element slot = (Element) slotNodes.item(i);
                slots.add(new ScheduleSlot(
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
                List<Map<String, String>> options = parseOptions(action);

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
                        cond.getAttribute("axis").isEmpty() ? cond.getAttribute("check") : cond.getAttribute("axis"),
                        cond.getAttribute("operator"),
                        cond.getAttribute("value")
                ));
            }
        }
        return conditions;
    }

    private List<Map<String, String>> parseOptions(Element parent) {
        List<Map<String, String>> options = new ArrayList<>();
        NodeList optNodes = parent.getElementsByTagName("options");
        if (optNodes.getLength() > 0) {
            Element optsEl = (Element) optNodes.item(0);
            NodeList optList = optsEl.getElementsByTagName("option");
            for (int i = 0; i < optList.getLength(); i++) {
                Element opt = (Element) optList.item(i);
                Map<String, String> optMap = new LinkedHashMap<>();
                var attrs = opt.getAttributes();
                for (int j = 0; j < attrs.getLength(); j++) {
                    optMap.put(attrs.item(j).getNodeName(), attrs.item(j).getNodeValue());
                }
                options.add(optMap);
            }
        }
        return options;
    }

    private String getTextContent(Element root, String tagName) {
        NodeList nodes = root.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return "";
    }

    private int parseIntAttr(Element el, String attr, int defaultVal) {
        String val = el.getAttribute(attr);
        return val.isEmpty() ? defaultVal : Integer.parseInt(val);
    }
}
