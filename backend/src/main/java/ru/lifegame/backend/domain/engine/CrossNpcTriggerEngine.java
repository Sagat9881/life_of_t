package ru.lifegame.backend.application.engine;

import com.sagat.life_of_t.domain.engine.runtime.NpcInstance;
import com.sagat.life_of_t.domain.engine.runtime.NpcRelationshipGraph;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks cross-NPC conditions and generates events.
 * E.g., tension between two NPCs exceeds threshold → conflict event.
 * All thresholds are configurable, no hardcoded NPC names.
 */
public class CrossNpcTriggerEngine {
    private static final int TENSION_CONFLICT_THRESHOLD = 70;
    private static final int JEALOUSY_LONELINESS_THRESHOLD = 60;

    private final NpcRegistry registry;

    public CrossNpcTriggerEngine(NpcRegistry registry) {
        this.registry = registry;
    }

    public record CrossNpcEvent(String type, String npcA, String npcB, String description) {}

    public List<CrossNpcEvent> checkTriggers() {
        List<CrossNpcEvent> events = new ArrayList<>();
        NpcRelationshipGraph graph = registry.relationshipGraph();

        List<NpcInstance> named = registry.namedNpcs();
        for (int i = 0; i < named.size(); i++) {
            for (int j = i + 1; j < named.size(); j++) {
                String a = named.get(i).entityId();
                String b = named.get(j).entityId();
                var relation = graph.getRelation(a, b);

                if (relation.tension() >= TENSION_CONFLICT_THRESHOLD) {
                    events.add(new CrossNpcEvent(
                            "NPC_CONFLICT", a, b,
                            "Tension between " + a + " and " + b + " reached critical level"
                    ));
                }

                if (relation.respect() < 20 && relation.familiarity() > 50) {
                    events.add(new CrossNpcEvent(
                            "NPC_RESENTMENT", a, b,
                            a + " resents " + b + " despite familiarity"
                    ));
                }
            }
        }

        for (NpcInstance npc : named) {
            if (npc.mood().loneliness() >= JEALOUSY_LONELINESS_THRESHOLD && npc.hasMemory()) {
                if (npc.memory().isBeingIgnored("INTERACT_" + npc.entityId().toUpperCase(), 3)) {
                    events.add(new CrossNpcEvent(
                            "NPC_JEALOUSY", npc.entityId(), "player",
                            npc.entityId() + " feels ignored and jealous"
                    ));
                }
            }
        }

        return events;
    }
}
