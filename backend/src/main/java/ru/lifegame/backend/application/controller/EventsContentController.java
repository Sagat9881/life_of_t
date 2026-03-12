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
import ru.lifegame.backend.domain.narrative.spec.EventSpec;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/content")
@Tag(name = "Game Content")
public class EventsContentController {

    private final GameContentService contentService;

    public EventsContentController(GameContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping("/events")
    @Operation(summary = "Get all narrative event definitions",
               description = "Returns all narrative events with dialogue lines, options and effects")
    public ResponseEntity<EventsContentResponse> getEvents() {
        ContentVersion version = contentService.getCurrentVersion();
        List<EventSpec> events = contentService.getAllEvents();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .eTag(version.version())
                .body(new EventsContentResponse(version, events));
    }

    public record EventsContentResponse(
            ContentVersion version,
            List<EventSpec> events
    ) {}
}
