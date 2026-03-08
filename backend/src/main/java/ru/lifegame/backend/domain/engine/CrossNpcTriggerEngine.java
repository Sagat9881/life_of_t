package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipGraph;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipEdge;
import ru.lifegame.backend.domain.npc.spec.ConditionSpec;
import ru.lifegame.backend.domain.npc.engine.ConditionEvaluator;

import java.util.*;

public class CrossNpcTriggerEngine {

    private final List<CrossNpcTrigger> triggers;

    public CrossNpcTriggerEngine(List<CrossNpcTrigger> triggers) {
        this.triggers = triggers != null ? triggers : List.of();
    }

    public record CrossNpcTrigger(
            String id,
            String npcA,
            String npcB,
            List<ConditionSpec> conditions,
            String eventId
    ) {}

    public List<String> evaluate(NpcRegistry registry, ConditionEvaluator evaluator) {
        List<String> firedEventIds = new ArrayList<>();
        NpcRelationshipGraph graph = registry.relationshipGraph();

        for (CrossNpcTrigger trigger : triggers) {
            Optional<NpcInstance> a = registry.get(trigger.npcA());
            Optional<NpcInstance> b = registry.get(trigger.npcB());
            if (a.isEmpty() || b.isEmpty()) continue;

            Optional<NpcRelationshipEdge> edge = graph.getEdge(trigger.npcA(), trigger.npcB());
            if (edge.isEmpty()) continue;

            boolean allMet = true;
            for (ConditionSpec cond : trigger.conditions()) {
                if (!evaluateEdgeCondition(cond, edge.get())) {
                    allMet = false;
                    break;
                }
            }
            if (allMet) {
                firedEventIds.add(trigger.eventId());
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
        return switch (cond.operator()) {
            case "gte" -> val >= cond.numericValue();
            case "lte" -> val <= cond.numericValue();
            case "gt" -> val > cond.numericValue();
            case "lt" -> val < cond.numericValue();
            default -> false;
        };
    }
}
