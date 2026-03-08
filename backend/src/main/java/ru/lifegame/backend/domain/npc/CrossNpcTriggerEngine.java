package ru.lifegame.backend.domain.npc;

import com.life_of_t.domain.npc.NpcRelationshipGraph;
import com.life_of_t.domain.npc.spec.ConditionSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Evaluates cross-NPC triggers: conditions that involve relationships between NPCs.
 * E.g., "if player ignores husband AND visits father often => husband jealousy event".
 * Trigger definitions come from XML — engine only knows abstract conditions.
 */
public class CrossNpcTriggerEngine {

    /**
     * A cross-NPC trigger loaded from XML.
     */
    public record CrossNpcTrigger(
            String triggerId,
            String sourceNpc,
            String targetNpc,
            String relAxis,
            String operator,
            int threshold,
            List<ConditionSpec> additionalConditions,
            String resultEventId,
            Map<String, Integer> relAdjustments
    ) {}

    private final List<CrossNpcTrigger> triggers;

    public CrossNpcTriggerEngine(List<CrossNpcTrigger> triggers) {
        this.triggers = List.copyOf(triggers);
    }

    public static CrossNpcTriggerEngine empty() {
        return new CrossNpcTriggerEngine(List.of());
    }

    /**
     * Check all cross-NPC triggers against current state.
     * Returns list of triggered event IDs.
     */
    public List<TriggeredCrossEvent> evaluate(
            NpcRelationshipGraph graph,
            NpcRegistry registry,
            ConditionEvaluator condEvaluator,
            Object sessionContext
    ) {
        List<TriggeredCrossEvent> results = new ArrayList<>();

        for (CrossNpcTrigger trigger : triggers) {
            var edge = graph.getEdge(trigger.sourceNpc(), trigger.targetNpc());
            if (edge.isEmpty()) continue;

            int axisValue = edge.get().getAxis(trigger.relAxis());
            boolean axisMet = evaluateOperator(axisValue, trigger.operator(), trigger.threshold());
            if (!axisMet) continue;

            // Check additional conditions (mood, memory, etc.)
            boolean allConditionsMet = trigger.additionalConditions().stream()
                    .allMatch(cond -> {
                        var npc = registry.get(trigger.sourceNpc());
                        return npc.map(n -> condEvaluator.evaluate(cond, n, null))
                                .orElse(false);
                    });

            if (allConditionsMet) {
                // Apply relationship adjustments
                trigger.relAdjustments().forEach((axis, delta) ->
                        graph.adjustRelationship(trigger.sourceNpc(), trigger.targetNpc(), axis, delta));

                results.add(new TriggeredCrossEvent(
                        trigger.triggerId(),
                        trigger.resultEventId(),
                        trigger.sourceNpc(),
                        trigger.targetNpc()
                ));
            }
        }

        return results;
    }

    private boolean evaluateOperator(int value, String operator, int threshold) {
        return switch (operator) {
            case "gte" -> value >= threshold;
            case "gt" -> value > threshold;
            case "lte" -> value <= threshold;
            case "lt" -> value < threshold;
            case "eq" -> value == threshold;
            default -> false;
        };
    }

    public record TriggeredCrossEvent(
            String triggerId,
            String eventId,
            String sourceNpc,
            String targetNpc
    ) {}
}
