package com.life_of_t.domain.npc;

import java.util.*;

/**
 * Bidirectional relationship graph between all NPC pairs.
 * Each edge has: respect, tension, familiarity (0-100).
 * Loaded from XML spec, updated by cross-NPC triggers.
 * Backend treats axis names as opaque strings — content defines them.
 */
public class NpcRelationshipGraph {

    public record NpcEdge(
            String npcA,
            String npcB,
            Map<String, Integer> axes
    ) {
        public int getAxis(String axis) {
            return axes.getOrDefault(axis, 50);
        }

        public NpcEdge withAxis(String axis, int value) {
            var newAxes = new HashMap<>(axes);
            newAxes.put(axis, Math.max(0, Math.min(100, value)));
            return new NpcEdge(npcA, npcB, Map.copyOf(newAxes));
        }

        public NpcEdge adjustAxis(String axis, int delta) {
            return withAxis(axis, getAxis(axis) + delta);
        }

        /**
         * Canonical key for this edge (order-independent).
         */
        public String key() {
            return npcA.compareTo(npcB) < 0
                    ? npcA + "<->" + npcB
                    : npcB + "<->" + npcA;
        }
    }

    private final Map<String, NpcEdge> edges;

    public NpcRelationshipGraph() {
        this.edges = new HashMap<>();
    }

    /**
     * Initialize an edge between two NPCs with default values.
     */
    public void addEdge(String npcA, String npcB, Map<String, Integer> initialAxes) {
        NpcEdge edge = new NpcEdge(npcA, npcB, Map.copyOf(initialAxes));
        edges.put(edge.key(), edge);
    }

    /**
     * Get the relationship edge between two NPCs.
     * Returns empty if no relationship defined.
     */
    public Optional<NpcEdge> getEdge(String npcA, String npcB) {
        String key = npcA.compareTo(npcB) < 0
                ? npcA + "<->" + npcB
                : npcB + "<->" + npcA;
        return Optional.ofNullable(edges.get(key));
    }

    /**
     * Adjust a specific axis of a relationship between two NPCs.
     */
    public void adjustRelationship(String npcA, String npcB, String axis, int delta) {
        getEdge(npcA, npcB).ifPresent(edge -> {
            edges.put(edge.key(), edge.adjustAxis(axis, delta));
        });
    }

    /**
     * Get all edges where given NPC is involved.
     */
    public List<NpcEdge> edgesFor(String npcId) {
        return edges.values().stream()
                .filter(e -> e.npcA().equals(npcId) || e.npcB().equals(npcId))
                .toList();
    }

    /**
     * Check if tension between two NPCs exceeds threshold.
     */
    public boolean hasTensionAbove(String npcA, String npcB, int threshold) {
        return getEdge(npcA, npcB)
                .map(e -> e.getAxis("tension") > threshold)
                .orElse(false);
    }

    public Collection<NpcEdge> allEdges() { return Collections.unmodifiableCollection(edges.values()); }
}
