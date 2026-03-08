package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipGraph;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipEdge;
import ru.lifegame.backend.domain.engine.spec.ConditionSpec;

import java.util.*;

public class CrossNpcTriggerEngine {

    public record CrossNpcTrigger(
        String id,
        String npcA,
        String npcB,
        List<ConditionSpec> conditions,
        String eventId
    ) {}

    private final List<CrossNpcTrigger> triggers = new ArrayList<>();

    public void registerTrigger(CrossNpcTrigger trigger) {
        triggers.add(trigger);
    }

    public List<String> checkTriggers(NpcRegistry registry, NpcRelationshipGraph graph) {
        List<String> firedEvents = new ArrayList<>();
        for (var trigger : triggers) {
            var npcA = registry.get(trigger.npcA());
            var npcB = registry.get(trigger.npcB());
            if (npcA.isEmpty() || npcB.isEmpty()) continue;
            var edge = graph.getEdge(trigger.npcA(), trigger.npcB());
            if (edge.isPresent() && allConditionsMet(trigger.conditions(), edge.get())) {
                firedEvents.add(trigger.eventId());
            }
        }
        return firedEvents;
    }

    private boolean allConditionsMet(List<ConditionSpec> conditions, NpcRelationshipEdge edge) {
        if (conditions == null || conditions.isEmpty()) return true;
        return conditions.stream().allMatch(c -> evaluateEdgeCondition(c, edge));
    }

    private boolean evaluateEdgeCondition(ConditionSpec c, NpcRelationshipEdge edge) {
        int actual = switch (c.target()) {
            case "respect" -> edge.respect();
            case "tension" -> edge.tension();
            case "familiarity" -> edge.familiarity();
            default -> 0;
        };
        return switch (c.operator()) {
            case "gte" -> actual >= c.intValue();
            case "lte" -> actual <= c.intValue();
            case "gt" -> actual > c.intValue();
            case "lt" -> actual < c.intValue();
            default -> false;
        };
    }
}
