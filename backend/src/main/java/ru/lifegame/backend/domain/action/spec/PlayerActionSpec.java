package ru.lifegame.backend.domain.action.spec;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
        ActionFlags flags,
        Map<String, Integer> skillGains,
        JobEffects jobEffects,
        List<ExtraRelEffect> extraRelationshipEffects
) {
    public record TimeCostSkillModifier(String skillName, double reductionPerLevel, int minCost) {}

    public record StatEffects(int energy, int health, int stress, int mood, int money, int selfEsteem) {}

    public record ActionFlags(
            boolean rested,
            boolean worked,
            Set<String> interactedNpcs,
            boolean resetHouseholdDays
    ) {}

    public record JobEffects(int satisfaction, int burnoutRisk) {}

    public record ExtraRelEffect(String target, int closeness, int trust, int stability, int romance) {}
}
