package ru.lifegame.backend.application.engine.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Bidirectional relationship graph between NPC pairs.
 * Tracks respect, tension, familiarity for cross-NPC interactions.
 */
public class NpcRelationshipGraph {

    public record NpcPair(String npcA, String npcB) {
        public NpcPair {
            if (npcA.compareTo(npcB) > 0) {
                String tmp = npcA; npcA = npcB; npcB = tmp;
            }
        }
    }

    public record NpcRelation(int respect, int tension, int familiarity) {
        public NpcRelation withRespect(int delta) {
            return new NpcRelation(clamp(respect + delta), tension, familiarity);
        }
        public NpcRelation withTension(int delta) {
            return new NpcRelation(respect, clamp(tension + delta), familiarity);
        }
        public NpcRelation withFamiliarity(int delta) {
            return new NpcRelation(respect, tension, clamp(familiarity + delta));
        }
        private static int clamp(int v) { return Math.max(0, Math.min(100, v)); }
    }

    private final Map<NpcPair, NpcRelation> relations = new HashMap<>();

    public void setRelation(String npcA, String npcB, NpcRelation relation) {
        relations.put(new NpcPair(npcA, npcB), relation);
    }

    public NpcRelation getRelation(String npcA, String npcB) {
        return relations.getOrDefault(new NpcPair(npcA, npcB),
                new NpcRelation(50, 0, 0));
    }

    public void modifyTension(String npcA, String npcB, int delta) {
        NpcPair pair = new NpcPair(npcA, npcB);
        NpcRelation current = relations.getOrDefault(pair, new NpcRelation(50, 0, 0));
        relations.put(pair, current.withTension(delta));
    }

    public void modifyRespect(String npcA, String npcB, int delta) {
        NpcPair pair = new NpcPair(npcA, npcB);
        NpcRelation current = relations.getOrDefault(pair, new NpcRelation(50, 0, 0));
        relations.put(pair, current.withRespect(delta));
    }

    public void modifyFamiliarity(String npcA, String npcB, int delta) {
        NpcPair pair = new NpcPair(npcA, npcB);
        NpcRelation current = relations.getOrDefault(pair, new NpcRelation(50, 0, 0));
        relations.put(pair, current.withFamiliarity(delta));
    }

    public boolean isTensionCritical(String npcA, String npcB, int threshold) {
        return getRelation(npcA, npcB).tension() >= threshold;
    }

    public Map<NpcPair, NpcRelation> allRelations() {
        return Map.copyOf(relations);
    }
}
