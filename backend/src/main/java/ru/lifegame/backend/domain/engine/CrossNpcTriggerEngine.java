package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipGraph;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipEdge;
import ru.lifegame.backend.domain.npc.graph.CrossNpcConditionSpec;

import java.util.*;
import java.util.stream.Collectors;

public class CrossNpcTriggerEngine {

    private final NpcRegistry registry;
    private final List<CrossNpcTrigger> triggers;

    public CrossNpcTriggerEngine(NpcRegistry registry, List<CrossNpcTrigger> triggers) {
        this.registry = registry;
        this.triggers = triggers != null ? triggers : List.of();
    }

    public record CrossNpcTrigger(String id, String npcA, String npcB, List<CrossNpcConditionSpec> conditions, String eventId) {}

    public List<String> evaluate(Map<String, Object> gameContext) {
        NpcRelationshipGraph graph = registry.relationshipGraph();
        List<String> firedEventIds = new ArrayList<>();

        for (CrossNpcTrigger trigger : triggers) {
            Optional<NpcRelationshipEdge> edge = graph.getEdge(trigger.npcA(), trigger.npcB());
            if (edge.isEmpty()) continue;

            boolean allMet = trigger.conditions().stream().allMatch(cond -> {
                double actual = switch (cond.axis()) {
                    case "tension" -> edge.get().tension();
                    case "respect" -> edge.get().respect();
                    case "familiarity" -> edge.get().familiarity();
                    default -> 0;
                };
                double expected = cond.threshold();
                return switch (cond.operator()) {
                    case "gte" -> actual >= expected;
                    case "lte" -> actual <= expected;
                    case "gt" -> actual > expected;
                    case "lt" -> actual < expected;
                    default -> false;
                };
            });

            if (allMet) {
                firedEventIds.add(trigger.eventId());
            }
        }

        return firedEventIds;
    }
}
