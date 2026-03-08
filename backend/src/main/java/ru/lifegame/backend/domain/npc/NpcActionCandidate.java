package ru.lifegame.backend.domain.npc;

import ru.lifegame.backend.domain.event.game.EventOption;
import ru.lifegame.backend.domain.event.game.GameEventType;

import java.util.List;

/**
 * A candidate action that an NPC might choose to initiate.
 * Scored by BehaviorEngine using utility functions.
 */
public record NpcActionCandidate(
    String id,
    String title,
    String description,
    GameEventType eventType,
    List<EventOption> playerOptions,
    double baseScore,
    NpcActionCondition condition
) {
    @FunctionalInterface
    public interface NpcActionCondition {
        boolean isMet(NpcProfile profile, int currentDay, int currentHour);
    }

    public double score(NpcProfile profile, int currentDay, int currentHour) {
        if (!condition.isMet(profile, currentDay, currentHour)) return -1;
        double moodFactor = profile.mood().urgencyScore() / 50.0;
        return baseScore + moodFactor;
    }
}