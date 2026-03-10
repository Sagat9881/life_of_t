package ru.lifegame.backend.application.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import ru.lifegame.backend.domain.dto.content.ActionDefView;
import ru.lifegame.backend.domain.dto.content.ConflictDefView;
import ru.lifegame.backend.domain.dto.content.ContentVersion;
import ru.lifegame.backend.domain.narrative.parser.EventSpecParser;
import ru.lifegame.backend.domain.narrative.parser.QuestSpecParser;
import ru.lifegame.backend.domain.npc.spec.EventSpec;
import ru.lifegame.backend.domain.narrative.spec.QuestSpec;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameContentService {

    private static final Logger log = LoggerFactory.getLogger(GameContentService.class);

    private final Map<String, ActionDefView>   actions   = new ConcurrentHashMap<>();
    private final Map<String, ConflictDefView> conflicts = new ConcurrentHashMap<>();
    private final Map<String, EventSpec>       events    = new ConcurrentHashMap<>();
    private final Map<String, QuestSpec>       quests    = new ConcurrentHashMap<>();

    private ContentVersion currentVersion;

    @PostConstruct
    public void initialize() {
        log.info("Loading game content from narrative XMLs...");
        loadActionsFromXml();
        loadEventsFromXml();
        loadQuestsFromXml();
        loadPlaceholderConflicts();
        currentVersion = new ContentVersion("2.0.0-xml", Instant.now());
        log.info("Content loaded: {} actions, {} events, {} quests, {} conflicts",
                actions.size(), events.size(), quests.size(), conflicts.size());
    }

    public List<ActionDefView>   getAllActions()   { return List.copyOf(actions.values()); }
    public List<ConflictDefView> getAllConflicts() { return List.copyOf(conflicts.values()); }
    public List<EventSpec>       getAllEvents()    { return List.copyOf(events.values()); }
    public List<QuestSpec>       getAllQuests()    { return List.copyOf(quests.values()); }
    public ContentVersion        getCurrentVersion() { return currentVersion; }

    public Optional<EventSpec> getEvent(String id) { return Optional.ofNullable(events.get(id)); }
    public Optional<QuestSpec> getQuest(String id) { return Optional.ofNullable(quests.get(id)); }

    private void loadEventsFromXml() {
        EventSpecParser parser = new EventSpecParser();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] files = resolver.getResources("classpath:narrative/events/*.xml");
            for (Resource res : files) {
                String filename = res.getFilename();
                try (InputStream is = res.getInputStream()) {
                    EventSpec spec = parser.parse(is, filename);
                    events.put(spec.id(), spec);
                } catch (Exception e) {
                    log.error("Failed to parse event file: {}", filename, e);
                }
            }
            log.info("Loaded {} events", events.size());
        } catch (Exception e) {
            log.error("Failed to scan narrative/events/", e);
        }
    }

    /**
     * Loads all quests from the single narrative/quests.xml container.
     * Was: scanning narrative/quests/*.xml (directory does not exist -> 0 quests).
     */
    private void loadQuestsFromXml() {
        QuestSpecParser parser = new QuestSpecParser();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource res = resolver.getResource("classpath:narrative/quests.xml");
            if (!res.exists()) {
                log.warn("narrative/quests.xml not found — no quests loaded");
                return;
            }
            try (InputStream is = res.getInputStream()) {
                List<QuestSpec> parsed = parser.parseAll(is, "quests.xml");
                parsed.forEach(q -> quests.put(q.id(), q));
                log.info("Loaded {} quests from quests.xml", parsed.size());
            }
        } catch (Exception e) {
            log.error("Failed to load narrative/quests.xml", e);
        }
    }

    private void loadActionsFromXml() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource actionsXml = resolver.getResource("classpath:narrative/player-actions/actions.xml");
            if (!actionsXml.exists()) {
                log.warn("actions.xml not found, using placeholder data");
                loadPlaceholderActions();
                return;
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc;
            try (InputStream is = actionsXml.getInputStream()) {
                doc = builder.parse(is);
            }
            doc.getDocumentElement().normalize();
            NodeList actionNodes = doc.getElementsByTagName("action");
            for (int i = 0; i < actionNodes.getLength(); i++) {
                try {
                    ActionDefView action = parseAction((Element) actionNodes.item(i));
                    actions.put(action.code(), action);
                } catch (Exception e) {
                    log.error("Failed to parse action at index {}", i, e);
                }
            }
            log.info("Loaded {} actions", actions.size());
        } catch (Exception e) {
            log.error("Failed to load actions.xml, using placeholders", e);
            loadPlaceholderActions();
        }
    }

    private ActionDefView parseAction(Element el) {
        String code        = el.getAttribute("code");
        String label       = getTextContent(el, "label");
        String description = getTextContent(el, "description");
        int    timeCost    = Integer.parseInt(el.getAttribute("time-cost"));

        Map<String, Integer> statEffects = new HashMap<>();
        Element statsEl = (Element) el.getElementsByTagName("stats").item(0);
        if (statsEl != null) {
            parseAttr(statsEl, "energy",      statEffects, "energy");
            parseAttr(statsEl, "health",      statEffects, "health");
            parseAttr(statsEl, "stress",      statEffects, "stress");
            parseAttr(statsEl, "mood",        statEffects, "mood");
            parseAttr(statsEl, "money",       statEffects, "money");
            parseAttr(statsEl, "self-esteem", statEffects, "selfEsteem");
        }

        Map<String, Integer> skillGains = new HashMap<>();
        NodeList skillNodes = el.getElementsByTagName("skill");
        for (int i = 0; i < skillNodes.getLength(); i++) {
            Element s = (Element) skillNodes.item(i);
            skillGains.put(s.getAttribute("name"), Integer.parseInt(s.getAttribute("xp")));
        }

        int energyCost = Math.abs(statEffects.getOrDefault("energy", 0));
        int moneyGain  = statEffects.getOrDefault("money", 0);

        return new ActionDefView(
                code, label, description,
                List.of(), energyCost, energyCost, Map.of(), List.of(), List.of(),
                statEffects, skillGains, moneyGain,
                timeCost * 60,
                code.toLowerCase(),
                mapActionToIcon(code),
                List.of("morning", "day", "evening"),
                List.of("any"),
                List.of(), List.of()
        );
    }

    private void parseAttr(Element el, String xmlAttr, Map<String, Integer> map, String key) {
        if (el.hasAttribute(xmlAttr)) map.put(key, Integer.parseInt(el.getAttribute(xmlAttr)));
    }

    private String getTextContent(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent().trim() : "";
    }

    private String mapActionToIcon(String code) {
        return switch (code) {
            case "GO_TO_WORK", "WORK_ON_PROJECT" -> "briefcase";
            case "DATE_WITH_HUSBAND"             -> "heart";
            case "VISIT_FATHER"                  -> "car";
            case "PLAY_WITH_CAT"                 -> "cat";
            case "WALK_DOG"                      -> "dog";
            case "REST_AT_HOME", "SELF_CARE"     -> "bed";
            case "HOUSEHOLD", "COOK_FOOD"        -> "home";
            case "CALL_HUSBAND"                  -> "phone";
            case "EAT_FOOD"                      -> "utensils";
            case "FEED_PETS"                     -> "bowl";
            case "BEAUTY_ROUTINE"                -> "mirror";
            default                              -> "circle";
        };
    }

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
