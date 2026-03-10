package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;

/**
 * Fired when a relationship is permanently broken after a conflict resolution.
 * Frontend uses this to trigger the relationship-break animation and update NPC status.
 */
public record RelationshipBrokenEvent(
        String sessionId,
        String npcId
) implements DomainEvent {
    @Override public Instant timestamp() { return Instant.now(); }
    @Override public String eventType() { return "RELATIONSHIP_BROKEN"; }
}
