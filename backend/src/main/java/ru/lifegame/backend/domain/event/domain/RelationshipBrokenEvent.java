package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;

public record RelationshipBrokenEvent(String sessionId, String npcName) implements DomainEvent {
    @Override public Instant timestamp() { return Instant.now(); }
    @Override public String eventType() { return "RELATIONSHIP_BROKEN"; }
}
