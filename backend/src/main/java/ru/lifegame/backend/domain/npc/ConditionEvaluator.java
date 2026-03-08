package ru.lifegame.backend.domain.npc;

import java.util.List;
import java.util.Set;

/**
 * Interprets ConditionSpec against live NPC state + game context.
 * Completely data-driven — no hardcoded NPC names or action IDs.
 */
public class ConditionEvaluator {

    /**
     * All conditions must pass (AND logic).
     */
    public boolean allMet(List<ConditionSpec> conditions, NpcInstance npc,
                          int currentDay, int currentHour, StatAccessor stats) {
        return conditions.stream().allMatch(c -> evaluate(c, npc, currentDay, currentHour, stats));
    }

    public boolean evaluate(ConditionSpec spec, NpcInstance npc,
                            int currentDay, int currentHour, StatAccessor stats) {
        return switch (spec.type()) {
            case "mood" -> evaluateMood(spec, npc);
            case "memory" -> evaluateMemory(spec, npc);
            case "schedule" -> evaluateSchedule(spec, npc, currentHour);
            case "stat" -> evaluateStat(spec, stats);
            case "day" -> compare(currentDay, spec.operator(), spec.value());
            case "trait" -> evaluateTrait(spec, npc);
            default -> false;
        };
    }

    private boolean evaluateMood(ConditionSpec spec, NpcInstance npc) {
        int axisValue = npc.mood().axis(spec.target());
        return compare(axisValue, spec.operator(), spec.value());
    }

    private boolean evaluateMemory(ConditionSpec spec, NpcInstance npc) {
        if (!npc.hasMemory()) return false;
        return switch (spec.target()) {
            case "work_obsession" -> npc.memory().detectWorkObsession(5);
            case "being_ignored" -> npc.memory().isBeingIgnored(Set.of(), spec.value());
            case "has_recent" -> npc.memory().hasRecentAction(spec.operator(), spec.value());
            default -> false;
        };
    }

    private boolean evaluateSchedule(ConditionSpec spec, NpcInstance npc, int hour) {
        return npc.schedule().isAvailable(hour);
    }

    private boolean evaluateStat(ConditionSpec spec, StatAccessor stats) {
        int statValue = stats.getStat(spec.target());
        return compare(statValue, spec.operator(), spec.value());
    }

    private boolean evaluateTrait(ConditionSpec spec, NpcInstance npc) {
        int traitValue = npc.spec().personalityTrait(spec.target());
        return compare(traitValue, spec.operator(), spec.value());
    }

    private boolean compare(int actual, String operator, int expected) {
        return switch (operator) {
            case "gte", ">=" -> actual >= expected;
            case "lte", "<=" -> actual <= expected;
            case "gt", ">" -> actual > expected;
            case "lt", "<" -> actual < expected;
            case "eq", "==" -> actual == expected;
            case "neq", "!=" -> actual != expected;
            case "mod" -> actual % expected == 0;
            default -> false;
        };
    }

    /**
     * Port interface: allows ConditionEvaluator to read player stats
     * without depending on concrete PlayerCharacter.
     */
    @FunctionalInterface
    public interface StatAccessor {
        int getStat(String statName);
    }
}
