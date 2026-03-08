package ru.lifegame.backend.domain.npc;

import ru.lifegame.backend.domain.npc.graph.NpcRelationshipGraph;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipEdge;
import ru.lifegame.backend.domain.npc.spec.ConditionSpec;
import ru.lifegame.backend.domain.npc.engine.NpcRegistry;
import ru.lifegame.backend.domain.npc.engine.ConditionEvaluator;

import java.util.*;

public class CrossNpcTriggerEngine {

    public record CrossNpcCondition(
        String type,
        String npcA,
        String npcB,
        String axis,
        String operator,
        int threshold
    ) {}

    public record CrossNpcTrigger(
        String id,
        String eventId,
        List<CrossNpcCondition> conditions
    ) {}

    private final List<CrossNpcTrigger> triggers = new ArrayList<>();

    public void registerTrigger(CrossNpcTrigger trigger) {
        triggers.add(trigger);
    }

    public List<String> checkTriggers(NpcRelationshipGraph graph) {
        List<String> firedEventIds = new ArrayList<>();
        for (CrossNpcTrigger trigger : triggers) {
            boolean allMet = trigger.conditions().stream().allMatch(c -> {
                Optional<NpcRelationshipEdge> edge = graph.getEdge(c.npcA(), c.npcB());
                if (edge.isEmpty()) return false;
                int actual = switch (c.axis()) {
                    case "tension" -> edge.get().tension();
                    case "respect" -> edge.get().respect();
                    case "familiarity" -> edge.get().familiarity();
                    default -> 0;
                };
                return switch (c.operator()) {
                    case "gte" -> actual >= c.threshold();
                    case "lte" -> actual <= c.threshold();
                    case "gt" -> actual > c.threshold();
                    case "lt" -> actual < c.threshold();
                    default -> false;
                };
            });
            if (allMet) firedEventIds.add(trigger.eventId());
        }
        return firedEventIds;
    }
}
