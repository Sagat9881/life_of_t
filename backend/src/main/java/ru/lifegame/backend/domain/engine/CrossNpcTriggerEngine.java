package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipGraph;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipEdge;

import java.util.*;

public class CrossNpcTriggerEngine {

    public record CrossNpcTrigger(String id, String npcA, String npcB, String axis, String operator, int threshold, String eventId) {}

    private final List<CrossNpcTrigger> triggers = new ArrayList<>();

    public void registerTrigger(CrossNpcTrigger trigger) {
        triggers.add(trigger);
    }

    public List<String> checkTriggers(NpcRegistry registry) {
        List<String> firedEventIds = new ArrayList<>();
        NpcRelationshipGraph graph = registry.relationshipGraph();

        for (CrossNpcTrigger t : triggers) {
            Optional<NpcRelationshipEdge> edge = graph.getEdge(t.npcA(), t.npcB());
            if (edge.isEmpty()) continue;

            int val = switch (t.axis()) {
                case "tension" -> edge.get().tension();
                case "respect" -> edge.get().respect();
                case "familiarity" -> edge.get().familiarity();
                default -> 0;
            };

            boolean met = switch (t.operator()) {
                case "gte" -> val >= t.threshold();
                case "lte" -> val <= t.threshold();
                case "gt" -> val > t.threshold();
                case "lt" -> val < t.threshold();
                default -> false;
            };

            if (met) firedEventIds.add(t.eventId());
        }
        return firedEventIds;
    }
}
