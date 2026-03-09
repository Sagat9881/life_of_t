package ru.lifegame.backend.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import ru.lifegame.backend.domain.dto.content.*;

import jakarta.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for loading and caching game content from XML specs.
 * Parses narrative/*.xml files and converts to ContentDef DTOs.
 */
@Service
public class GameContentService {

    private static final Logger log = LoggerFactory.getLogger(GameContentService.class);

    private final Map<String, ActionDefView> actions = new ConcurrentHashMap<>();
    private final Map<String, ConflictDefView> conflicts = new ConcurrentHashMap<>();
    private final Map<String, QuestDefView> quests = new ConcurrentHashMap<>();
    private final Map<String, EventDefView> events = new ConcurrentHashMap<>();
    
    private ContentVersion currentVersion;

    @PostConstruct
    public void initialize() {
        loadContent();
    }

    private void loadContent() {
        log.info("Loading game content from narrative XMLs...");
        
        loadActionsFromXml();
        loadQuestsFromXml();
        loadEventsFromXml();
        loadPlaceholderConflicts(); // TODO: Create conflict XMLs
        
        currentVersion = new ContentVersion("2.0.0-xml", Instant.now());
        
        log.info("Content loaded: {} actions, {} quests, {} events, {} conflicts",
            actions.size(), quests.size(), events.size(), conflicts.size());
    }

    public List<ActionDefView> getAllActions() {
        return List.copyOf(actions.values());
    }

    public List<ConflictDefView> getAllConflicts() {
        return List.copyOf(conflicts.values());
    }

    public List<QuestDefView> getAllQuests() {
        return List.copyOf(quests.values());
    }

    public List<EventDefView> getAllEvents() {
        return List.copyOf(events.values());
    }

    public ContentVersion getCurrentVersion() {
        return currentVersion;
    }

    // ============ XML Loaders ============

    private void loadActionsFromXml() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource actionsXml = resolver.getResource("classpath:narrative/player-actions/actions.xml");
            
