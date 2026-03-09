package ru.lifegame.backend.domain.dto.content;

import java.util.List;
import java.util.Map;

/**
 * Narrative event definition.
 * Frontend renders event UI from this data.
 */
public record EventDefView(
    String id,
    String title,
    String description,
    String category,
    
    // Trigger conditions
    List<String> requiredTags,
    Map<String, Integer> minSkills,
    Integer minDay,
    Integer maxDay,
    
    // Options available to player
    List<EventOptionView> options,
    
    // Meta
    Integer priority,
    Boolean repeatable
) {
    public record EventOptionView(
        String code,
        String text,
        
        // Visibility conditions
        Map<String, Integer> requiredSkills,
        List<String> requiredTags,
        
        // Effects
        Map<String, Integer> statEffects,
        Map<String, Integer> relationshipEffects,
        Map<String, Integer> skillGains,
        Integer moneyEffect,
        List<String> tagsGranted,
        List<String> tagsRemoved,
        
        // Outcome text
        String outcomeMessage
    ) {}
}
