package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;

public record NpcActivityChangedEvent(
        String sessionId,
        String npcId,
        String oldActivity,
        String newActivity,
        String locationId,
        Instant timestamp
) implements DomainEvent {

    public NpcActivityChangedEvent(String sessionId, String npcId,
                                   String oldActivity, String newActivity,
                                   String locationId) {
        this(sessionId, npcId, oldActivity, newActivity, locationId, Instant.now());
    }

    @Override public String eventType() { return "NPC_ACTIVITY_CHANGED"; }
}
