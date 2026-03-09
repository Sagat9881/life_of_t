package ru.lifegame.backend.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.lifegame.backend.application.service.GameContentService;
import ru.lifegame.backend.domain.dto.content.ConflictDefView;
import ru.lifegame.backend.domain.dto.content.ContentVersion;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/content")
@Tag(name = "Game Content")
public class ConflictsContentController {

    private final GameContentService contentService;

    public ConflictsContentController(GameContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping("/conflicts")
    @Operation(summary = "Get all conflict type definitions",
               description = "Returns conflict types with tactics and skill requirements")
    public ResponseEntity<ConflictsContentResponse> getConflicts() {
        ContentVersion version = contentService.getCurrentVersion();
        List<ConflictDefView> conflicts = contentService.getAllConflicts();
        
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
            .eTag(version.version())
            .body(new ConflictsContentResponse(version, conflicts));
    }

    public record ConflictsContentResponse(
        ContentVersion version,
        List<ConflictDefView> conflicts
    ) {}
}
