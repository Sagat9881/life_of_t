package ru.lifegame.backend.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.lifegame.backend.application.service.GameContentService;
import ru.lifegame.backend.domain.dto.content.ContentVersion;
import ru.lifegame.backend.domain.narrative.spec.QuestSpec;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/content")
@Tag(name = "Game Content")
public class QuestsContentController {

    private final GameContentService contentService;

    public QuestsContentController(GameContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping("/quests")
    @Operation(summary = "Get all quest definitions",
               description = "Returns quest structure with steps, objectives and rewards")
    public ResponseEntity<QuestsContentResponse> getQuests() {
        ContentVersion version = contentService.getCurrentVersion();
        List<QuestSpec> quests = contentService.getAllQuests();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .eTag(version.version())
                .body(new QuestsContentResponse(version, quests));
    }

    public record QuestsContentResponse(
            ContentVersion version,
            List<QuestSpec> quests
    ) {}
}
