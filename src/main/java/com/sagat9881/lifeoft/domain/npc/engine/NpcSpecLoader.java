package com.sagat9881.lifeoft.domain.npc.engine;

import com.sagat9881.lifeoft.domain.npc.model.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.*;
import java.io.InputStream;
import java.util.*;

/**
 * Loads NPC specifications from XML files in narrative/npc-behavior/.
 * 
 * This is the bridge between narrative content (XML) and the engine (Java).
 * The engine never knows specific NPC names — everything is data.
 */
public class NpcSpecLoader {

    /**
     * Parse a single NPC spec from an XML InputStream.
     */
    public NpcSpec load(InputStream xmlStream) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(xmlStream);
            Element root = doc.getDocumentElement();

            String id = root.getAttribute("id");
            String type = root.getAttribute("type");
            String category = attr(root, "category", "human");

            String displayName = textContent(root, "display-name", id);
            String defaultLocation = textContent(root, "default-location", "living_room");

            // Parse personality traits
            Map<String, Double> personality = new LinkedHashMap<>();
            NodeList traits = root.getElementsByTagName("trait");
            for (int i = 0; i < traits.getLength(); i++) {
                Element trait = (Element) traits.item(i);
                if (trait.getParentNode().getNodeName().equals("personality")) {
                    personality.put(
                            trait.getAttribute("name"),
                            Double.parseDouble(trait.getAttribute("value"))
                    );
                }
            }

            // Parse mood initial values
            Map<String, Double> moodInitial = new LinkedHashMap<>();
            NodeList moodNodes = root.getElementsByTagName("mood-initial");
            if (moodNodes.getLength() > 0) {
                Element moodEl = (Element) moodNodes.item(0);
                for (String axis : List.of("happiness", "anxiety", "loneliness",
                        "irritability", "energy", "affection")) {
                    String val = moodEl.getAttribute(axis);
                    if (!val.isEmpty()) {
                        moodInitial.put(axis, Double.parseDouble(val));
                    }
                }
            }

            // Parse mood decay rates
            Map<String, Double> moodDecayRates = new LinkedHashMap<>();
            NodeList decayNodes = root.getElementsByTagName("mood-decay");
            if (decayNodes.getLength() > 0) {
                Element decayEl = (Element) decayNodes.item(0);
                NamedNodeMap attrs = decayEl.getAttributes();
                for (int i = 0; i < attrs.getLength(); i++) {
                    Attr a = (Attr) attrs.item(i);
                    moodDecayRates.put(a.getName(), Double.parseDouble(a.getValue()));
                }
            }

            // Parse memory config
            boolean memoryEnabled = false;
            int shortTermSize = 10;
            NodeList memoryNodes = root.getElementsByTagName("memory");
            if (memoryNodes.getLength() > 0) {
                Element memEl = (Element) memoryNodes.item(0);
                memoryEnabled = "true".equals(memEl.getAttribute("enabled"));
                String sts = memEl.getAttribute("short-term-size");
                if (!sts.isEmpty()) shortTermSize = Integer.parseInt(sts);
            }

            // Parse schedule slots
            List<ScheduleSlot> scheduleSlots = new ArrayList<>();
            NodeList slotNodes = root.getElementsByTagName("slot");
            for (int i = 0; i < slotNodes.getLength(); i++) {
                Element slot = (Element) slotNodes.item(i);
                if (slot.getParentNode().getNodeName().equals("schedule")) {
                    scheduleSlots.add(new ScheduleSlot(
                            Integer.parseInt(slot.getAttribute("start")),
                            Integer.parseInt(slot.getAttribute("end")),
                            slot.getAttribute("activity"),
                            slot.getAttribute("location"),
                            attr(slot, "animation", "idle")
                    ));
                }
            }

            // Parse scored actions
            List<ScoredAction> scoredActions = new ArrayList<>();
            NodeList actionNodes = root.getElementsByTagName("action");
            for (int i = 0; i < actionNodes.getLength(); i++) {
                Element actionEl = (Element) actionNodes.item(i);
                if (actionEl.getParentNode().getNodeName().equals("actions")) {
                    scoredActions.add(parseScoredAction(actionEl));
                }
            }

