package ru.lifegame.backend.domain.event.domain;

import ru.lifegame.backend.application.view.EventOptionView;

import java.time.Instant;
import java.util.List;

/**
 * Domain event fired when a narrative event is triggered for a session.
 * options is typed as List<EventOptionView> for type safety — previously
 * it was List<Map<String,String>> which was unstructured and error-prone.
 */
public record NarrativeEventTriggeredEvent(
        String sessionId,
        String narrativeEventId,
        String title,
        String description,
        List<EventOptionView> options
) implements DomainEvent {
    @Override public Instant timestamp() { return Instant.now(); }
    @Override public String eventType() { return "NARRATIVE_EVENT_TRIGGERED"; }
}
