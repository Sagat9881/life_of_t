package com.sagat9881.lifeoft.domain.npc.engine;

import java.util.*;

/**
 * Bidirectional relationship graph between NPC pairs.
 * Tracks respect, tension, and familiarity between any two NPCs.
 * 
 * Loaded from XML relationship definitions.
 * Used by CrossNpcTriggerEngine to detect cross-NPC conflicts.
 */
public class NpcRelationshipGraph {

    private final Map<String, NpcNpcRelation> relations;

    public NpcRelationshipGraph() {
        this.relations = new HashMap<>();
    }

    /**
     * Set initial relationship between two NPCs.
     */
    public void setRelation(String npcA, String npcB, double respect, double tension, double familiarity) {
        String key = makeKey(npcA, npcB);
        relations.put(key, new NpcNpcRelation(npcA, npcB, respect, tension, familiarity));
    }

    /**
     * Get relationship between two NPCs.
     */
    public Optional<NpcNpcRelation> getRelation(String npcA, String npcB) {
        String key = makeKey(npcA, npcB);
        return Optional.ofNullable(relations.get(key));
    }

    /**
     * Adjust tension between two NPCs.
     */
    public void adjustTension(String npcA, String npcB, double delta) {
        getRelation(npcA, npcB).ifPresent(rel -> {
            String key = makeKey(npcA, npcB);
            relations.put(key, rel.withTension(clamp(rel.tension() + delta)));
        });
    }

    /**
     * Adjust respect between two NPCs.
     */
    public void adjustRespect(String npcA, String npcB, double delta) {
        getRelation(npcA, npcB).ifPresent(rel -> {
            String key = makeKey(npcA, npcB);
            relations.put(key, rel.withRespect(clamp(rel.respect() + delta)));
        });
    }

    /**
     * Get all pairs where tension exceeds threshold.
     */
    public List<NpcNpcRelation> tensePairs(double threshold) {
        List<NpcNpcRelation> result = new ArrayList<>();
        for (NpcNpcRelation rel : relations.values()) {
            if (rel.tension() >= threshold) {
                result.add(rel);
            }
        }
        return result;
    }

    public Collection<NpcNpcRelation> all() {
        return Collections.unmodifiableCollection(relations.values());
    }

    private String makeKey(String a, String b) {
        return a.compareTo(b) <= 0 ? a + "::" + b : b + "::" + a;
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(100.0, value));
    }

    /**
     * Immutable record of relationship between two NPCs.
     */
    public record NpcNpcRelation(
            String npcA,
            String npcB,
            double respect,
            double tension,
            double familiarity
    ) {
        public NpcNpcRelation withTension(double newTension) {
            return new NpcNpcRelation(npcA, npcB, respect, newTension, familiarity);
        }

        public NpcNpcRelation withRespect(double newRespect) {
            return new NpcNpcRelation(npcA, npcB, newRespect, tension, familiarity);
        }

        public NpcNpcRelation withFamiliarity(double newFamiliarity) {
            return new NpcNpcRelation(npcA, npcB, respect, tension, newFamiliarity);
        }
    }
}
