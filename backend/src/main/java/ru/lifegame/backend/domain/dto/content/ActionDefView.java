package ru.lifegame.backend.domain.dto.content;

import java.util.List;
import java.util.Map;

/**
 * Definition of a game action for data-driven frontend.
 * Contains all metadata needed to render action UI without hardcoding.
 */
public record ActionDefView(
    String code,
    String title,
    String description,
    List<String> tags,
    
    // Requirements
    Integer energyCost,
    Integer minEnergy,
    Map<String, Integer> requiredSkills,
    List<String> requiredTags,
    List<String> forbiddenTags,
    
    // Effects (base values)
    Map<String, Integer> statEffects,
    Map<String, Integer> skillGains,
    Integer moneyGain,
    Integer durationMinutes,
    
    // Animation/visual hints
    String animationTrigger,
    String iconName,
    
    // Availability conditions
    List<String> availableTimeOfDay,
    List<String> availableLocations,
    
    // Conflict/quest hints
    List<String> potentialConflictTypes,
    List<String> relatedQuestIds
) {}
