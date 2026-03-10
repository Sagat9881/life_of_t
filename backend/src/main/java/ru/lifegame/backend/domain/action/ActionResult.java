package ru.lifegame.backend.domain.action;

import ru.lifegame.backend.domain.model.stats.StatChanges;
import java.util.Map;
import java.util.Set;

public record ActionResult(
        ActionType actionType,
        int timeCost,
        String description,
        StatChanges statChanges,
        Map<String, Integer> relationshipChanges,
        Map<String, Integer> petMoodChanges,
        boolean rested,
        boolean workedToday,
        Set<String> interactedNpcs
) {
    public boolean interactedWith(String npcId) {
        return interactedNpcs != null && interactedNpcs.contains(npcId.toUpperCase());
    }
}
