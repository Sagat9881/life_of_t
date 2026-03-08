package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipGraph;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipEdge;

import java.util.*;

public class CrossNpcTriggerEngine {

    public record CrossNpcTrigger(String id, String npcA, String npcB,
                                   String axis, String operator, double threshold,
                                   String eventId) {}

    private final List<CrossNpcTrigger> triggers;

    public CrossNpcTriggerEngine(List<CrossNpcTrigger> triggers) {
        this.triggers = triggers != null ? triggers : List.of();
    }

    public List<String> check(NpcRegistry registry) {
        List<String> firedEvents = new ArrayList<>();
        NpcRelationshipGraph graph = registry.relationshipGraph();
        for (CrossNpcTrigger trigger : triggers) {
            Optional<NpcRelationshipEdge> edge = graph.getEdge(trigger.npcA(), trigger.npcB());
            edge.ifPresent(e -> {
                double val = switch (trigger.axis()) {
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
                if (met) firedEvents.add(trigger.eventId());
            });
        }
        return firedEvents;
    }
}
