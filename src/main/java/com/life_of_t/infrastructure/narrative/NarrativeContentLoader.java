package com.life_of_t.infrastructure.narrative;

import com.life_of_t.domain.npc.spec.NpcSpec;
import com.life_of_t.domain.npc.spec.ScheduleSlot;
import com.life_of_t.domain.npc.spec.ScoredAction;
import com.life_of_t.domain.npc.spec.ConditionSpec;
import com.life_of_t.domain.npc.graph.CrossNpcConditionSpec;
import com.life_of_t.domain.npc.graph.NpcRelationshipEdge;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.*;

/**
 * Loads all narrative content from XML specifications.
 * Scans classpath: narrative/npc-behavior/*.xml
 * 
 * The engine knows ZERO concrete NPC names, actions, or events.
 * Everything is data-driven from XML.
 */
public class NarrativeContentLoader {

    private final List<NpcSpec> npcSpecs = new ArrayList<>();
    private final List<NpcRelationshipEdge> relationshipEdges = new ArrayList<>();
    private final List<CrossNpcConditionSpec> crossNpcConditions = new ArrayList<>();

    /**
     * Load all NPC specs from XML files on classpath.
     * Call once at application startup.
     */
    public void loadFromClasspath(String... xmlPaths) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        for (String path : xmlPaths) {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
                if (is == null) continue;
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(is);
                doc.getDocumentElement().normalize();

                String rootTag = doc.getDocumentElement().getTagName();
                if ("npc".equals(rootTag)) {
                    npcSpecs.add(parseNpcSpec(doc.getDocumentElement()));
                } else if ("cross-npc-relations".equals(rootTag)) {
                    parseCrossNpcRelations(doc.getDocumentElement());
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to load narrative XML: " + path, e);
            }
        }
    }

    /**
     * Scan a directory pattern for XML files.
     * In Spring context, use ResourcePatternResolver to find all matching files.
     */
    public void loadNpcSpec(InputStream xmlStream) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlStream);
            doc.getDocumentElement().normalize();
            npcSpecs.add(parseNpcSpec(doc.getDocumentElement()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse NPC spec XML", e);
        }
    }

    private NpcSpec parseNpcSpec(Element root) {
        String id = root.getAttribute("id");
        String type = root.getAttribute("type"); // "named" or "filler"
        String category = root.getAttribute("category"); // "human", "cat", "dog"

        String displayName = getTextContent(root, "display-name", id);

        // Personality traits
        Map<String, Integer> personality = new LinkedHashMap<>();
        NodeList traitNodes = root.getElementsByTagName("trait");
        for (int i = 0; i < traitNodes.getLength(); i++) {
            Element trait = (Element) traitNodes.item(i);
            // Only process direct children of <personality>
            if (trait.getParentNode() instanceof Element parent 
                    && "personality".equals(parent.getTagName())) {
                personality.put(
                        trait.getAttribute("name"),
                        Integer.parseInt(trait.getAttribute("value"))
                );
            }
        }

        // Initial mood (6 axes)
        Map<String, Integer> moodInitial = new LinkedHashMap<>();
        NodeList moodNodes = root.getElementsByTagName("mood-initial");
        if (moodNodes.getLength() > 0) {
            Element moodEl = (Element) moodNodes.item(0);
            for (String axis : List.of("happiness", "anxiety", "loneliness", 
                                        "irritability", "energy", "affection")) {
                String val = moodEl.getAttribute(axis);
                moodInitial.put(axis, val.isEmpty() ? 50 : Integer.parseInt(val));
            }
        }

        // Memory config
        boolean memoryEnabled = false;
        int shortTermSize = 10;
        NodeList memoryNodes = root.getElementsByTagName("memory");
        if (memoryNodes.getLength() > 0) {
            Element memEl = (Element) memoryNodes.item(0);
            memoryEnabled = "true".equals(memEl.getAttribute("enabled"));
            String sts = memEl.getAttribute("short-term-size");
            if (!sts.isEmpty()) shortTermSize = Integer.parseInt(sts);
        }

        // Schedule slots
        List<ScheduleSlot> scheduleSlots = new ArrayList<>();
        NodeList slotNodes = root.getElementsByTagName("slot");
        for (int i = 0; i < slotNodes.getLength(); i++) {
            Element slot = (Element) slotNodes.item(i);
            if (slot.getParentNode() instanceof Element parent 
                    && "schedule".equals(parent.getTagName())) {
                scheduleSlots.add(new ScheduleSlot(
                        Integer.parseInt(slot.getAttribute("start")),
                        Integer.parseInt(slot.getAttribute("end")),
                        slot.getAttribute("activity"),
                        slot.getAttribute("location"),
                        slot.getAttribute("animation")
                ));
            }
        }

        // Actions with scoring
        List<ScoredAction> actions = new ArrayList<>();
        NodeList actionNodes = root.getElementsByTagName("action");
        for (int i = 0; i < actionNodes.getLength(); i++) {
            Element actionEl = (Element) actionNodes.item(i);
            if (actionEl.getParentNode() instanceof Element parent 
                    && "actions".equals(parent.getTagName())) {
                actions.add(parseScoredAction(actionEl));
            }
        }

        return new NpcSpec(
                id, type, category, displayName,
                personality, moodInitial,
                memoryEnabled, shortTermSize,
                scheduleSlots, actions
        );
    }

    private ScoredAction parseScoredAction(Element actionEl) {
        String actionId = actionEl.getAttribute("id");
        double baseScore = parseDouble(actionEl.getAttribute("base-score"), 0.5);
        String eventType = actionEl.getAttribute("event-type");

        // Parse conditions
        List<ConditionSpec> conditions = new ArrayList<>();
        NodeList condNodes = actionEl.getElementsByTagName("condition");
        for (int i = 0; i < condNodes.getLength(); i++) {
            Element condEl = (Element) condNodes.item(i);
            if (condEl.getParentNode() instanceof Element parent 
                    && "conditions".equals(parent.getTagName())) {
                conditions.add(new ConditionSpec(
                        condEl.getAttribute("type"),
                        condEl.getAttribute("axis").isEmpty() 
                                ? condEl.getAttribute("check") 
                                : condEl.getAttribute("axis"),
                        condEl.getAttribute("operator"),
                        condEl.getAttribute("value")
                ));
            }
        }

        // Parse options (for NPC-initiated events)
        List<ScoredAction.ActionOption> options = new ArrayList<>();
        NodeList optNodes = actionEl.getElementsByTagName("option");
        for (int i = 0; i < optNodes.getLength(); i++) {
            Element optEl = (Element) optNodes.item(i);
            options.add(new ScoredAction.ActionOption(
                    optEl.getAttribute("id"),
                    optEl.getAttribute("text"),
                    optEl.getAttribute("result"),
                    parseIntAttr(optEl, "energy", 0),
                    parseIntAttr(optEl, "stress", 0),
                    parseIntAttr(optEl, "mood", 0),
                    parseIntAttr(optEl, "money", 0),
                    optEl.getAttribute("relationship-target"),
                    parseIntAttr(optEl, "relationship-delta", 0)
            ));
        }

        return new ScoredAction(actionId, baseScore, eventType, conditions, options);
    }

    private void parseCrossNpcRelations(Element root) {
        // Parse edges
        NodeList edgeNodes = root.getElementsByTagName("edge");
        for (int i = 0; i < edgeNodes.getLength(); i++) {
            Element el = (Element) edgeNodes.item(i);
            relationshipEdges.add(new NpcRelationshipEdge(
                    el.getAttribute("npc-a"),
                    el.getAttribute("npc-b"),
                    parseIntAttr(el, "respect", 50),
                    parseIntAttr(el, "tension", 0),
                    parseIntAttr(el, "familiarity", 50)
            ));
        }

        // Parse cross conditions
        NodeList condNodes = root.getElementsByTagName("cross-condition");
        for (int i = 0; i < condNodes.getLength(); i++) {
            Element el = (Element) condNodes.item(i);
            crossNpcConditions.add(new CrossNpcConditionSpec(
                    el.getAttribute("npc-a"),
                    el.getAttribute("npc-b"),
                    el.getAttribute("axis"),
                    el.getAttribute("operator"),
                    parseIntAttr(el, "value", 0),
                    el.getAttribute("result-event")
            ));
        }
    }

    // --- Utility methods ---

    private String getTextContent(Element parent, String tagName, String defaultVal) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) return nodes.item(0).getTextContent().trim();
        return defaultVal;
    }

    private int parseIntAttr(Element el, String attr, int defaultVal) {
        String val = el.getAttribute(attr);
        if (val == null || val.isEmpty()) return defaultVal;
        try { return Integer.parseInt(val); }
        catch (NumberFormatException e) { return defaultVal; }
    }

    private double parseDouble(String val, double defaultVal) {
        if (val == null || val.isEmpty()) return defaultVal;
        try { return Double.parseDouble(val); }
        catch (NumberFormatException e) { return defaultVal; }
    }

    // --- Accessors ---

    public List<NpcSpec> getNpcSpecs() {
        return Collections.unmodifiableList(npcSpecs);
    }

    public List<NpcRelationshipEdge> getRelationshipEdges() {
        return Collections.unmodifiableList(relationshipEdges);
    }

    public List<CrossNpcConditionSpec> getCrossNpcConditions() {
        return Collections.unmodifiableList(crossNpcConditions);
    }
}
