package ru.lifegame.backend.domain.npc;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Loads NPC specifications from XML files in narrative/npc-behavior/ directory.
 * Called at application startup to build the master NPC spec list.
 * Supports both named and filler NPC types.
 */
public class NpcSpecLoader {

    /**
     * Load all NPC specs from a directory.
     */
    public List<NpcSpec> loadFromDirectory(Path directory) {
        List<NpcSpec> specs = new ArrayList<>();
        try (Stream<Path> files = Files.list(directory)) {
            files.filter(p -> p.toString().endsWith(".xml"))
                .forEach(p -> {
                    try {
                        specs.add(parseFile(p));
                    } catch (Exception e) {
                        System.err.println("Failed to parse NPC spec: " + p + " — " + e.getMessage());
                    }
                });
        } catch (IOException e) {
            throw new RuntimeException("Cannot read NPC spec directory: " + directory, e);
        }
        return specs;
    }

    /**
     * Load a single NPC spec from an InputStream (for classpath resources).
     */
    public NpcSpec loadFromStream(InputStream is) {
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(is);
            return parseDocument(doc);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse NPC spec from stream", e);
        }
    }

    private NpcSpec parseFile(Path path) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().parse(path.toFile());
        return parseDocument(doc);
    }

    private NpcSpec parseDocument(Document doc) {
        Element root = doc.getDocumentElement();
        String id = root.getAttribute("id");
        String type = root.getAttribute("type");
        String category = attr(root, "category", "human");

        String displayName = textContent(root, "display-name", id);

        Map<String, Integer> personality = parsePersonality(root);
        NpcSpec.MoodInitial moodInitial = parseMoodInitial(root);

        Element memoryEl = firstChild(root, "memory");
        boolean memoryEnabled = memoryEl != null && "true".equals(memoryEl.getAttribute("enabled"));
        int memorySize = memoryEl != null ? intAttr(memoryEl, "short-term-size", 10) : 10;

        List<NpcSpec.ScheduleSlot> slots = parseSchedule(root);
        List<ScoredAction> actions = parseActions(root);
        List<String> questLines = parseQuestLines(root);

        return new NpcSpec(id, type, category, displayName, personality,
            moodInitial, memoryEnabled, memorySize, slots, actions, questLines);
    }

    private Map<String, Integer> parsePersonality(Element root) {
        Map<String, Integer> traits = new LinkedHashMap<>();
        Element el = firstChild(root, "personality");
        if (el == null) return traits;
        NodeList nodes = el.getElementsByTagName("trait");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element t = (Element) nodes.item(i);
            traits.put(t.getAttribute("name"), Integer.parseInt(t.getAttribute("value")));
        }
        return traits;
    }

    private NpcSpec.MoodInitial parseMoodInitial(Element root) {
        Element el = firstChild(root, "mood-initial");
        if (el == null) return NpcSpec.MoodInitial.defaults();
        return new NpcSpec.MoodInitial(
            intAttr(el, "happiness", 50),
            intAttr(el, "anxiety", 20),
            intAttr(el, "loneliness", 30),
            intAttr(el, "irritability", 10),
            intAttr(el, "energy", 70),
            intAttr(el, "affection", 50)
        );
    }

    private List<NpcSpec.ScheduleSlot> parseSchedule(Element root) {
        List<NpcSpec.ScheduleSlot> slots = new ArrayList<>();
        Element el = firstChild(root, "schedule");
        if (el == null) return slots;
        NodeList nodes = el.getElementsByTagName("slot");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element s = (Element) nodes.item(i);
            slots.add(new NpcSpec.ScheduleSlot(
                intAttr(s, "start", 0),
                intAttr(s, "end", 24),
                attr(s, "activity", "idle"),
                attr(s, "location", "unknown"),
                attr(s, "animation", "idle")
            ));
        }
        return slots;
    }

    private List<ScoredAction> parseActions(Element root) {
        List<ScoredAction> actions = new ArrayList<>();
        Element actionsEl = firstChild(root, "actions");
        if (actionsEl == null) return actions;

        NodeList nodes = actionsEl.getElementsByTagName("action");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element a = (Element) nodes.item(i);
            String actionId = a.getAttribute("id");
            double baseScore = doubleAttr(a, "base-score", 0.5);
            String eventType = attr(a, "event-type", "RANDOM_ENCOUNTER");
            String title = attr(a, "title", actionId);
            String description = attr(a, "description", "");

            List<ConditionSpec> conditions = parseConditions(a);
            List<ScoredAction.ActionOption> options = parseOptions(a);

            actions.add(new ScoredAction(actionId, title, description,
                eventType, baseScore, conditions, options));
        }
        return actions;
    }

    private List<ConditionSpec> parseConditions(Element actionEl) {
        List<ConditionSpec> conditions = new ArrayList<>();
        Element condEl = firstChild(actionEl, "conditions");
        if (condEl == null) return conditions;
        NodeList nodes = condEl.getElementsByTagName("condition");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element c = (Element) nodes.item(i);
            conditions.add(new ConditionSpec(
                c.getAttribute("type"),
                attr(c, "target", attr(c, "axis", attr(c, "check", ""))),
                attr(c, "operator", "gte"),
                intAttr(c, "value", 0)
            ));
        }
        return conditions;
    }

    private List<ScoredAction.ActionOption> parseOptions(Element actionEl) {
        List<ScoredAction.ActionOption> options = new ArrayList<>();
        Element optsEl = firstChild(actionEl, "options");
        if (optsEl == null) return options;
        NodeList nodes = optsEl.getElementsByTagName("option");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element o = (Element) nodes.item(i);
            Map<String, Integer> relDeltas = new LinkedHashMap<>();
            String relTarget = attr(o, "relationship-target", null);
            if (relTarget != null) {
                relDeltas.put(relTarget, intAttr(o, "relationship-delta", 0));
            }
            Map<String, Integer> skillDeltas = new LinkedHashMap<>();
            String skillTarget = attr(o, "skill-target", null);
            if (skillTarget != null) {
                skillDeltas.put(skillTarget, intAttr(o, "skill-delta", 0));
            }
            options.add(new ScoredAction.ActionOption(
                o.getAttribute("id"),
                attr(o, "text", ""),
                attr(o, "result", ""),
                intAttr(o, "energy", 0),
                intAttr(o, "stress", 0),
                intAttr(o, "mood", 0),
                intAttr(o, "money", 0),
                intAttr(o, "self-esteem", 0),
                intAttr(o, "job-satisfaction", 0),
                relDeltas,
                skillDeltas
            ));
        }
        return options;
    }

    private List<String> parseQuestLines(Element root) {
        Element el = firstChild(root, "quest-lines");
        if (el == null) return List.of();
        String text = el.getTextContent().trim();
        if (text.isEmpty()) return List.of();
        return Arrays.stream(text.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
    }

    // === XML utility helpers ===

    private Element firstChild(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        return nodes.getLength() > 0 ? (Element) nodes.item(0) : null;
    }

    private String textContent(Element parent, String tagName, String defaultVal) {
        Element el = firstChild(parent, tagName);
        return el != null ? el.getTextContent().trim() : defaultVal;
    }

    private String attr(Element el, String name, String defaultVal) {
        String val = el.getAttribute(name);
        return (val != null && !val.isEmpty()) ? val : defaultVal;
    }

    private int intAttr(Element el, String name, int defaultVal) {
        String val = el.getAttribute(name);
        if (val == null || val.isEmpty()) return defaultVal;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private double doubleAttr(Element el, String name, double defaultVal) {
        String val = el.getAttribute(name);
        if (val == null || val.isEmpty()) return defaultVal;
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}
