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
 * Loads all narrative content from XML specification files.
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

    private NpcSpec parseNpcElement(Element npcEl) {
        String id = npcEl.getAttribute("id");
        String type = npcEl.getAttribute("type");
        String category = npcEl.getAttribute("category");
        String displayName = getTextContent(npcEl, "display-name", id);

        Map<String, Integer> personality = parsePersonality(npcEl);
        Map<String, Integer> moodInitial = parseMoodInitial(npcEl);
        boolean memoryEnabled = parseMemoryEnabled(npcEl);
        int shortTermSize = parseShortTermSize(npcEl);
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

    private Map<String, Integer> parseMoodInitial(Element npcEl) {
        Map<String, Integer> mood = new LinkedHashMap<>();
        NodeList moodNodes = npcEl.getElementsByTagName("mood-initial");
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

    private boolean parseMemoryEnabled(Element npcEl) {
        NodeList memNodes = npcEl.getElementsByTagName("memory");
        if (memNodes.getLength() > 0) {
            return Boolean.parseBoolean(((Element) memNodes.item(0)).getAttribute("enabled"));
        }
        return false;
    }

    private int parseShortTermSize(Element npcEl) {
        NodeList memNodes = npcEl.getElementsByTagName("memory");
        if (memNodes.getLength() > 0) {
            String size = ((Element) memNodes.item(0)).getAttribute("short-term-size");
            return size.isEmpty() ? 5 : Integer.parseInt(size);
        }
        return 5;
    }

    private List<ScheduleSlot> parseSchedule(Element npcEl) {
        List<ScheduleSlot> slots = new ArrayList<>();
        NodeList scheduleNodes = npcEl.getElementsByTagName("schedule");
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

    private List<ScoredAction> parseActions(Element npcEl) {
        List<ScoredAction> actions = new ArrayList<>();
        NodeList actionsNodes = npcEl.getElementsByTagName("actions");
        if (actionsNodes.getLength() > 0) {
            Element actionsEl = (Element) actionsNodes.item(0);
            NodeList actionNodes = actionsEl.getElementsByTagName("action");
            for (int i = 0; i < actionNodes.getLength(); i++) {
                Element actionEl = (Element) actionNodes.item(i);
                String actionId = actionEl.getAttribute("id");
                double baseScore = Double.parseDouble(
                        actionEl.getAttribute("base-score").isEmpty() ? "0.5" : actionEl.getAttribute("base-score"));
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
        NodeList conditionsNodes = parent.getElementsByTagName("conditions");
        if (conditionsNodes.getLength() > 0) {
            Element conditionsEl = (Element) conditionsNodes.item(0);
            NodeList condNodes = conditionsEl.getElementsByTagName("condition");
            for (int i = 0; i < condNodes.getLength(); i++) {
                Element cond = (Element) condNodes.item(i);
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

    private List<Map<String, String>> parseOptions(Element actionEl) {
        List<Map<String, String>> options = new ArrayList<>();
        NodeList optionsNodes = actionEl.getElementsByTagName("options");
        if (optionsNodes.getLength() > 0) {
            Element optionsEl = (Element) optionsNodes.item(0);
            NodeList optionNodes = optionsEl.getElementsByTagName("option");
            for (int i = 0; i < optionNodes.getLength(); i++) {
                Element opt = (Element) optionNodes.item(i);
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
                Element edgeEl = (Element) edgeNodes.item(i);
                edges.add(new NpcRelationshipEdge(
                        edgeEl.getAttribute("from"),
                        edgeEl.getAttribute("to"),
                        Integer.parseInt(edgeEl.getAttribute("respect")),
                        Integer.parseInt(edgeEl.getAttribute("tension")),
                        Integer.parseInt(edgeEl.getAttribute("familiarity"))
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load relationship edges from: " + resourcePath, e);
        }
        return edges;
    }

    private String getTextContent(Element parent, String tagName, String defaultValue) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return defaultValue;
    }
}
