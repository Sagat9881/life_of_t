package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipGraph;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipEdge;
import ru.lifegame.backend.domain.engine.spec.ConditionSpec;

import java.util.*;

public class CrossNpcTriggerEngine {

    public record CrossNpcTrigger(String id, String npcA, String npcB, List<ConditionSpec> conditions, String eventId) {}

    private final List<CrossNpcTrigger> triggers = new ArrayList<>();

    public void registerTrigger(CrossNpcTrigger trigger) {
        triggers.add(trigger);
    }

    public List<String> checkTriggers(NpcRegistry registry, NpcRelationshipGraph graph) {
        List<String> firedEventIds = new ArrayList<>();
        for (CrossNpcTrigger trigger : triggers) {
            Optional<NpcRelationshipEdge> edge = graph.getEdge(trigger.npcA(), trigger.npcB());
            if (edge.isPresent()) {
                boolean allMet = trigger.conditions().stream().allMatch(c -> evaluateEdgeCondition(c, edge.get()));
                if (allMet) firedEventIds.add(trigger.eventId());
            }
        }
        return firedEventIds;
    }

    private boolean evaluateEdgeCondition(ConditionSpec condition, NpcRelationshipEdge edge) {
        int actual = switch (condition.target()) {
            case "tension" -> edge.tension();
            case "respect" -> edge.respect();
            case "familiarity" -> edge.familiarity();
            default -> 0;
        };
        return switch (condition.operator()) {
            case "gte" -> actual >= condition.intValue();
            case "lte" -> actual <= condition.intValue();
            case "gt" -> actual > condition.intValue();
            case "lt" -> actual < condition.intValue();
            default -> false;
        };
    }
}
