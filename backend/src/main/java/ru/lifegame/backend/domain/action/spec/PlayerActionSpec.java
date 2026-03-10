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
        List<String> locations,
        Flags flags,
        JobEffects jobEffects,
        List<ExtraRelEffect> extraRelationshipEffects
) {

    public record StatEffects(
            int energy,
            int health,
            int stress,
            int mood,
            int money,
            int selfEsteem
    ) {}

    public record Flags(
            boolean resetHouseholdDays
    ) {
        public static Flags defaults() {
            return new Flags(false);
        }
    }

    public record JobEffects(
            int satisfaction,
            int burnoutRisk
    ) {
        public static JobEffects none() {
            return new JobEffects(0, 0);
        }
    }

    public record ExtraRelEffect(
            String target,
            int closeness,
            int trust,
            int stability,
            int romance
    ) {}
}
