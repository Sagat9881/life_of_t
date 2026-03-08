package ru.lifegame.backend.domain.npc.runtime;

import ru.lifegame.backend.domain.npc.graph.NpcRelationshipGraph;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipEdge;

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
                Optional<NpcRelationshipEdge> edge = graph.getEdge(c.npcA(), c.npcB());
                if (edge.isEmpty()) return false;
                int val = getAxis(edge.get(), c.axis());
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

    private int getAxis(NpcRelationshipEdge edge, String axis) {
        return switch (axis) {
            case "respect" -> edge.respect();
            case "tension" -> edge.tension();
            case "familiarity" -> edge.familiarity();
            default -> 0;
        };
    }
}
