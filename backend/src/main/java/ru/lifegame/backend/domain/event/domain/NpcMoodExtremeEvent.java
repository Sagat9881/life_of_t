package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;

public record NpcMoodExtremeEvent(
        String sessionId,
        String npcId,
        String axis,
        int value,
        Instant timestamp
) implements DomainEvent {

    public NpcMoodExtremeEvent(String sessionId, String npcId, String axis, int value) {
        this(sessionId, npcId, axis, value, Instant.now());
    }

    @Override public String eventType() { return "NPC_MOOD_EXTREME"; }
}
