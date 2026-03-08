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
import java.nio.file.*;
import java.io.IOException;

/**
 * Loads all narrative content from XML specifications.
 * Scans narrative/npc-behavior/*.xml at startup.
 * Backend knows ZERO concrete NPC names — everything comes from XML.
 */
@Component
public class NarrativeContentLoader {

    private final List<NpcSpec> npcSpecs = new ArrayList<>();
    private final List<NpcRelationshipEdge> relationshipEdges = new ArrayList<>();

    public void loadFromClasspath(String basePath) {
        try {
            Path npcDir = Paths.get(basePath, "npc-behavior");
            if (!Files.exists(npcDir)) return;

            try (var stream = Files.list(npcDir)) {
                stream.filter(p -> p.toString().endsWith(".xml"))
                      .forEach(this::parseNpcFile);
            }

            Path relPath = Paths.get(basePath, "relationships.xml");
            if (Files.exists(relPath)) {
                parseRelationshipsFile(relPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load narrative content from: " + basePath, e);
        }
    }

    private void parseNpcFile(Path path) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(Files.newInputStream(path));
            Element root = doc.getDocumentElement();

            String id = root.getAttribute("id");
            String type = root.getAttribute("type");
            String category = root.getAttribute("category");
            String displayName = getTextContent(root, "display-name", id);

            Map<String, Integer> personality = parseTraits(root, "personality");
            Map<String, Integer> moodInitial = parseMoodInitial(root);
            boolean memoryEnabled = parseMemoryEnabled(root);
            int shortTermSize = parseShortTermSize(root);
            List<ScheduleSlot> schedule = parseSchedule(root);
            List<ScoredAction> actions = parseActions(root);
            List<String> questLines = parseQuestLines(root);

            NpcSpec spec = new NpcSpec(
                id, type, category, displayName,
                personality, moodInitial, memoryEnabled, shortTermSize,
                schedule, actions, questLines
            );
            npcSpecs.add(spec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse NPC file: " + path, e);
        }
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
        if (nodes.getLength() == 0) return mood;
        Element el = (Element) nodes.item(0);
        String[] axes = {"happiness", "anxiety", "loneliness", "irritability", "energy", "affection"};
        for (String axis : axes) {
            String val = el.getAttribute(axis);
            if (!val.isEmpty()) mood.put(axis, Integer.parseInt(val));
        }
        return mood;
    }

    private boolean parseMemoryEnabled(Element root) {
        NodeList nodes = root.getElementsByTagName("memory");
        if (nodes.getLength() == 0) return false;
        return Boolean.parseBoolean(((Element) nodes.item(0)).getAttribute("enabled"));
    }

    private int parseShortTermSize(Element root) {
        NodeList nodes = root.getElementsByTagName("memory");
        if (nodes.getLength() == 0) return 5;
        String size = ((Element) nodes.item(0)).getAttribute("short-term-size");
        return size.isEmpty() ? 5 : Integer.parseInt(size);
    }

    private List<ScheduleSlot> parseSchedule(Element root) {
        List<ScheduleSlot> slots = new ArrayList<>();
        NodeList parents = root.getElementsByTagName("schedule");
        if (parents.getLength() == 0) return slots;
        Element parent = (Element) parents.item(0);
        NodeList slotNodes = parent.getElementsByTagName("slot");
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
        NodeList parents = root.getElementsByTagName("actions");
        if (parents.getLength() == 0) return actions;
        Element parent = (Element) parents.item(0);
        NodeList actionNodes = parent.getElementsByTagName("action");
        for (int i = 0; i < actionNodes.getLength(); i++) {
            Element a = (Element) actionNodes.item(i);
            String actionId = a.getAttribute("id");
            double baseScore = Double.parseDouble(a.getAttribute("base-score"));
            String eventType = a.getAttribute("event-type");

            List<ConditionSpec> conditions = new ArrayList<>();
            NodeList condNodes = a.getElementsByTagName("condition");
            for (int j = 0; j < condNodes.getLength(); j++) {
                Element c = (Element) condNodes.item(j);
                conditions.add(new ConditionSpec(
                    c.getAttribute("type"),
                    c.getAttribute("axis").isEmpty() ? c.getAttribute("check") : c.getAttribute("axis"),
                    c.getAttribute("operator"),
                    c.getAttribute("value")
                ));
            }

            List<Map<String, String>> options = new ArrayList<>();
            NodeList optNodes = a.getElementsByTagName("option");
            for (int j = 0; j < optNodes.getLength(); j++) {
                Element o = (Element) optNodes.item(j);
                Map<String, String> opt = new LinkedHashMap<>();
                var attrs = o.getAttributes();
                for (int k = 0; k < attrs.getLength(); k++) {
                    opt.put(attrs.item(k).getNodeName(), attrs.item(k).getNodeValue());
                }
                options.add(opt);
            }

            actions.add(new ScoredAction(actionId, baseScore, eventType, conditions, options));
        }
        return actions;
    }

    private List<String> parseQuestLines(Element root) {
        NodeList nodes = root.getElementsByTagName("quest-lines");
        if (nodes.getLength() == 0) return List.of();
        String text = nodes.item(0).getTextContent().trim();
        if (text.isEmpty()) return List.of();
        return Arrays.asList(text.split("\\s*,\\s*"));
    }

    private void parseRelationshipsFile(Path path) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(Files.newInputStream(path));
            NodeList edgeNodes = doc.getElementsByTagName("edge");
            for (int i = 0; i < edgeNodes.getLength(); i++) {
                Element e = (Element) edgeNodes.item(i);
                relationshipEdges.add(new NpcRelationshipEdge(
                    e.getAttribute("from"),
                    e.getAttribute("to"),
                    Integer.parseInt(e.getAttribute("respect")),
                    Integer.parseInt(e.getAttribute("tension")),
                    Integer.parseInt(e.getAttribute("familiarity"))
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse relationships: " + path, e);
        }
    }

    private String getTextContent(Element root, String tagName, String defaultValue) {
        NodeList nodes = root.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) return defaultValue;
        return nodes.item(0).getTextContent().trim();
    }

    public List<NpcSpec> getNpcSpecs() { return Collections.unmodifiableList(npcSpecs); }
    public List<NpcRelationshipEdge> getRelationshipEdges() { return Collections.unmodifiableList(relationshipEdges); }
}
