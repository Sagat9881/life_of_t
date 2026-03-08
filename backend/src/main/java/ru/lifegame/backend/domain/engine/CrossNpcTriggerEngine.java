package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipGraph;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipEdge;
import ru.lifegame.backend.domain.engine.spec.ConditionSpec;

import java.util.*;

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

    public record TriggeredCrossEvent(
            String triggerId,
            String eventId,
            String npcA,
            String npcB
    ) {}

    public List<TriggeredCrossEvent> evaluate(NpcRegistry registry) {
        List<TriggeredCrossEvent> results = new ArrayList<>();
        NpcRelationshipGraph graph = registry.relationshipGraph();

        for (CrossNpcTrigger trigger : triggers) {
            Optional<NpcRelationshipEdge> edge = graph.getEdge(trigger.npcA(), trigger.npcB());
            if (edge.isEmpty()) continue;

            double value = switch (trigger.axis()) {
                case "tension" -> edge.get().tension();
                case "respect" -> edge.get().respect();
                case "familiarity" -> edge.get().familiarity();
                default -> 0;
            };

            boolean met = switch (trigger.operator()) {
                case "gte" -> value >= trigger.threshold();
                case "lte" -> value <= trigger.threshold();
                case "gt" -> value > trigger.threshold();
                case "lt" -> value < trigger.threshold();
                default -> false;
            };

            if (met) {
                results.add(new TriggeredCrossEvent(trigger.id(), trigger.eventId(), trigger.npcA(), trigger.npcB()));
            }
        }
        return results;
    }
}
