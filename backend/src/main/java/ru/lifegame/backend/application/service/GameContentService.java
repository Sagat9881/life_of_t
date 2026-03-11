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
import ru.lifegame.backend.domain.conflict.engine.ConflictEngine;
import ru.lifegame.backend.domain.conflict.spec.ConflictSpec;
import ru.lifegame.backend.domain.conflict.spec.ConflictTacticSpec;
import ru.lifegame.backend.domain.dto.content.ActionDefView;
import ru.lifegame.backend.domain.dto.content.ConflictDefView;
import ru.lifegame.backend.domain.dto.content.ContentVersion;
import ru.lifegame.backend.domain.narrative.parser.EventSpecParser;
import ru.lifegame.backend.domain.narrative.parser.QuestSpecParser;
import ru.lifegame.backend.domain.narrative.spec.EventSpec;
import ru.lifegame.backend.domain.narrative.spec.QuestSpec;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameContentService {

    private static final Logger log = LoggerFactory.getLogger(GameContentService.class);

    // classpath*: prefix required for PathMatchingResourcePatternResolver to
    // scan inside nested JARs (BOOT-INF/lib/life-of-t.jar) in Spring Boot fat JAR.
    private static final String PATH_ACTIONS = "classpath*:narrative/player-actions/**/*.xml";
    private static final String PATH_EVENTS  = "classpath*:narrative/events/*.xml";
    private static final String PATH_QUESTS  = "classpath*:narrative/quests/**/*.xml";

    private final Map<String, ActionDefView>   actions   = new ConcurrentHashMap<>();
    private final Map<String, ConflictDefView> conflicts = new ConcurrentHashMap<>();
    private final Map<String, EventSpec>       events    = new ConcurrentHashMap<>();
    private final Map<String, QuestSpec>       quests    = new ConcurrentHashMap<>();

    private final ConflictEngine conflictEngine;
    // Explicit classLoader — required for nested-JAR scanning in fat JARs.
    private final PathMatchingResourcePatternResolver resolver =
            new PathMatchingResourcePatternResolver(GameContentService.class.getClassLoader());

    private ContentVersion currentVersion;

    public GameContentService(ConflictEngine conflictEngine) {
        this.conflictEngine = conflictEngine;
    }

    @PostConstruct
    public void initialize() {
        log.info("Loading game content from narrative XMLs...");
        loadActionsFromXml();
        loadEventsFromXml();
        loadQuestsFromXml();
        loadConflictsFromEngine();
        // CRITICAL: player actions are required for the game to function.
        if (actions.isEmpty()) {
            throw new IllegalStateException(
                    "No player actions loaded from " + PATH_ACTIONS +
                    " — cannot start the game. Check that life-of-t.jar is on the classpath "
                    + "and contains narrative/player-actions/*.xml files.");
        }
        currentVersion = new ContentVersion("2.0.0-xml", Instant.now());
        log.info("Content loaded: {} actions, {} events, {} quests, {} conflicts",
                actions.size(), events.size(), quests.size(), conflicts.size());
    }

    public List<ActionDefView>   getAllActions()    { return List.copyOf(actions.values()); }
    public List<ConflictDefView> getAllConflicts()  { return List.copyOf(conflicts.values()); }
    public List<EventSpec>       getAllEvents()     { return List.copyOf(events.values()); }
    public List<QuestSpec>       getAllQuests()     { return List.copyOf(quests.values()); }
    public ContentVersion        getCurrentVersion() { return currentVersion; }

    public Optional<EventSpec> getEvent(String id) { return Optional.ofNullable(events.get(id)); }
    public Optional<QuestSpec> getQuest(String id) { return Optional.ofNullable(quests.get(id)); }

    // ─────────────────────────────────────────────────────────────────────────
    // Loaders
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Scans {@code classpath*:narrative/player-actions/**\/*.xml}.
     * Scan failure or empty result → WARN + return (non-fatal here;
     * emptiness is checked critically in {@link #initialize()}).
     * Per-file parse error → ERROR + skip file.
     */
    private void loadActionsFromXml() {
        Resource[] files;
        try {
            files = resolver.getResources(PATH_ACTIONS);
        } catch (IOException e) {
            log.warn("Cannot scan {}: {} — starting without actions", PATH_ACTIONS, e.getMessage());
            return;
        }
        if (files == null || files.length == 0) {
            log.warn("No player action XML files found at {} — starting without actions", PATH_ACTIONS);
            return;
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (Exception e) {
            log.error("Cannot create XML DocumentBuilder — skipping action load", e);
            return;
        }
        for (Resource res : files) {
            String filename = res.getFilename();
            try (InputStream is = res.getInputStream()) {
                Document doc = builder.parse(is);
                doc.getDocumentElement().normalize();
                ActionDefView action = parseAction(doc.getDocumentElement(), filename);
                actions.put(action.code(), action);
            } catch (Exception e) {
                log.error("Failed to parse action file: {} — skipping", filename, e);
            }
        }
        log.info("Loaded {} actions from {}", actions.size(), PATH_ACTIONS);
    }

    /**
     * Scans {@code classpath*:narrative/events/*.xml}.
     * Non-critical: missing directory or empty result → WARN only.
     */
    private void loadEventsFromXml() {
        Resource[] files;
        try {
            files = resolver.getResources(PATH_EVENTS);
        } catch (IOException e) {
            log.warn("Cannot scan {}: {} — starting without events", PATH_EVENTS, e.getMessage());
            return;
        }
        if (files == null || files.length == 0) {
            log.warn("No event XML files found at {} — starting without events", PATH_EVENTS);
            return;
        }
        EventSpecParser parser = new EventSpecParser();
        for (Resource res : files) {
            String filename = res.getFilename();
            try (InputStream is = res.getInputStream()) {
                EventSpec spec = parser.parse(is, filename);
                events.put(spec.id(), spec);
            } catch (Exception e) {
                log.error("Failed to parse event file: {} — skipping", filename, e);
            }
        }
        log.info("Loaded {} events from {}", events.size(), PATH_EVENTS);
    }

    /**
     * Scans {@code classpath*:narrative/quests/**\/*.xml} recursively.
     * Each file must contain exactly one {@code <quest>} root element.
     * Duplicate quest IDs: first file wins.
     * Non-critical: missing directory or empty result → WARN only.
     */
    private void loadQuestsFromXml() {
        Resource[] files;
        try {
            files = resolver.getResources(PATH_QUESTS);
        } catch (IOException e) {
            log.warn("Cannot scan {}: {} — starting without quests", PATH_QUESTS, e.getMessage());
            return;
        }
        if (files == null || files.length == 0) {
            log.warn("No quest XML files found at {} — starting without quests", PATH_QUESTS);
            return;
        }
        QuestSpecParser parser = new QuestSpecParser();
        for (Resource res : files) {
            String filename = res.getFilename();
            try (InputStream is = res.getInputStream()) {
                QuestSpec spec = parser.parseOne(is, filename);
                quests.putIfAbsent(spec.id(), spec);
            } catch (Exception e) {
                log.error("Failed to parse quest file: {} — skipping", filename, e);
            }
        }
        log.info("Loaded {} quests from {}", quests.size(), PATH_QUESTS);
    }

    private void loadConflictsFromEngine() {
        conflictEngine.getConflictSpecs().forEach(spec -> {
            ConflictDefView view = toConflictDefView(spec);
            conflicts.put(view.type(), view);
        });
        log.info("Loaded {} conflicts from ConflictEngine", conflicts.size());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mappers
    // ─────────────────────────────────────────────────────────────────────────

    private ConflictDefView toConflictDefView(ConflictSpec spec) {
        List<ConflictDefView.TacticDefView> tacticViews = spec.tactics().stream()
                .map(this::toTacticDefView)
                .toList();
        return new ConflictDefView(
                spec.id(),
                spec.meta().label(),
                spec.meta().description(),
                tacticViews,
                50
        );
    }

    private ConflictDefView.TacticDefView toTacticDefView(ConflictTacticSpec tactic) {
        Map<String, Integer> successRelChanges = tactic.successOutcome().relationshipChanges() != null
                ? tactic.successOutcome().relationshipChanges() : Map.of();
        Map<String, Integer> successStatChanges = tactic.successOutcome().statChanges() != null
                ? tactic.successOutcome().statChanges() : Map.of();
        int stressReduction = -successStatChanges.getOrDefault("stress", 0);
        return new ConflictDefView.TacticDefView(
                tactic.code(),
                tactic.label(),
                tactic.description(),
                Map.of(),
                stressReduction,
                successRelChanges,
                Map.of(),
                tactic.baseCspCost(),
                Map.of()
        );
    }

    private ActionDefView parseAction(Element el, String filename) {
        String code        = requireAttr(el, "code", filename);
        String label       = getTextContent(el, "label");
        String description = getTextContent(el, "description");
        String icon        = getTextContent(el, "icon");
        String animTrigger = getTextContent(el, "animation-trigger");
        if (animTrigger.isEmpty()) animTrigger = code.toLowerCase();

        int timeCost = 60;
        String timeCostAttr = el.getAttribute("time-cost");
        if (!timeCostAttr.isEmpty()) timeCost = Integer.parseInt(timeCostAttr);

        Map<String, Integer> statEffects = new HashMap<>();
        Element statsEl = (Element) el.getElementsByTagName("stats").item(0);
        if (statsEl != null) {
            parseStatAttr(statsEl, "energy",      statEffects, "energy");
            parseStatAttr(statsEl, "health",      statEffects, "health");
            parseStatAttr(statsEl, "stress",      statEffects, "stress");
            parseStatAttr(statsEl, "mood",        statEffects, "mood");
            parseStatAttr(statsEl, "money",       statEffects, "money");
            parseStatAttr(statsEl, "self-esteem", statEffects, "selfEsteem");
        }

        Map<String, Integer> skillGains = new HashMap<>();
        NodeList skillNodes = el.getElementsByTagName("skill");
        for (int i = 0; i < skillNodes.getLength(); i++) {
            Element s = (Element) skillNodes.item(i);
            skillGains.put(s.getAttribute("name"), Integer.parseInt(s.getAttribute("xp")));
        }

        List<String> tags          = parseTextList(el, "tags",                "tag");
        List<String> requiredTags  = parseTextList(el, "required-tags",       "tag");
        List<String> forbiddenTags = parseTextList(el, "forbidden-tags",      "tag");
        List<String> timeSlots     = parseTextList(el, "time-slots",          "slot");
        List<String> locations     = parseTextList(el, "locations",           "location");
        List<String> conflictTypes = parseTextList(el, "potential-conflicts",  "conflict");
        List<String> relatedQuests = parseTextList(el, "related-quests",       "quest");

        if (timeSlots.isEmpty()) timeSlots = List.of("morning", "day", "evening");
        if (locations.isEmpty()) locations = List.of("any");

        Map<String, Integer> requiredSkills = new HashMap<>();
        Element reqSkillsEl = (Element) el.getElementsByTagName("required-skills").item(0);
        if (reqSkillsEl != null) {
            NodeList rs = reqSkillsEl.getElementsByTagName("skill");
            for (int i = 0; i < rs.getLength(); i++) {
                Element s = (Element) rs.item(i);
                requiredSkills.put(s.getAttribute("name"), Integer.parseInt(s.getAttribute("level")));
            }
        }

        int energyCost = Math.abs(statEffects.getOrDefault("energy", 0));
        int moneyGain  = statEffects.getOrDefault("money", 0);

        return new ActionDefView(
                code, label, description,
                tags,
                energyCost, energyCost,
                requiredSkills,
                requiredTags, forbiddenTags,
                statEffects, skillGains, moneyGain,
                timeCost * 60,
                animTrigger,
                icon,
                timeSlots,
                locations,
                conflictTypes,
                relatedQuests
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // XML helpers
    // ─────────────────────────────────────────────────────────────────────────

    private String requireAttr(Element el, String attr, String filename) {
        String val = el.getAttribute(attr);
        if (val == null || val.isBlank()) {
            throw new IllegalStateException(
                    "Missing required attribute '" + attr + "' in action file: " + filename);
        }
        return val;
    }

    private void parseStatAttr(Element el, String xmlAttr, Map<String, Integer> map, String key) {
        if (el.hasAttribute(xmlAttr)) map.put(key, Integer.parseInt(el.getAttribute(xmlAttr)));
    }

    private String getTextContent(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent().trim() : "";
    }

    private List<String> parseTextList(Element parent, String containerTag, String itemTag) {
        Element container = (Element) parent.getElementsByTagName(containerTag).item(0);
        if (container == null) return List.of();
        NodeList items = container.getElementsByTagName(itemTag);
        List<String> result = new ArrayList<>();
        for (int i = 0; i < items.getLength(); i++) {
            String text = items.item(i).getTextContent().trim();
            if (!text.isEmpty()) result.add(text);
        }
        return Collections.unmodifiableList(result);
    }
}
