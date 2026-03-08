package ru.lifegame.backend.domain.npc.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Evaluates cross-NPC conditions against the relationship graph.
 * All conditions loaded from XML — engine has zero hardcoded NPC knowledge.
 *
 * Called during endDay() to check for cross-NPC triggered events.
 */
public class CrossNpcTriggerEngine {

    private final List<CrossNpcConditionSpec> conditions;

    public CrossNpcTriggerEngine(List<CrossNpcConditionSpec> conditions) {
        this.conditions = new ArrayList<>(conditions);
    }

    /**
     * Check all cross-NPC conditions, return IDs of events that should fire.
     */
    public List<String> evaluateTriggers(NpcRelationshipGraph graph) {
        List<String> triggeredEventIds = new ArrayList<>();

        for (CrossNpcConditionSpec condition : conditions) {
            int actual = getAxisValue(graph, condition);
            if (matches(actual, condition.operator(), condition.value())) {
                triggeredEventIds.add(condition.resultEventId());
            }
        }

        return triggeredEventIds;
    }

    private int getAxisValue(NpcRelationshipGraph graph, CrossNpcConditionSpec condition) {
        return switch (condition.axis()) {
            case "tension" -> graph.getTension(condition.npcA(), condition.npcB());
            case "respect" -> graph.getRespect(condition.npcA(), condition.npcB());
            case "familiarity" -> graph.getFamiliarity(condition.npcA(), condition.npcB());
            default -> 0;
        };
    }

    private boolean matches(int actual, String operator, int expected) {
        return switch (operator) {
            case "gte" -> actual >= expected;
            case "lte" -> actual <= expected;
            case "eq" -> actual == expected;
            case "gt" -> actual > expected;
            case "lt" -> actual < expected;
            default -> false;
        };
    }
}
