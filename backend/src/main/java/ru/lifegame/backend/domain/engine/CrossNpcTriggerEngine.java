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
        List<String> triggeredEvents = new ArrayList<>();
        NpcRelationshipGraph graph = registry.relationshipGraph();
        for (CrossNpcTrigger trigger : triggers) {
            Optional<NpcRelationshipEdge> edge = graph.getEdge(trigger.npcA(), trigger.npcB());
            if (edge.isEmpty()) continue;
            double val = switch (trigger.axis()) {
                case "tension" -> edge.get().tension();
                case "respect" -> edge.get().respect();
                case "familiarity" -> edge.get().familiarity();
                default -> 0;
            };
            boolean met = switch (trigger.operator()) {
                case "gte" -> val >= trigger.threshold();
                case "lte" -> val <= trigger.threshold();
                case "gt" -> val > trigger.threshold();
                default -> false;
            };
            if (met) triggeredEvents.add(trigger.eventId());
        }
        return triggeredEvents;
    }
}
