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
 * The engine knows ZERO concrete NPC names — everything comes from XML.
 */
@Component
public class NarrativeContentLoader {

    private final List<NpcSpec> npcSpecs = new ArrayList<>();
    private final List<NpcRelationshipEdge> relationshipEdges = new ArrayList<>();

    public List<NpcSpec> loadNpcSpecs(List<String> xmlResourcePaths) {
        npcSpecs.clear();
        for (String path : xmlResourcePaths) {
            try {
                NpcSpec spec = parseNpcXml(path);
                if (spec != null) {
                    npcSpecs.add(spec);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse NPC spec: " + path, e);
            }
        }
        return Collections.unmodifiableList(npcSpecs);
    }

    public List<NpcRelationshipEdge> loadRelationshipEdges(List<String> xmlResourcePaths) {
        relationshipEdges.clear();
        for (String path : xmlResourcePaths) {
            try {
                List<NpcRelationshipEdge> edges = parseRelationshipsXml(path);
                relationshipEdges.addAll(edges);
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse relationships: " + path, e);
            }
        }
        return Collections.unmodifiableList(relationshipEdges);
    }

    public List<NpcSpec> getNpcSpecs() {
        return Collections.unmodifiableList(npcSpecs);
    }

    private NpcSpec parseNpcXml(String resourcePath) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) return null;

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(is);
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

        return new NpcSpec(id, type, category, displayName,
                personality, moodInitial, memoryEnabled, shortTermSize,
                schedule, actions);
    }

    private Map<String, Integer> parsePersonality(Element root) {
        Map<String, Integer> traits = new LinkedHashMap<>();
        NodeList personalityNodes = root.getElementsByTagName("personality");
        if (personalityNodes.getLength() > 0) {
            Element personalityEl = (Element) personalityNodes.item(0);
            NodeList traitNodes = personalityEl.getElementsByTagName("trait");
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
        NodeList moodNodes = root.getElementsByTagName("mood-initial");
        if (moodNodes.getLength() > 0) {
            Element moodEl = (Element) moodNodes.item(0);
            String[] axes = {"happiness", "anxiety", "loneliness", "irritability", "energy", "affection"};
            for (String axis : axes) {
                String val = moodEl.getAttribute(axis);
                if (!val.isEmpty()) {
                    mood.put(axis, Integer.parseInt(val));
                }
            }
        }
        return mood;
    }

    private boolean parseMemoryEnabled(Element root) {
        NodeList memNodes = root.getElementsByTagName("memory");
        if (memNodes.getLength() > 0) {
            return Boolean.parseBoolean(((Element) memNodes.item(0)).getAttribute("enabled"));
        }
        return false;
    }

    private int parseShortTermSize(Element root) {
        NodeList memNodes = root.getElementsByTagName("memory");
        if (memNodes.getLength() > 0) {
            String size = ((Element) memNodes.item(0)).getAttribute("short-term-size");
            return size.isEmpty() ? 5 : Integer.parseInt(size);
        }
        return 5;
    }

    private List<ScheduleSlot> parseSchedule(Element root) {
        List<ScheduleSlot> slots = new ArrayList<>();
        NodeList scheduleNodes = root.getElementsByTagName("schedule");
        if (scheduleNodes.getLength() > 0) {
            Element scheduleEl = (Element) scheduleNodes.item(0);
            NodeList slotNodes = scheduleEl.getElementsByTagName("slot");
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
                Element actionEl = (Element) actionNodes.item(i);
                String actionId = actionEl.getAttribute("id");
                double baseScore = Double.parseDouble(actionEl.getAttribute("base-score"));
                String eventType = actionEl.getAttribute("event-type");

                List<ConditionSpec> conditions = parseConditions(actionEl);
                List<Map<String, String>> options = parseOptions(actionEl);

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
                for (int a = 0; a < opt.getAttributes().getLength(); a++) {
                    optMap.put(opt.getAttributes().item(a).getNodeName(),
                            opt.getAttributes().item(a).getNodeValue());
                }
                options.add(optMap);
            }
        }
        return options;
    }

    private List<NpcRelationshipEdge> parseRelationshipsXml(String resourcePath) throws Exception {
        List<NpcRelationshipEdge> edges = new ArrayList<>();
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) return edges;

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(is);
        NodeList edgeNodes = doc.getElementsByTagName("edge");
        for (int i = 0; i < edgeNodes.getLength(); i++) {
            Element edge = (Element) edgeNodes.item(i);
            edges.add(new NpcRelationshipEdge(
                    edge.getAttribute("from"),
                    edge.getAttribute("to"),
                    Integer.parseInt(edge.getAttribute("respect")),
                    Integer.parseInt(edge.getAttribute("tension")),
                    Integer.parseInt(edge.getAttribute("familiarity"))
            ));
        }
        return edges;
    }

    private String getTextContent(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return "";
    }
}
