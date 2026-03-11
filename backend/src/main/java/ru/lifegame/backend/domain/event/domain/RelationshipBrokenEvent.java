package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;

public record RelationshipBrokenEvent(
        String sessionId,
        String npcId,
        Instant timestamp
) implements DomainEvent {

    public RelationshipBrokenEvent(String sessionId, String npcId) {
        this(sessionId, npcId, Instant.now());
    }

    @Override public String eventType() { return "RELATIONSHIP_BROKEN"; }
}
