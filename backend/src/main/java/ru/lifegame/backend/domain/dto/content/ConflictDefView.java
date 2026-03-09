package ru.lifegame.backend.domain.dto.content;

import java.util.List;
import java.util.Map;

/**
 * Definition of a conflict type with available tactics.
 * Frontend uses this to render conflict resolution UI dynamically.
 */
public record ConflictDefView(
    String type,
    String title,
    String description,
    List<TacticDefView> tactics,
    Integer baseStressPoints
) {
    public record TacticDefView(
        String code,
        String title,
        String description,
        
        // Skill requirements
        Map<String, Integer> skillRequirements,
        
        // Effects on conflict resolution
        Integer stressReduction,
        Map<String, Integer> relationshipEffects,
        Map<String, Integer> skillGains,
        
        // Success probability modifiers
        Integer baseSuccessChance,
        Map<String, Integer> skillSuccessModifiers
    ) {}
}
