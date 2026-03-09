package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record NarrativeEventTriggeredEvent(
        String sessionId,
        String narrativeEventId,
        String title,
        String description,
        List<Map<String, String>> options
) implements DomainEvent {
    @Override public Instant timestamp() { return Instant.now(); }
    @Override public String eventType() { return "NARRATIVE_EVENT_TRIGGERED"; }
}
