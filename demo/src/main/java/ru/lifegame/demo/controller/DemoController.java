package ru.lifegame.demo.controller;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.lifegame.demo.dto.DemoDtos.ActionRequestDto;
import ru.lifegame.demo.dto.DemoDtos.ActionResultDto;
import ru.lifegame.demo.dto.DemoDtos.CharacterDto;
import ru.lifegame.demo.dto.DemoDtos.GameStateDto;
import ru.lifegame.demo.dto.DemoDtos.QuestSummaryDto;
import ru.lifegame.demo.dto.DemoDtos.RelationshipsDto;
import ru.lifegame.demo.service.DemoGameService;

import java.util.List;
import java.util.Map;

/**
 * REST API for the demo module.
 *
 * <ul>
 *   <li>{@code GET  /api/demo/status}     – full game-state snapshot</li>
 *   <li>{@code GET  /api/demo/quest-log}  – active quests</li>
 *   <li>{@code GET  /api/demo/characters} – character info</li>
 *   <li>{@code POST /api/demo/action}     – execute interactive action</li>
 *   <li>{@code POST /api/demo/shutdown}   – graceful shutdown</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final DemoGameService gameService;
    private final ApplicationContext applicationContext;

    public DemoController(DemoGameService gameService,
                          ApplicationContext applicationContext) {
        this.gameService = gameService;
        this.applicationContext = applicationContext;
    }

    @GetMapping("/status")
    public ResponseEntity<GameStateDto> status() {
        return ResponseEntity.ok(gameService.buildGameStateDto());
    }

    @GetMapping("/quest-log")
    public ResponseEntity<List<QuestSummaryDto>> questLog() {
        return ResponseEntity.ok(gameService.buildActiveQuestDtos());
    }

    @GetMapping("/characters")
    public ResponseEntity<Map<String, CharacterDto>> characters() {
        CharacterDto tanya = gameService.buildCharacterDto();
        return ResponseEntity.ok(Map.of("tanya", tanya));
    }

    @GetMapping("/relationships")
    public ResponseEntity<RelationshipsDto> relationships() {
        return ResponseEntity.ok(gameService.buildRelationshipsDto());
    }

    /**
     * Executes an interactive action on a target NPC/self.
     * E.g. {"target": "alexander", "actionCode": "talk"}
     */
    @PostMapping("/action")
    public ResponseEntity<ActionResultDto> executeAction(@RequestBody ActionRequestDto request) {
        ActionResultDto result = gameService.executeAction(request.target(), request.actionCode());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/shutdown")
    public ResponseEntity<Map<String, String>> shutdown() {
        Thread shutdownThread = new Thread(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            SpringApplication.exit(applicationContext, () -> 0);
        });
        shutdownThread.setDaemon(true);
        shutdownThread.start();
        return ResponseEntity.ok(Map.of("status", "shutting down"));
    }
}
