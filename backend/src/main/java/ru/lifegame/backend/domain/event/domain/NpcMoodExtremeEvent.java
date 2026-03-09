package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;

/**
 * Published when an NPC's mood reaches extreme state (any axis >80 or <20).
 * Frontend can use this to show mood-related toasts or animations.
 */
public record NpcMoodExtremeEvent(
        String sessionId,
        String npcId,
        String axis,
        int value,
        String dominantMood
) implements DomainEvent {
    @Override public Instant timestamp() { return Instant.now(); }
    @Override public String eventType() { return "NPC_MOOD_EXTREME"; }
}
