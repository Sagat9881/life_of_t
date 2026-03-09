package ru.lifegame.backend.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.lifegame.backend.application.service.GameContentService;
import ru.lifegame.backend.domain.dto.content.ActionDefView;
import ru.lifegame.backend.domain.dto.content.ContentVersion;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/content")
@Tag(name = "Game Content", description = "Game content definitions for data-driven frontend")
public class ActionsContentController {

    private final GameContentService contentService;

    public ActionsContentController(GameContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping("/actions")
    @Operation(summary = "Get all action definitions",
               description = "Returns all available actions with metadata for rendering UI")
    public ResponseEntity<ActionsContentResponse> getActions() {
        ContentVersion version = contentService.getCurrentVersion();
        List<ActionDefView> actions = contentService.getAllActions();
        
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
            .eTag(version.version())
            .body(new ActionsContentResponse(version, actions));
    }

    public record ActionsContentResponse(
        ContentVersion version,
        List<ActionDefView> actions
    ) {}
}
