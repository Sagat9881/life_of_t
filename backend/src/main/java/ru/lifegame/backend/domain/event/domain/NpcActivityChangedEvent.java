package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;

/**
 * Published when an NPC transitions to a different activity
 * (schedule slot change, brain override, or mood override).
 */
public record NpcActivityChangedEvent(
        String sessionId,
        String npcId,
        String oldActivity,
        String newActivity,
        String locationId
) implements DomainEvent {
    @Override public Instant timestamp() { return Instant.now(); }
    @Override public String eventType() { return "NPC_ACTIVITY_CHANGED"; }
}
