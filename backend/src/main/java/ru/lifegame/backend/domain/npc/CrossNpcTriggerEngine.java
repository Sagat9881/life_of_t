package ru.lifegame.backend.domain.npc;

import ru.lifegame.backend.domain.npc.spec.ConditionSpec;

import java.util.*;

public class CrossNpcTriggerEngine {

    public record CrossNpcConditionSpec(String npcA, String npcB, String axis, String operator, int threshold) {}

    public record CrossNpcTrigger(
        String id,
        List<CrossNpcConditionSpec> conditions,
        String eventId,
        String description
    ) {}

    private final List<CrossNpcTrigger> triggers = new ArrayList<>();

    public void registerTrigger(CrossNpcTrigger trigger) {
        triggers.add(trigger);
    }

    public List<String> evaluate(NpcRelationshipGraph graph) {
        List<String> firedEventIds = new ArrayList<>();
        for (CrossNpcTrigger trigger : triggers) {
            boolean allMet = trigger.conditions().stream().allMatch(c -> {
                Optional<NpcRelationshipGraph.NpcEdge> edge = graph.getEdge(c.npcA(), c.npcB());
                if (edge.isEmpty()) return false;
                int val = edge.get().getAxis(c.axis());
                return switch (c.operator()) {
                    case "gte" -> val >= c.threshold();
                    case "lte" -> val <= c.threshold();
                    case "gt" -> val > c.threshold();
                    default -> false;
                };
            });
            if (allMet) firedEventIds.add(trigger.eventId());
        }
        return firedEventIds;
    }
}
