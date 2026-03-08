package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipGraph;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipEdge;

import java.util.*;

public class CrossNpcTriggerEngine {

    private final NpcRegistry registry;

    public CrossNpcTriggerEngine(NpcRegistry registry) {
        this.registry = registry;
    }

    public List<TriggeredCrossEvent> evaluate(Map<String, Object> context) {
        List<TriggeredCrossEvent> events = new ArrayList<>();
        NpcRelationshipGraph graph = registry.relationshipGraph();

        for (NpcRelationshipEdge edge : graph.allEdges()) {
            if (edge.tension() > 70) {
                events.add(new TriggeredCrossEvent(
                    edge.npcIdA() + "_" + edge.npcIdB() + "_tension",
                    "NPC tension conflict",
                    edge.npcIdA(), edge.npcIdB(), edge.tension()
                ));
            }
        }
        return events;
    }

    public record TriggeredCrossEvent(String eventId, String description,
                                      String npcA, String npcB, double severity) {}
}