            // Parse action reactions
            List<NpcSpec.ActionReaction> reactions = new ArrayList<>();
            NodeList reactionNodes = root.getElementsByTagName("reaction");
            for (int i = 0; i < reactionNodes.getLength(); i++) {
                Element rEl = (Element) reactionNodes.item(i);
                Map<String, Double> moodChanges = new LinkedHashMap<>();
                NodeList moodChangeNodes = rEl.getElementsByTagName("mood-change");
                for (int j = 0; j < moodChangeNodes.getLength(); j++) {
                    Element mc = (Element) moodChangeNodes.item(j);
                    moodChanges.put(mc.getAttribute("axis"), Double.parseDouble(mc.getAttribute("delta")));
                }
                reactions.add(new NpcSpec.ActionReaction(
                        rEl.getAttribute("trigger-action"),
                        moodChanges
                ));
            }

            return new NpcSpec(
                    id, type, category, displayName, defaultLocation,
                    personality, moodInitial, moodDecayRates,
                    memoryEnabled, shortTermSize,
                    scheduleSlots, scoredActions, reactions
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse NPC spec XML", e);
        }
    }

    private ScoredAction parseScoredAction(Element actionEl) {
        String actionId = actionEl.getAttribute("id");
        double baseScore = Double.parseDouble(attr(actionEl, "base-score", "0.5"));
        String animationKey = attr(actionEl, "animation", "idle");
        String locationId = attr(actionEl, "location", "");
        String eventType = attr(actionEl, "event-type", "");
        boolean isEventInitiator = "true".equals(attr(actionEl, "initiates-event", "false"))
                || !eventType.isEmpty();

        // Parse conditions
        List<ConditionSpec> conditions = new ArrayList<>();
        NodeList condNodes = actionEl.getElementsByTagName("condition");
        for (int i = 0; i < condNodes.getLength(); i++) {
            Element cEl = (Element) condNodes.item(i);
            conditions.add(new ConditionSpec(
                    cEl.getAttribute("type"),
                    attr(cEl, "axis", ""),
                    attr(cEl, "target", ""),
                    attr(cEl, "operator", "gte"),
                    attr(cEl, "value", "0"),
                    attr(cEl, "check", "")
            ));
        }

        // Parse mood weights
        Map<String, Double> moodWeights = new LinkedHashMap<>();
        NodeList mwNodes = actionEl.getElementsByTagName("mood-weight");
        for (int i = 0; i < mwNodes.getLength(); i++) {
            Element mw = (Element) mwNodes.item(i);
            moodWeights.put(mw.getAttribute("axis"), Double.parseDouble(mw.getAttribute("weight")));
        }

        // Parse personality weights
        Map<String, Double> personalityWeights = new LinkedHashMap<>();
        NodeList pwNodes = actionEl.getElementsByTagName("personality-weight");
        for (int i = 0; i < pwNodes.getLength(); i++) {
            Element pw = (Element) pwNodes.item(i);
            personalityWeights.put(pw.getAttribute("trait"), Double.parseDouble(pw.getAttribute("weight")));
        }

        // Parse options
        List<Map<String, String>> options = new ArrayList<>();
        NodeList optNodes = actionEl.getElementsByTagName("option");
        for (int i = 0; i < optNodes.getLength(); i++) {
            Element opt = (Element) optNodes.item(i);
            Map<String, String> optMap = new LinkedHashMap<>();
            NamedNodeMap attrs = opt.getAttributes();
            for (int j = 0; j < attrs.getLength(); j++) {
                Attr a = (Attr) attrs.item(j);
                optMap.put(a.getName(), a.getValue());
            }
            options.add(optMap);
        }

        return new ScoredAction(
                actionId, baseScore, animationKey, locationId,
                eventType, isEventInitiator,
                conditions, moodWeights, personalityWeights, options
        );
    }

    private String attr(Element el, String name, String defaultValue) {
        String val = el.getAttribute(name);
        return val.isEmpty() ? defaultValue : val;
    }

    private String textContent(Element parent, String tagName, String defaultValue) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return defaultValue;
    }
}
