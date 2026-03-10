package ru.lifegame.backend.domain.action.spec;

import java.util.List;
import java.util.Map;

public record PlayerActionSpec(
        String code,
        String label,
        String description,
        String icon,
        String animationTrigger,
        int baseTimeCost,
        StatEffects stats,
        Map<String, Integer> skillGains,
        Map<String, Integer> relationshipChanges,
        Map<String, Integer> petMoodChanges,
        List<String> tags,
        List<String> timeSlots,
        List<String> locations
) {

    public record StatEffects(
            int energy,
            int health,
            int stress,
            int mood,
            int money,
            int selfEsteem
    ) {}
}
