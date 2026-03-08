package ru.lifegame.backend.domain.action.spec;

import java.util.List;
import java.util.Map;

/**
 * Data-driven specification for a player action, loaded from XML.
 * Replaces hardcoded GoToWorkAction, DateWithHusbandAction, etc.
 */
public record PlayerActionSpec(
        String id,
        String code,
        String label,
        String description,
        String resultTextTemplate,
        int baseTimeCost,
        TimeCostSkillModifier timeCostSkillModifier,
        StatEffects stats,
        Map<String, Integer> relationshipChanges,
        Map<String, Integer> petMoodChanges,
        ActionFlags flags
) {
    public record TimeCostSkillModifier(String skillName, double reductionPerLevel, int minCost) {}

    public record StatEffects(int energy, int health, int stress, int mood, int money, int selfEsteem) {}

    public record ActionFlags(
            boolean rested,
            boolean worked,
            boolean interactedHusband,
            boolean interactedFather
    ) {}
}
