package ru.lifegame.backend.domain.dto.content;

import java.util.List;
import java.util.Map;

/**
 * Quest definition for data-driven frontend.
 * Contains quest structure, steps, and rewards.
 */
public record QuestDefView(
    String id,
    String title,
    String description,
    String category,
    
    // Start conditions
    List<String> requiredTags,
    Map<String, Integer> minSkills,
    Integer minDay,
    
    // Quest structure
    List<QuestStepView> steps,
    
    // Rewards
    Integer moneyReward,
    Map<String, Integer> skillRewards,
    List<String> tagsGranted,
    String completionMessage
) {
    public record QuestStepView(
        String id,
        String description,
        String type, // "ACTION", "EVENT_CHOICE", "CONFLICT_RESOLUTION", "SKILL_CHECK"
        
        // Completion conditions
        List<String> requiredActions,
        String requiredEventChoice,
        String requiredConflictResolution,
        Map<String, Integer> requiredSkillChecks
    ) {}
}
