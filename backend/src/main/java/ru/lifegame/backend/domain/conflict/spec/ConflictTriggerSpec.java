package ru.lifegame.backend.domain.conflict.spec;

import java.util.List;

/**
 * Data-driven trigger specification.
 * Defines conditions and cooldown instead of hardcoded Java classes.
 */
public record ConflictTriggerSpec(
        List<TriggerCondition> conditions,
        int cooldownDays,
        String triggerMode      // "ALL" (all conditions), "ANY" (at least one)
) {
}