            if (!actionsXml.exists()) {
                log.warn("actions.xml not found, keeping placeholder data");
                loadPlaceholderActions();
                return;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(actionsXml.getInputStream());
            doc.getDocumentElement().normalize();

            NodeList actionNodes = doc.getElementsByTagName("action");
            for (int i = 0; i < actionNodes.getLength(); i++) {
                try {
                    Element actionEl = (Element) actionNodes.item(i);
                    ActionDefView action = parseAction(actionEl);
                    actions.put(action.code(), action);
                } catch (Exception e) {
                    log.error("Failed to parse action at index {}", i, e);
                }
            }
            
            log.info("Loaded {} actions from XML", actions.size());
        } catch (Exception e) {
            log.error("Failed to load actions.xml, using placeholders", e);
            loadPlaceholderActions();
        }
    }

    private ActionDefView parseAction(Element actionEl) {
        String code = actionEl.getAttribute("code");
        String label = getTextContent(actionEl, "label");
        String description = getTextContent(actionEl, "description");
        int timeCost = Integer.parseInt(actionEl.getAttribute("time-cost"));

        // Parse stats
        Map<String, Integer> statEffects = new HashMap<>();
        Element statsEl = (Element) actionEl.getElementsByTagName("stats").item(0);
        if (statsEl != null) {
            parseAttribute(statsEl, "energy", statEffects, "energy");
            parseAttribute(statsEl, "health", statEffects, "health");
            parseAttribute(statsEl, "stress", statEffects, "stress");
            parseAttribute(statsEl, "mood", statEffects, "mood");
            parseAttribute(statsEl, "money", statEffects, "money");
            parseAttribute(statsEl, "self-esteem", statEffects, "selfEsteem");
        }

        // Parse skill gains
        Map<String, Integer> skillGains = new HashMap<>();
        NodeList skillNodes = actionEl.getElementsByTagName("skill");
        for (int i = 0; i < skillNodes.getLength(); i++) {
            Element skillEl = (Element) skillNodes.item(i);
            String skillName = skillEl.getAttribute("name");
            int xp = Integer.parseInt(skillEl.getAttribute("xp"));
            skillGains.put(skillName, xp);
        }

        int energyCost = Math.abs(statEffects.getOrDefault("energy", 0));
        int moneyGain = statEffects.getOrDefault("money", 0);

        return new ActionDefView(
            code,
            label,
            description,
            List.of(), // tags
            energyCost,
            energyCost,
            Map.of(), // requiredSkills
            List.of(), // requiredTags
            List.of(), // forbiddenTags
            statEffects,
            skillGains,
            moneyGain,
            timeCost * 60, // hours to minutes
            code.toLowerCase(),
            mapActionToIcon(code),
            List.of("morning", "day", "evening"),
            List.of("any"),
            List.of(),
            List.of()
        );
    }

    private void loadQuestsFromXml() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] questFiles = resolver.getResources("classpath:narrative/quests/*.xml");
            
            for (Resource questFile : questFiles) {
                try {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document doc = builder.parse(questFile.getInputStream());
                    doc.getDocumentElement().normalize();

                    Element questEl = (Element) doc.getElementsByTagName("quest").item(0);
                    QuestDefView quest = parseQuest(questEl);
                    quests.put(quest.id(), quest);
                } catch (Exception e) {
                    log.error("Failed to parse quest file: {}", questFile.getFilename(), e);
                }
            }
            
            log.info("Loaded {} quests from XML", quests.size());
        } catch (Exception e) {
            log.error("Failed to load quest XMLs", e);
        }
    }

    private QuestDefView parseQuest(Element questEl) {
        String id = questEl.getAttribute("id");
        String type = questEl.getAttribute("type");

        Element metaEl = (Element) questEl.getElementsByTagName("meta").item(0);
        String title = getTextContent(metaEl, "title-ru");
        String description = getTextContent(metaEl, "description-ru");

        // Parse objectives as steps
        List<QuestDefView.QuestStepView> steps = new ArrayList<>();
        NodeList objNodes = questEl.getElementsByTagName("objective");
        for (int i = 0; i < objNodes.getLength(); i++) {
            Element objEl = (Element) objNodes.item(i);
            String objId = objEl.getAttribute("id");
            String objType = objEl.getAttribute("type");
            String objDesc = getTextContent(objEl, "description-ru");

            steps.add(new QuestDefView.QuestStepView(
                objId,
                objDesc,
                objType,
                List.of(),
                null,
                null,
                Map.of()
            ));
        }

        return new QuestDefView(
            id,
            title,
            description,
            type.toLowerCase(),
            List.of(),
            Map.of(),
            1,
            steps,
            0,
            Map.of(),
            List.of(),
            "Квест завершён!"
        );
    }

    private void loadEventsFromXml() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] eventFiles = resolver.getResources("classpath:narrative/events/*.xml");
            
            for (Resource eventFile : eventFiles) {
                try {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document doc = builder.parse(eventFile.getInputStream());
                    doc.getDocumentElement().normalize();

                    Element eventEl = (Element) doc.getElementsByTagName("event").item(0);
                    EventDefView event = parseEvent(eventEl);
                    events.put(event.id(), event);
                } catch (Exception e) {
                    log.error("Failed to parse event file: {}", eventFile.getFilename(), e);
                }
            }
            
            log.info("Loaded {} events from XML", events.size());
        } catch (Exception e) {
            log.error("Failed to load event XMLs", e);
        }
    }

    private EventDefView parseEvent(Element eventEl) {
        String id = eventEl.getAttribute("id");
        String type = eventEl.getAttribute("type");

        Element metaEl = (Element) eventEl.getElementsByTagName("meta").item(0);
        String title = getTextContent(metaEl, "title-ru");
        String description = getTextContent(metaEl, "description-ru");

        // Simple events just have a single "acknowledge" option
        List<EventDefView.EventOptionView> options = List.of(
            new EventDefView.EventOptionView(
                "ACKNOWLEDGE",
                "Продолжить",
                Map.of(),
                List.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                0,
                List.of(),
                List.of(),
                description
            )
        );

        return new EventDefView(
            id,
            title,
            description,
            type.toLowerCase(),
            List.of(),
            Map.of(),
            1,
            null,
            options,
            5,
            type.equals("RANDOM")
        );
    }

    // ============ Helpers ============

    private String getTextContent(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent().trim() : "";
    }

    private void parseAttribute(Element el, String xmlAttr, Map<String, Integer> map, String mapKey) {
        if (el.hasAttribute(xmlAttr)) {
            map.put(mapKey, Integer.parseInt(el.getAttribute(xmlAttr)));
        }
    }

    private String mapActionToIcon(String code) {
        return switch (code) {
            case "GO_TO_WORK", "WORK_ON_PROJECT" -> "briefcase";
            case "DATE_WITH_HUSBAND" -> "heart";
            case "VISIT_FATHER" -> "car";
            case "PLAY_WITH_CAT" -> "cat";
            case "WALK_DOG" -> "dog";
            case "REST_AT_HOME", "SELF_CARE" -> "bed";
            case "HOUSEHOLD", "COOK_FOOD" -> "home";
            case "CALL_HUSBAND" -> "phone";
            case "EAT_FOOD" -> "utensils";
            case "FEED_PETS" -> "bowl";
            case "BEAUTY_ROUTINE" -> "mirror";
            default -> "circle";
        };
    }

    // ============ Placeholder (conflicts don't have XML yet) ============

    private void loadPlaceholderActions() {
        actions.put("REST", new ActionDefView(
            "REST", "Отдохнуть", "Отдохнуть и восстановить силы",
            List.of("self_care"), 0, 0, Map.of(), List.of(), List.of(),
            Map.of("energy", 30, "stress", -10, "mood", 10),
            Map.of(), 0, 60, "rest", "bed",
            List.of("morning", "day", "evening", "night"),
            List.of("home"), List.of(), List.of()
        ));
    }

    private void loadPlaceholderConflicts() {
        // TODO: Create conflict XMLs in narrative/
        conflicts.put("WORK_DEADLINE", new ConflictDefView(
            "WORK_DEADLINE",
            "Рабочий дедлайн",
            "Начальник требует сделать работу в нереальные сроки",
            List.of(
                new ConflictDefView.TacticDefView(
                    "SURRENDER", "Уступить", "Согласиться и работать сверхурочно",
                    Map.of(), -5, Map.of("boss", 5), Map.of(), 80, Map.of()
                ),
                new ConflictDefView.TacticDefView(
                    "ASSERT", "Настоять", "Объяснить, что сроки нереальны",
                    Map.of("assertiveness", 30), 15, Map.of("boss", -10),
                    Map.of("assertiveness", 2), 40, Map.of("assertiveness", 20)
                ),
                new ConflictDefView.TacticDefView(
                    "COMPROMISE", "Компромисс", "Предложить реалистичный промежуточный вариант",
                    Map.of("communication", 20), 10, Map.of("boss", 0),
                    Map.of("communication", 2), 60, Map.of("communication", 15)
                )
            ),
            50
        ));
    }
}
