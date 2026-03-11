package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;

public record NpcMoodExtremeEvent(
        String sessionId,
        String npcId,
        String axis,
        int value,
        String dominantMood,
        Instant timestamp
) implements DomainEvent {

    public NpcMoodExtremeEvent(String sessionId, String npcId,
                               String axis, int value, String dominantMood) {
        this(sessionId, npcId, axis, value, dominantMood, Instant.now());
    }

    @Override public String eventType() { return "NPC_MOOD_EXTREME"; }
}
