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
            String edgeField,
            String operator,
            double threshold,
            String eventId
    ) {}

    public List<String> evaluate(NpcRegistry registry) {
        NpcRelationshipGraph graph = registry.relationshipGraph();
        List<String> triggeredEvents = new ArrayList<>();
        for (CrossNpcTrigger trigger : triggers) {
            Optional<NpcRelationshipEdge> edge = graph.getEdge(trigger.npcA(), trigger.npcB());
            edge.ifPresent(e -> {
                double val = switch (trigger.edgeField()) {
                    case "tension" -> e.tension();
                    case "respect" -> e.respect();
                    case "familiarity" -> e.familiarity();
                    default -> 0;
                };
                boolean met = switch (trigger.operator()) {
                    case "gte" -> val >= trigger.threshold();
                    case "lte" -> val <= trigger.threshold();
                    case "gt" -> val > trigger.threshold();
                    default -> false;
                };
                if (met) triggeredEvents.add(trigger.eventId());
            });
        }
        return triggeredEvents;
    }
}
