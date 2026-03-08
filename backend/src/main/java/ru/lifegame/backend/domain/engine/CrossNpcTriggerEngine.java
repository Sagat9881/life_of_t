package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipGraph;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipEdge;
import ru.lifegame.backend.domain.npc.graph.CrossNpcConditionSpec;

import java.util.*;
import java.util.stream.Collectors;

public class CrossNpcTriggerEngine {

    public record CrossNpcTrigger(String id, String npcA, String npcB,
                                   List<CrossNpcConditionSpec> conditions,
                                   String eventId) {}

    private final List<CrossNpcTrigger> triggers;

    public CrossNpcTriggerEngine(List<CrossNpcTrigger> triggers) {
        this.triggers = triggers;
    }

    public List<String> evaluate(NpcRegistry registry, NpcRelationshipGraph graph, Map<String, Object> context) {
        List<String> firedEventIds = new ArrayList<>();
        for (CrossNpcTrigger trigger : triggers) {
            Optional<NpcRelationshipEdge> edge = graph.getEdge(trigger.npcA(), trigger.npcB());
            if (edge.isEmpty()) continue;
            boolean allMet = trigger.conditions().stream().allMatch(c ->
                    evaluateCondition(c, edge.get(), registry, context));
            if (allMet) firedEventIds.add(trigger.eventId());
        }
        return firedEventIds;
    }

    private boolean evaluateCondition(CrossNpcConditionSpec c, NpcRelationshipEdge edge,
                                      NpcRegistry registry, Map<String, Object> context) {
        double actual = switch (c.axis()) {
            case "tension" -> edge.tension();
            case "respect" -> edge.respect();
            case "familiarity" -> edge.familiarity();
            default -> 0;
        };
        double expected = c.threshold();
        return switch (c.operator()) {
            case "gte" -> actual >= expected;
            case "lte" -> actual <= expected;
            case "gt" -> actual > expected;
            case "lt" -> actual < expected;
            default -> false;
        };
    }
}
