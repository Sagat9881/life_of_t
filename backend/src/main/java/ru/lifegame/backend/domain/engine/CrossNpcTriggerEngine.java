package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipGraph;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipEdge;
import ru.lifegame.backend.domain.engine.spec.ConditionSpec;
import ru.lifegame.backend.domain.npc.engine.ConditionEvaluator;

import java.util.*;

public class CrossNpcTriggerEngine {

    public record CrossNpcTrigger(
            String id,
            String npcA,
            String npcB,
            String edgeField,
            String operator,
            double threshold,
            String eventId,
            List<ConditionSpec> additionalConditions
    ) {}

    private final List<CrossNpcTrigger> triggers;

    public CrossNpcTriggerEngine(List<CrossNpcTrigger> triggers) {
        this.triggers = triggers;
    }

    public List<String> evaluate(NpcRegistry registry, Map<String, Object> gameContext) {
        List<String> triggeredEventIds = new ArrayList<>();
        NpcRelationshipGraph graph = registry.relationshipGraph();

        for (CrossNpcTrigger trigger : triggers) {
            NpcRelationshipEdge edge = graph.getEdge(trigger.npcA(), trigger.npcB());
            if (edge == null) continue;

            double value = switch (trigger.edgeField()) {
                case "respect" -> edge.respect();
                case "tension" -> edge.tension();
                case "familiarity" -> edge.familiarity();
                default -> 0;
            };

            boolean met = switch (trigger.operator()) {
                case "gte" -> value >= trigger.threshold();
                case "lte" -> value <= trigger.threshold();
                case "gt" -> value > trigger.threshold();
                case "lt" -> value < trigger.threshold();
                default -> false;
            };

            if (met) {
                triggeredEventIds.add(trigger.eventId());
            }
        }
        return triggeredEventIds;
    }
}
