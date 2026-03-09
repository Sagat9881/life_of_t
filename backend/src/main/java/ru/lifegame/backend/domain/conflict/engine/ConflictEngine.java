package ru.lifegame.backend.domain.conflict.engine;

import ru.lifegame.backend.domain.conflict.core.Conflict;
import ru.lifegame.backend.domain.conflict.spec.ConflictSpec;
import ru.lifegame.backend.domain.conflict.spec.TriggerCondition;
import ru.lifegame.backend.domain.model.character.PlayerCharacter;
import ru.lifegame.backend.domain.model.relationship.Relationships;
import ru.lifegame.backend.domain.model.session.GameTime;

import java.util.*;

/**
 * Data-driven conflict engine.
 * Replaces hardcoded trigger classes (BurnoutTrigger, GuiltTrigger, etc.) with XML-based evaluation.
 */
public class ConflictEngine {

    private final List<ConflictSpec> conflictSpecs;
    private final UniversalConditionEvaluator conditionEvaluator;
    private final Map<String, Integer> lastTriggeredDay;  // conflict ID -> day when last triggered

    public ConflictEngine(List<ConflictSpec> conflictSpecs) {
        this.conflictSpecs = conflictSpecs;
        this.conditionEvaluator = new UniversalConditionEvaluator();
        this.lastTriggeredDay = new HashMap<>();
    }

    /**
     * Evaluate all conflict triggers and return newly triggered conflicts.
     * Called at end of day.
     */
    public List<Conflict> evaluateTriggers(
            PlayerCharacter player,
            Relationships relationships,
            GameTime time,
            Map<String, Object> additionalContext
    ) {
        List<Conflict> newConflicts = new ArrayList<>();

        Map<String, Object> context = buildContext(player, relationships, time, additionalContext);

        for (ConflictSpec spec : conflictSpecs) {
            if (shouldTrigger(spec, context, time.day())) {
                Conflict conflict = Conflict.fromSpec(spec);
                newConflicts.add(conflict);
                lastTriggeredDay.put(spec.id(), time.day());
            }
        }

        return newConflicts;
    }

    private boolean shouldTrigger(ConflictSpec spec, Map<String, Object> context, int currentDay) {
        // Check cooldown
        Integer lastDay = lastTriggeredDay.get(spec.id());
        if (lastDay != null) {
            int daysSince = currentDay - lastDay;
            if (daysSince < spec.trigger().cooldownDays()) {
                return false;  // still on cooldown
            }
        }

        // Evaluate conditions
        List<TriggerCondition> conditions = spec.trigger().conditions();
        if (conditions.isEmpty()) return false;

        boolean allConditionsMet = conditions.stream()
                .allMatch(cond -> conditionEvaluator.evaluate(cond, context));

        boolean anyConditionMet = conditions.stream()
                .anyMatch(cond -> conditionEvaluator.evaluate(cond, context));

        return spec.trigger().triggerMode().equals("ALL") ? allConditionsMet : anyConditionMet;
    }

    private Map<String, Object> buildContext(
            PlayerCharacter player,
            Relationships relationships,
            GameTime time,
            Map<String, Object> additionalContext
    ) {
        Map<String, Object> context = new HashMap<>();
        context.put("player", player);
        context.put("relationships", relationships);
        context.put("time", time);

        if (additionalContext != null) {
            context.putAll(additionalContext);
        }

        return context;
    }

    /**
     * Get all loaded conflict specs (for debugging/testing).
     */
    public List<ConflictSpec> getConflictSpecs() {
        return List.copyOf(conflictSpecs);
    }
}
