package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;
import java.util.List;

/**
 * Domain event fired when a narrative event is triggered for a session.
 * options is typed as List<NarrativeOption> for type safety — previously
 * it was List<EventOptionView> which violated clean architecture by depending
 * on the application layer.
 */
public record NarrativeEventTriggeredEvent(
        String sessionId,
        String narrativeEventId,
        String title,
        String description,
        List<NarrativeOption> options,
        Instant timestamp
) implements DomainEvent {

    public record NarrativeOption(String code, String labelRu) {}

    public NarrativeEventTriggeredEvent(String sessionId, String narrativeEventId,
                                        String title, String description,
                                        List<NarrativeOption> options) {
        this(sessionId, narrativeEventId, title, description, options, Instant.now());
    }

    @Override public String eventType() { return "NARRATIVE_EVENT_TRIGGERED"; }
}
