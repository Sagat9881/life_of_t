package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.npc.graph.NpcRelationshipGraph;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipEdge;
import ru.lifegame.backend.domain.npc.spec.ConditionSpec;
import ru.lifegame.backend.domain.npc.engine.ConditionEvaluator;
import ru.lifegame.backend.domain.npc.engine.NpcRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CrossNpcTriggerEngine {

    private final List<CrossNpcTrigger> triggers;

    public CrossNpcTriggerEngine(List<CrossNpcTrigger> triggers) {
        this.triggers = triggers;
    }

    public record CrossNpcTrigger(
        String id,
        String npcA,
        String npcB,
        List<ConditionSpec> conditions,
        String resultEventId
    ) {}

    public List<String> evaluate(
            NpcRelationshipGraph graph,
            ru.lifegame.backend.domain.npc.engine.NpcRegistry npcRegistry,
            ru.lifegame.backend.domain.npc.engine.ConditionEvaluator evaluator,
            Map<String, Object> gameState
    ) {
        List<String> firedEventIds = new ArrayList<>();
        for (CrossNpcTrigger trigger : triggers) {
            NpcRelationshipEdge edge = graph.getEdge(trigger.npcA(), trigger.npcB());
            if (edge == null) continue;

            boolean allMet = true;
            for (ConditionSpec cond : trigger.conditions()) {
                if (!evaluateEdgeCondition(cond, edge)) {
                    allMet = false;
                    break;
                }
            }
            if (allMet) {
                firedEventIds.add(trigger.resultEventId());
            }
        }
        return firedEventIds;
    }

    private boolean evaluateEdgeCondition(ConditionSpec cond, NpcRelationshipEdge edge) {
        double val = switch (cond.target()) {
            case "tension" -> edge.tension();
            case "respect" -> edge.respect();
            case "familiarity" -> edge.familiarity();
            default -> 0;
        };
        double threshold = Double.parseDouble(cond.value());
        return switch (cond.operator()) {
            case "gte" -> val >= threshold;
            case "lte" -> val <= threshold;
            case "gt" -> val > threshold;
            case "lt" -> val < threshold;
            case "eq" -> val == threshold;
            default -> false;
        };
    }
}
