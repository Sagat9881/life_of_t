package ru.lifegame.backend.domain.npc;

import ru.lifegame.backend.domain.npc.spec.ScheduleSlot;
import ru.lifegame.backend.domain.npc.spec.ScoredAction;
import ru.lifegame.backend.domain.npc.spec.ConditionSpec;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.*;

/**
 * Loads NPC specifications from XML files in narrative/npc-behavior/.
 * This is the only infrastructure class that reads NPC content —
 * the domain engine works entirely with NpcSpec abstractions.
 */
public class NpcSpecLoader {

    private static final String NPC_SPEC_PATTERN = "classpath:narrative/npc-behavior/*.xml";

    public List<NpcSpec> loadAll() {
        List<NpcSpec> specs = new ArrayList<>();
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(NPC_SPEC_PATTERN);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            for (Resource resource : resources) {
                try (InputStream is = resource.getInputStream()) {
                    Document doc = builder.parse(is);
                    doc.getDocumentElement().normalize();
                    specs.add(parseNpc(doc.getDocumentElement()));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load NPC specs from XML", e);
        }
        return specs;
    }

    private NpcSpec parseNpc(Element root) {
        String id = root.getAttribute("id");
        String type = root.getAttribute("type");
        String category = getAttributeOrDefault(root, "category", "human");
        String displayName = getTextContent(root, "display-name", id);

        Map<String, Integer> personality = new LinkedHashMap<>();
        NodeList traits = root.getElementsByTagName("trait");
        for (int i = 0; i < traits.getLength(); i++) {
            Element trait = (Element) traits.item(i);
            if (trait.getParentNode().getNodeName().equals("personality")) {
                personality.put(trait.getAttribute("name"),
                        Integer.parseInt(trait.getAttribute("value")));
            }
        }

        Map<String, Integer> moodInitial = new LinkedHashMap<>();
        NodeList moodNodes = root.getElementsByTagName("mood-initial");
        if (moodNodes.getLength() > 0) {
            Element moodEl = (Element) moodNodes.item(0);
            NamedNodeMap attrs = moodEl.getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                Attr attr = (Attr) attrs.item(i);
                moodInitial.put(attr.getName(), Integer.parseInt(attr.getValue()));
            }
        }

        boolean memoryEnabled = true;
        int shortTermSize = 10;
        NodeList memNodes = root.getElementsByTagName("memory");
        if (memNodes.getLength() > 0) {
            Element memEl = (Element) memNodes.item(0);
            memoryEnabled = Boolean.parseBoolean(memEl.getAttribute("enabled"));
            String sts = memEl.getAttribute("short-term-size");
            if (!sts.isEmpty()) shortTermSize = Integer.parseInt(sts);
        }

        List<ScheduleSlot> scheduleSlots = new ArrayList<>();
        NodeList slots = root.getElementsByTagName("slot");
        for (int i = 0; i < slots.getLength(); i++) {
            Element slot = (Element) slots.item(i);
            if (slot.getParentNode().getNodeName().equals("schedule")) {
                scheduleSlots.add(new ScheduleSlot(
                        Integer.parseInt(slot.getAttribute("start")),
                        Integer.parseInt(slot.getAttribute("end")),
                        slot.getAttribute("activity"),
                        slot.getAttribute("location"),
                        getAttributeOrDefault(slot, "animation", slot.getAttribute("activity"))
                ));
            }
        }

        List<ScoredAction> actions = new ArrayList<>();
        NodeList actionNodes = root.getElementsByTagName("action");
        for (int i = 0; i < actionNodes.getLength(); i++) {
            Element actionEl = (Element) actionNodes.item(i);
            if (!actionEl.getParentNode().getNodeName().equals("actions")) continue;

            String actionId = actionEl.getAttribute("id");
            double baseScore = Double.parseDouble(
                    getAttributeOrDefault(actionEl, "base-score", "0.5"));
            String eventType = getAttributeOrDefault(actionEl, "event-type", "NPC_INITIATED");

            List<ConditionSpec> conditions = parseConditions(actionEl);

            List<ScoredAction.ActionOption> options = new ArrayList<>();
            NodeList optionNodes = actionEl.getElementsByTagName("option");
            for (int j = 0; j < optionNodes.getLength(); j++) {
                Element optEl = (Element) optionNodes.item(j);
                int energyDelta = parseIntAttr(optEl, "energy", 0);
                int stressDelta = parseIntAttr(optEl, "stress", 0);
                int moodDelta = parseIntAttr(optEl, "mood", 0);
                int moneyDelta = parseIntAttr(optEl, "money", 0);
                String relTarget = optEl.getAttribute("relationship-target");
                int relDelta = parseIntAttr(optEl, "relationship-delta", 0);

                options.add(new ScoredAction.ActionOption(
                        optEl.getAttribute("id"),
                        optEl.getAttribute("text"),
                        optEl.getAttribute("result"),
                        energyDelta, stressDelta, moodDelta, moneyDelta,
                        relTarget, relDelta
                ));
            }

            actions.add(new ScoredAction(actionId, baseScore, eventType, conditions, options));
        }

        List<String> questLines = new ArrayList<>();
        NodeList qlNodes = root.getElementsByTagName("quest-lines");
        if (qlNodes.getLength() > 0) {
            String raw = qlNodes.item(0).getTextContent().trim();
            if (!raw.isEmpty()) {
                questLines = Arrays.stream(raw.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
            }
        }

        return new NpcSpec(
                id, type, category, displayName,
                Map.copyOf(personality), Map.copyOf(moodInitial),
                memoryEnabled, shortTermSize,
                List.copyOf(scheduleSlots),
                List.copyOf(actions),
                List.copyOf(questLines)
        );
    }

    private List<ConditionSpec> parseConditions(Element parent) {
        List<ConditionSpec> conditions = new ArrayList<>();
        NodeList condNodes = parent.getElementsByTagName("condition");
        for (int i = 0; i < condNodes.getLength(); i++) {
            Element condEl = (Element) condNodes.item(i);
            if (!condEl.getParentNode().getNodeName().equals("conditions")) continue;
            conditions.add(new ConditionSpec(
                    condEl.getAttribute("type"),
                    getAttributeOrDefault(condEl, "axis",
                            getAttributeOrDefault(condEl, "target", "")),
                    getAttributeOrDefault(condEl, "operator", "gte"),
                    getAttributeOrDefault(condEl, "value", "0"),
                    getAttributeOrDefault(condEl, "check", ""),
                    getAttributeOrDefault(condEl, "npc", ""),
                    getAttributeOrDefault(condEl, "action", ""),
                    getAttributeOrDefault(condEl, "min-count", "0")
            ));
        }
        return conditions;
    }

    private String getTextContent(Element root, String tagName, String defaultValue) {
        NodeList nodes = root.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return defaultValue;
    }

    private String getAttributeOrDefault(Element el, String attr, String defaultValue) {
        String val = el.getAttribute(attr);
        return (val != null && !val.isEmpty()) ? val : defaultValue;
    }

    private int parseIntAttr(Element el, String attr, int defaultValue) {
        String val = el.getAttribute(attr);
        if (val == null || val.isEmpty()) return defaultValue;
        try { return Integer.parseInt(val); }
        catch (NumberFormatException e) { return defaultValue; }
    }
}
