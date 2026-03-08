package com.sagat9881.lifeoft.domain.npc.engine;

import java.util.*;

/**
 * Bidirectional relationship graph between NPC pairs.
 * Tracks respect, tension, familiarity between any two NPCs.
 * Loaded from XML cross-npc-relationships spec.
 * Used by CrossNpcTriggerEngine for inter-NPC dynamics.
 */
public class NpcRelationshipGraph {

    private final Map<String, NpcNpcRelation> edges;

    public NpcRelationshipGraph() {
        this.edges = new HashMap<>();
    }

    public void addRelation(String npcA, String npcB, double respect, double tension, double familiarity) {
        String key = edgeKey(npcA, npcB);
        edges.put(key, new NpcNpcRelation(npcA, npcB, respect, tension, familiarity));
    }

    public Optional<NpcNpcRelation> getRelation(String npcA, String npcB) {
        return Optional.ofNullable(edges.get(edgeKey(npcA, npcB)));
    }

    public void updateTension(String npcA, String npcB, double delta) {
        String key = edgeKey(npcA, npcB);
        NpcNpcRelation current = edges.get(key);
        if (current != null) {
            edges.put(key, current.withTension(clamp(current.tension() + delta)));
        }
    }

    public void updateRespect(String npcA, String npcB, double delta) {
        String key = edgeKey(npcA, npcB);
        NpcNpcRelation current = edges.get(key);
        if (current != null) {
            edges.put(key, current.withRespect(clamp(current.respect() + delta)));
        }
    }

    public List<NpcNpcRelation> highTensionPairs(double threshold) {
        return edges.values().stream()
                .filter(r -> r.tension() >= threshold)
                .toList();
    }

    private String edgeKey(String a, String b) {
        // Alphabetical order for consistency
        return a.compareTo(b) <= 0 ? a + "::" + b : b + "::" + a;
    }

    private double clamp(double v) {
        return Math.max(0, Math.min(100, v));
    }

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
    }
}
