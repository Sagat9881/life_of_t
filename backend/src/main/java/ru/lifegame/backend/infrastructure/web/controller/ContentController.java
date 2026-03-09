package ru.lifegame.backend.infrastructure.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.lifegame.backend.application.service.content.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for game content (data-driven frontend).
 * 
 * Frontend calls these endpoints ONCE at bootstrap to load all game content.
 * Content is static/cacheable — no session ID needed.
 * 
 * Endpoints:
 *   GET /api/content/actions       — all player actions
 *   GET /api/content/npcs          — all NPC definitions
 *   GET /api/content/locations     — all location configs
 *   GET /api/content/conflicts     — all conflict types
 *   GET /api/content/quests        — all quest definitions
 *   GET /api/content/animations    — all character animations
 *   GET /api/content/all           — everything in one call
 */
@RestController
@RequestMapping("/api/content")
public class ContentController {
    private static final Logger log = LoggerFactory.getLogger(ContentController.class);

    private final ActionContentService actionService;
    private final NPCContentService npcService;
    private final LocationContentService locationService;
    private final ConflictContentService conflictService;
    private final QuestContentService questService;

    public ContentController(
            ActionContentService actionService,
            NPCContentService npcService,
            LocationContentService locationService,
            ConflictContentService conflictService,
            QuestContentService questService
    ) {
        this.actionService = actionService;
        this.npcService = npcService;
        this.locationService = locationService;
        this.conflictService = conflictService;
        this.questService = questService;
    }

    /**
     * Get all player actions.
     * @return list of action definitions with effects, costs, requirements
     */
    @GetMapping("/actions")
    public List<Map<String, Object>> getAllActions() {
        log.debug("Loading all player actions");
        return actionService.loadAllActions();
    }

    /**
     * Get all NPC definitions.
     * @return list of NPCs with schedules, personalities, relationships
     */
    @GetMapping("/npcs")
    public List<Map<String, Object>> getAllNPCs() {
        log.debug("Loading all NPCs");
        return npcService.loadAllNPCs();
    }

    /**
     * Get all location configs.
     * @return list of locations with objects, triggers, visual data
     */
    @GetMapping("/locations")
    public List<Map<String, Object>> getAllLocations() {
        log.debug("Loading all locations");
        return locationService.loadAllLocations();
    }

    /**
     * Get all conflict types.
     * @return list of conflict definitions with tactics, resolutions
     */
    @GetMapping("/conflicts")
    public List<Map<String, Object>> getAllConflicts() {
        log.debug("Loading all conflict types");
        return conflictService.loadAllConflicts();
    }

    /**
     * Get all quest definitions.
     * @return list of quests with steps, rewards, triggers
     */
    @GetMapping("/quests")
    public List<Map<String, Object>> getAllQuests() {
        log.debug("Loading all quests");
        return questService.loadAllQuests();
    }

    /**
     * Get all character animation definitions.
     * @return map of characterId -> animations
     */
    @GetMapping("/animations")
    public Map<String, List<Map<String, Object>>> getAllAnimations() {
        log.debug("Loading all animations");
        return Map.of(
            "persi", List.of(
                Map.of("name", "idle", "frames", 24, "fps", 12, "loop", true),
                Map.of("name", "walk", "frames", 12, "fps", 8, "loop", true)
            ),
            "duke", List.of(
                Map.of("name", "idle_morning", "frames", 12, "fps", 6, "loop", true),
                Map.of("name", "walk", "frames", 12, "fps", 8, "loop", true),
                Map.of("name", "run_puppy", "frames", 12, "fps", 14, "loop", true)
            )
            // TODO: load from asset-specs XML dynamically
        );
    }

    /**
     * Get ALL content in single request (faster initial load).
     * Frontend can use this for bootstrap instead of 6 separate calls.
     */
    @GetMapping("/all")
    public Map<String, Object> getAllContent() {
        log.info("Loading ALL game content for frontend bootstrap");
        return Map.of(
            "actions", actionService.loadAllActions(),
            "npcs", npcService.loadAllNPCs(),
            "locations", locationService.loadAllLocations(),
            "conflicts", conflictService.loadAllConflicts(),
            "quests", questService.loadAllQuests(),
            "animations", getAllAnimations()
        );
    }
}
