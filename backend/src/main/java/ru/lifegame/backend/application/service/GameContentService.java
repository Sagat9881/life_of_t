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

    private final ConflictEngine conflictEngine;

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

    private void loadConflictsFromEngine() {
        conflictEngine.getConflictSpecs().forEach(spec -> {
            ConflictDefView view = toConflictDefView(spec);
            conflicts.put(view.type(), view);
        });
        log.info("Loaded {} conflicts from ConflictEngine", conflicts.size());
    }

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
     * Scans classpath:narrative/quests/**\/*.xml recursively.
     * Each file must contain exactly one {@code <quest>} root element.
     * Duplicate quest IDs: first file wins (alphabetical scan order).
     * Missing directory: logs a warning, starts without quests (non-fatal).
     */
    private void loadQuestsFromXml() {
        QuestSpecParser parser = new QuestSpecParser();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] files = resolver.getResources("classpath:narrative/quests/**/*.xml");
            if (files.length == 0) {
                log.warn("No quest XML files found at classpath:narrative/quests/ — starting without quests");
                return;
            }
            for (Resource res : files) {
                String filename = res.getFilename();
                try (InputStream is = res.getInputStream()) {
                    QuestSpec spec = parser.parseOne(is, filename);
                    quests.putIfAbsent(spec.id(), spec);
                } catch (Exception e) {
                    log.error("Failed to parse quest file: {} — skipping", filename, e);
                }
            }
            log.info("Loaded {} quests", quests.size());
        } catch (Exception e) {
            log.error("Failed to scan classpath:narrative/quests/", e);
        }
    }

    private void loadActionsFromXml() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] files = resolver.getResources("classpath:narrative/actions/**/*.xml");
            if (files.length == 0) {
                throw new IllegalStateException(
                    "No player action XML files found at classpath:narrative/actions/ — game cannot start");
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            for (Resource res : files) {
                String filename = res.getFilename();
                try (InputStream is = res.getInputStream()) {
                    Document doc = builder.parse(is);
                    doc.getDocumentElement().normalize();
                    Element root = doc.getDocumentElement();
                    ActionDefView action = parseAction(root, filename);
                    actions.put(action.code(), action);
                } catch (Exception e) {
                    throw new RuntimeException(
                        "Failed to parse action file: " + filename + " — game cannot start", e);
                }
            }
            log.info("Loaded {} actions", actions.size());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to scan classpath:narrative/actions/ — game cannot start", e);
        }
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

        List<String> tags           = parseTextList(el, "tags",               "tag");
        List<String> requiredTags   = parseTextList(el, "required-tags",      "tag");
        List<String> forbiddenTags  = parseTextList(el, "forbidden-tags",     "tag");
        List<String> timeSlots      = parseTextList(el, "time-slots",         "slot");
        List<String> locations      = parseTextList(el, "locations",          "location");
        List<String> conflictTypes  = parseTextList(el, "potential-conflicts", "conflict");
        List<String> relatedQuests  = parseTextList(el, "related-quests",      "quest");

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
