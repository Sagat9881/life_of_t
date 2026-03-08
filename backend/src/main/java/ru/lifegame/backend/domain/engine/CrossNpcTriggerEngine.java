package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipGraph;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipEdge;

import java.util.*;
import java.util.stream.Collectors;

public class CrossNpcTriggerEngine {

    private final List<CrossNpcTrigger> triggers;

    public CrossNpcTriggerEngine(List<CrossNpcTrigger> triggers) {
        this.triggers = triggers;
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
        return triggers.stream()
                .filter(t -> {
                    Optional<NpcRelationshipEdge> edge = graph.getEdge(t.npcA(), t.npcB());
                    if (edge.isEmpty()) return false;
                    double value = switch (t.axis()) {
                        case "tension" -> edge.get().tension();
                        case "respect" -> edge.get().respect();
                        case "familiarity" -> edge.get().familiarity();
                        default -> 0;
                    };
                    return switch (t.operator()) {
                        case "gte" -> value >= t.threshold();
                        case "lte" -> value <= t.threshold();
                        case "gt" -> value > t.threshold();
                        case "lt" -> value < t.threshold();
                        default -> false;
                    };
                })
                .map(CrossNpcTrigger::eventId)
                .collect(Collectors.toList());
    }
}
