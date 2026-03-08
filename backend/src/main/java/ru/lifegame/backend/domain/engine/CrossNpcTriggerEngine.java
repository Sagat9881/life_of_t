package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipGraph;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipEdge;
import ru.lifegame.backend.domain.npc.graph.CrossNpcConditionSpec;

import java.util.*;

public class CrossNpcTriggerEngine {

    private final List<CrossNpcTrigger> triggers;

    public CrossNpcTriggerEngine(List<CrossNpcTrigger> triggers) {
        this.triggers = triggers;
    }

    public record CrossNpcTrigger(
            String id,
            String npcA,
            String npcB,
            List<CrossNpcConditionSpec> conditions,
            String eventId,
            String description
    ) {}

    public List<String> evaluate(NpcRegistry registry, Map<String, Object> context) {
        List<String> triggeredEvents = new ArrayList<>();
        NpcRelationshipGraph graph = registry.relationshipGraph();

        for (CrossNpcTrigger trigger : triggers) {
            boolean allMet = true;
            for (CrossNpcConditionSpec cond : trigger.conditions()) {
                if (!evaluateCondition(cond, graph, registry, context)) {
                    allMet = false;
                    break;
                }
            }
            if (allMet) {
                triggeredEvents.add(trigger.eventId());
            }
        }
        return triggeredEvents;
    }

    private boolean evaluateCondition(CrossNpcConditionSpec cond, NpcRelationshipGraph graph, NpcRegistry registry, Map<String, Object> context) {
        if ("relationship".equals(cond.type())) {
            Optional<NpcRelationshipEdge> edge = graph.getEdge(cond.npcA(), cond.npcB());
            if (edge.isEmpty()) return false;
            double val = switch (cond.axis()) {
                case "tension" -> edge.get().tension();
                case "respect" -> edge.get().respect();
                case "familiarity" -> edge.get().familiarity();
                default -> 0;
            };
            double threshold = Double.parseDouble(cond.value());
            return switch (cond.operator()) {
                case "gte" -> val >= threshold;
                case "lte" -> val <= threshold;
                case "gt" -> val > threshold;
                default -> false;
            };
        }
        return false;
    }
}
