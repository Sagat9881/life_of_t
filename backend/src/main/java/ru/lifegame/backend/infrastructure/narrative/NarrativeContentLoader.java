package ru.lifegame.backend.infrastructure.narrative;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import ru.lifegame.backend.domain.npc.spec.NpcSpec;
import ru.lifegame.backend.domain.npc.spec.ScheduleSlot;
import ru.lifegame.backend.domain.npc.spec.ScoredAction;
import ru.lifegame.backend.domain.npc.spec.ConditionSpec;
import ru.lifegame.backend.domain.npc.graph.CrossNpcConditionSpec;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import java.io.InputStream;
import java.util.*;

public class NarrativeContentLoader {

    private final String npcBehaviorPattern;

    public NarrativeContentLoader(String npcBehaviorPattern) {
        this.npcBehaviorPattern = npcBehaviorPattern;
    }

    public NarrativeContentLoader() {
        this("classpath:narrative/npc-behavior/*.xml");
    }

    public List<NpcSpec> loadAllNpcSpecs() {
        List<NpcSpec> specs = new ArrayList<>();
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(npcBehaviorPattern);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            for (Resource resource : resources) {
                try (InputStream is = resource.getInputStream()) {
                    Document doc = builder.parse(is);
                    doc.getDocumentElement().normalize();
                    NpcSpec spec = parseNpcElement(doc.getDocumentElement());
                    if (spec != null) {
                        specs.add(spec);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load NPC specs from " + npcBehaviorPattern, e);
        }
        return Collections.unmodifiableList(specs);
    }

    public List<CrossNpcConditionSpec> loadCrossNpcConditions() {
        List<CrossNpcConditionSpec> conditions = new ArrayList<>();
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:narrative/cross-npc/*.xml");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            for (Resource resource : resources) {
                try (InputStream is = resource.getInputStream()) {
                    Document doc = builder.parse(is);
                    NodeList triggerNodes = doc.getElementsByTagName("trigger");
                    for (int i = 0; i < triggerNodes.getLength(); i++) {
                        Element el = (Element) triggerNodes.item(i);
                        conditions.add(parseCrossNpcCondition(el));
                    }
                }
            }
        } catch (Exception e) {
            // cross-npc directory is optional
        }
        return Collections.unmodifiableList(conditions);
    }

    private NpcSpec parseNpcElement(Element root) {
        String id = root.getAttribute("id");
        String type = root.getAttribute("type");
        String category = root.getAttribute("category");
        boolean isNamed = "named".equalsIgnoreCase(type);

        String displayName = getTextContent(root, "display-name", id);

        Map<String, Integer> personality = parsePersonality(root);
        Map<String, Integer> moodInitial = parseMoodInitial(root);
        boolean memoryEnabled = parseMemoryEnabled(root);
        int shortTermSize = parseShortTermSize(root);
        List<ScheduleSlot> schedule = parseSchedule(root);
        List<ScoredAction> actions = parseActions(root);
        List<String> questLines = parseQuestLines(root);

        return new NpcSpec(
                id, type, category, displayName, isNamed,
                personality, moodInitial, memoryEnabled, shortTermSize,
                schedule, actions, questLines
        );
    }

    private Map<String, Integer> parsePersonality(Element root) {
        Map<String, Integer> traits = new LinkedHashMap<>();
        NodeList personalityNodes = root.getElementsByTagName("personality");
        if (personalityNodes.getLength() > 0) {
            Element personalityEl = (Element) personalityNodes.item(0);
            NodeList traitNodes = personalityEl.getElementsByTagName("trait");
            for (int i = 0; i < traitNodes.getLength(); i++) {
                Element trait = (Element) traitNodes.item(i);
                traits.put(trait.getAttribute("name"), Integer.parseInt(trait.getAttribute("value")));
            }
        }
        return Collections.unmodifiableMap(traits);
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
        return Collections.unmodifiableMap(mood);
    }

    private boolean parseMemoryEnabled(Element root) {
        NodeList memNodes = root.getElementsByTagName("memory");
        if (memNodes.getLength() > 0) {
            return "true".equalsIgnoreCase(((Element) memNodes.item(0)).getAttribute("enabled"));
        }
        return false;
    }

    private int parseShortTermSize(Element root) {
        NodeList memNodes = root.getElementsByTagName("memory");
        if (memNodes.getLength() > 0) {
            String size = ((Element) memNodes.item(0)).getAttribute("short-term-size");
            if (!size.isEmpty()) return Integer.parseInt(size);
        }
        return 10;
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
        return Collections.unmodifiableList(slots);
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
        return Collections.unmodifiableList(actions);
    }

    private List<ConditionSpec> parseConditions(Element parent) {
        List<ConditionSpec> conditions = new ArrayList<>();
        NodeList condNodes = parent.getElementsByTagName("conditions");
        if (condNodes.getLength() > 0) {
            Element condsEl = (Element) condNodes.item(0);
            NodeList condList = condsEl.getElementsByTagName("condition");
            for (int i = 0; i < condList.getLength(); i++) {
                Element c = (Element) condList.item(i);
                conditions.add(new ConditionSpec(
                        c.getAttribute("type"),
                        c.getAttribute("axis").isEmpty() ? c.getAttribute("check") : c.getAttribute("axis"),
                        c.getAttribute("operator"),
                        c.getAttribute("value")
                ));
            }
        }
        return Collections.unmodifiableList(conditions);
    }

    private List<Map<String, String>> parseOptions(Element parent) {
        List<Map<String, String>> options = new ArrayList<>();
        NodeList optionsNodes = parent.getElementsByTagName("options");
        if (optionsNodes.getLength() > 0) {
            Element optionsEl = (Element) optionsNodes.item(0);
            NodeList optList = optionsEl.getElementsByTagName("option");
            for (int i = 0; i < optList.getLength(); i++) {
                Element o = (Element) optList.item(i);
                Map<String, String> optionMap = new LinkedHashMap<>();
                var attrs = o.getAttributes();
                for (int j = 0; j < attrs.getLength(); j++) {
                    optionMap.put(attrs.item(j).getNodeName(), attrs.item(j).getNodeValue());
                }
                options.add(Collections.unmodifiableMap(optionMap));
            }
        }
        return Collections.unmodifiableList(options);
    }

    private List<String> parseQuestLines(Element root) {
        List<String> quests = new ArrayList<>();
        NodeList questNodes = root.getElementsByTagName("quest-lines");
        if (questNodes.getLength() > 0) {
            String text = questNodes.item(0).getTextContent().trim();
            if (!text.isEmpty()) {
                for (String q : text.split(",")) {
                    quests.add(q.trim());
                }
            }
        }
        return Collections.unmodifiableList(quests);
    }

    private CrossNpcConditionSpec parseCrossNpcCondition(Element el) {
        return new CrossNpcConditionSpec(
                el.getAttribute("id"),
                el.getAttribute("source-npc"),
                el.getAttribute("target-npc"),
                el.getAttribute("edge-field"),
                el.getAttribute("operator"),
                el.getAttribute("threshold").isEmpty() ? 0 : Integer.parseInt(el.getAttribute("threshold")),
                el.getAttribute("event-id")
        );
    }

    private String getTextContent(Element parent, String tagName, String defaultValue) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return defaultValue;
    }
}
