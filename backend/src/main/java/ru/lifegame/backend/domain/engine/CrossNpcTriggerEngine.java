package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipGraph;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipEdge;

import java.util.*;

public class CrossNpcTriggerEngine {

    private final List<CrossNpcTrigger> triggers;

    public CrossNpcTriggerEngine(List<CrossNpcTrigger> triggers) {
        this.triggers = triggers != null ? triggers : List.of();
    }

    public record CrossNpcTrigger(
            String id,
            String npcA,
            String npcB,
            String axis,
            String operator,
            double threshold,
            String eventId
    ) {}

    public List<String> evaluate(NpcRegistry registry) {
        NpcRelationshipGraph graph = registry.relationshipGraph();
        if (graph == null) return List.of();

        List<String> firedEvents = new ArrayList<>();
        for (CrossNpcTrigger trigger : triggers) {
            Optional<NpcRelationshipEdge> edge = graph.getEdge(trigger.npcA(), trigger.npcB());
            edge.ifPresent(e -> {
                double value = switch (trigger.axis()) {
                    case "tension" -> e.tension();
                    case "respect" -> e.respect();
                    case "familiarity" -> e.familiarity();
                    default -> 0;
                };
                boolean met = switch (trigger.operator()) {
                    case "gte" -> value >= trigger.threshold();
                    case "lte" -> value <= trigger.threshold();
                    case "gt" -> value > trigger.threshold();
                    case "lt" -> value < trigger.threshold();
                    default -> false;
                };
                if (met) firedEvents.add(trigger.eventId());
            });
        }
        return firedEvents;
    }
}
