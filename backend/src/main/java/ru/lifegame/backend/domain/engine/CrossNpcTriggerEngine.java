package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipGraph;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipEdge;
import ru.lifegame.backend.domain.npc.graph.CrossNpcConditionSpec;

import java.util.*;
import java.util.stream.Collectors;

public class CrossNpcTriggerEngine {

    public record CrossNpcTrigger(
            String id,
            String npcA,
            String npcB,
            List<CrossNpcConditionSpec> conditions,
            String eventId
    ) {}

    private final List<CrossNpcTrigger> triggers;

    public CrossNpcTriggerEngine(List<CrossNpcTrigger> triggers) {
        this.triggers = triggers;
    }

    public List<String> evaluate(NpcRegistry registry, NpcRelationshipGraph graph, Map<String, Object> context) {
        List<String> firedEvents = new ArrayList<>();
        for (CrossNpcTrigger trigger : triggers) {
            NpcRelationshipEdge edge = graph.getEdge(trigger.npcA(), trigger.npcB());
            if (edge == null) continue;
            boolean allMet = trigger.conditions().stream().allMatch(c -> evaluateCondition(c, edge, registry, context));
            if (allMet) firedEvents.add(trigger.eventId());
        }
        return firedEvents;
    }

    private boolean evaluateCondition(CrossNpcConditionSpec c, NpcRelationshipEdge edge, NpcRegistry registry, Map<String, Object> context) {
        double value = switch (c.target()) {
            case "respect" -> edge.respect();
            case "tension" -> edge.tension();
            case "familiarity" -> edge.familiarity();
            default -> {
                NpcInstance npc = registry.get(c.target());
                yield npc != null ? npc.mood().getAxis(c.axis()) : 0;
            }
        };
        double threshold = c.value();
        return switch (c.operator()) {
            case "gte" -> value >= threshold;
            case "lte" -> value <= threshold;
            case "gt" -> value > threshold;
            case "lt" -> value < threshold;
            default -> false;
        };
    }
}
