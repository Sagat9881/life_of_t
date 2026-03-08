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
import java.util.stream.Collectors;

/**
 * Loads all narrative content from XML specification files.
 * Scans narrative/npc-behavior/*.xml at startup.
 * Backend knows ZERO concrete NPC names — everything comes from XML.
 */
@Component
public class NarrativeContentLoader {

    public List<NpcSpec> loadNpcSpecs(String resourcePath) {
        List<NpcSpec> specs = new ArrayList<>();
        try {
            // Scan all XML files in the resource path
            ClassLoader cl = getClass().getClassLoader();
            InputStream index = cl.getResourceAsStream(resourcePath);
            if (index == null) {
                return specs;
            }
            // In production, use Spring's ResourcePatternResolver
            // For now, parse individual files passed via configuration
            return specs;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load narrative specs from: " + resourcePath, e);
        }
    }

    public NpcSpec parseNpcXml(InputStream xmlStream) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(xmlStream);
            Element root = doc.getDocumentElement();

            String id = root.getAttribute("id");
            String type = root.getAttribute("type");
            String category = root.getAttribute("category");
            String displayName = getTextContent(root, "display-name");

            // Parse personality traits
            Map<String, Integer> personality = parseTraits(root, "personality");

            // Parse initial mood
            Map<String, Integer> moodInitial = parseMoodInitial(root);

            // Parse memory config
            boolean memoryEnabled = false;
            int shortTermSize = 10;
            NodeList memoryNodes = root.getElementsByTagName("memory");
            if (memoryNodes.getLength() > 0) {
                Element memEl = (Element) memoryNodes.item(0);
                memoryEnabled = Boolean.parseBoolean(memEl.getAttribute("enabled"));
                String sts = memEl.getAttribute("short-term-size");
                if (!sts.isEmpty()) shortTermSize = Integer.parseInt(sts);
            }

            // Parse schedule
            List<ScheduleSlot> schedule = parseSchedule(root);

            // Parse actions
            List<ScoredAction> actions = parseActions(root);

            return new NpcSpec(
                id, type, category, displayName,
                personality, moodInitial,
                memoryEnabled, shortTermSize,
                schedule, actions
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse NPC XML", e);
        }
    }

    private Map<String, Integer> parseTraits(Element root, String tagName) {
        Map<String, Integer> traits = new LinkedHashMap<>();
        NodeList nodes = root.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            Element container = (Element) nodes.item(0);
            NodeList traitNodes = container.getElementsByTagName("trait");
            for (int i = 0; i < traitNodes.getLength(); i++) {
                Element t = (Element) traitNodes.item(i);
                traits.put(t.getAttribute("name"), Integer.parseInt(t.getAttribute("value")));
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

    private List<ScheduleSlot> parseSchedule(Element root) {
        List<ScheduleSlot> slots = new ArrayList<>();
        NodeList schedNodes = root.getElementsByTagName("schedule");
        if (schedNodes.getLength() > 0) {
            Element schedEl = (Element) schedNodes.item(0);
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

    private List<ScoredAction> parseActions(Element root) {
        List<ScoredAction> actions = new ArrayList<>();
        NodeList actionsNodes = root.getElementsByTagName("actions");
        if (actionsNodes.getLength() == 0) return actions;

        Element actionsEl = (Element) actionsNodes.item(0);
        NodeList actionNodes = actionsEl.getElementsByTagName("action");
        for (int i = 0; i < actionNodes.getLength(); i++) {
            Element a = (Element) actionNodes.item(i);
            String actionId = a.getAttribute("id");
            double baseScore = Double.parseDouble(a.getAttribute("base-score"));
            String eventType = a.hasAttribute("event-type") ? a.getAttribute("event-type") : "GENERIC";

            // Parse conditions
            List<ConditionSpec> conditions = parseConditions(a);

            // Parse options
            List<ScoredAction.ActionOption> options = parseOptions(a);

            actions.add(new ScoredAction(actionId, baseScore, eventType, conditions, options));
        }
        return actions;
    }

    private List<ConditionSpec> parseConditions(Element parent) {
        List<ConditionSpec> conditions = new ArrayList<>();
        NodeList condNodes = parent.getElementsByTagName("conditions");
        if (condNodes.getLength() == 0) return conditions;

        Element condEl = (Element) condNodes.item(0);
        NodeList conds = condEl.getElementsByTagName("condition");
        for (int i = 0; i < conds.getLength(); i++) {
            Element c = (Element) conds.item(i);
            conditions.add(new ConditionSpec(
                c.getAttribute("type"),
                c.hasAttribute("axis") ? c.getAttribute("axis") : c.getAttribute("check"),
                c.hasAttribute("operator") ? c.getAttribute("operator") : "eq",
                c.hasAttribute("value") ? c.getAttribute("value") : "true"
            ));
        }
        return conditions;
    }

    private List<ScoredAction.ActionOption> parseOptions(Element parent) {
        List<ScoredAction.ActionOption> options = new ArrayList<>();
        NodeList optNodes = parent.getElementsByTagName("options");
        if (optNodes.getLength() == 0) return options;

        Element optEl = (Element) optNodes.item(0);
        NodeList opts = optEl.getElementsByTagName("option");
        for (int i = 0; i < opts.getLength(); i++) {
            Element o = (Element) opts.item(i);
            Map<String, String> effects = new LinkedHashMap<>();
            // Collect all attributes as effects
            var attrs = o.getAttributes();
            for (int j = 0; j < attrs.getLength(); j++) {
                var attr = attrs.item(j);
                String name = attr.getNodeName();
                if (!name.equals("id") && !name.equals("text") && !name.equals("result")) {
                    effects.put(name, attr.getNodeValue());
                }
            }
            options.add(new ScoredAction.ActionOption(
                o.getAttribute("id"),
                o.getAttribute("text"),
                o.getAttribute("result"),
                effects
            ));
        }
        return options;
    }

    public List<NpcRelationshipEdge> parseRelationshipGraph(InputStream xmlStream) {
        List<NpcRelationshipEdge> edges = new ArrayList<>();
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(xmlStream);
            NodeList edgeNodes = doc.getElementsByTagName("edge");
            for (int i = 0; i < edgeNodes.getLength(); i++) {
                Element e = (Element) edgeNodes.item(i);
                edges.add(new NpcRelationshipEdge(
                    e.getAttribute("from"),
                    e.getAttribute("to"),
                    Integer.parseInt(e.getAttribute("respect")),
                    Integer.parseInt(e.getAttribute("tension")),
                    Integer.parseInt(e.getAttribute("familiarity"))
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse relationship graph XML", e);
        }
        return edges;
    }

    public List<CrossNpcConditionSpec> parseCrossNpcTriggers(InputStream xmlStream) {
        List<CrossNpcConditionSpec> triggers = new ArrayList<>();
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(xmlStream);
            NodeList triggerNodes = doc.getElementsByTagName("trigger");
            for (int i = 0; i < triggerNodes.getLength(); i++) {
                Element t = (Element) triggerNodes.item(i);
                triggers.add(new CrossNpcConditionSpec(
                    t.getAttribute("id"),
                    t.getAttribute("npc-a"),
                    t.getAttribute("npc-b"),
                    t.getAttribute("field"),
                    t.getAttribute("operator"),
                    Integer.parseInt(t.getAttribute("threshold")),
                    t.getAttribute("event-id")
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse cross-NPC triggers XML", e);
        }
        return triggers;
    }

    private String getTextContent(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return "";
    }
}
