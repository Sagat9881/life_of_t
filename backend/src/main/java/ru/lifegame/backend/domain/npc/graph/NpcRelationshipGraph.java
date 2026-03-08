package ru.lifegame.backend.domain.npc.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Bidirectional graph of NPC-to-NPC relationships.
 * Loaded from XML cross-npc specifications.
 * Engine doesn't know concrete NPC names — all data-driven.
 */
public class NpcRelationshipGraph {

    private final List<NpcRelationshipEdge> edges;

    public NpcRelationshipGraph() {
        this.edges = new ArrayList<>();
    }

    public NpcRelationshipGraph(List<NpcRelationshipEdge> edges) {
        this.edges = new ArrayList<>(edges);
    }

    public void addEdge(NpcRelationshipEdge edge) {
        edges.add(edge);
    }

    public Optional<NpcRelationshipEdge> getEdge(String npcA, String npcB) {
        return edges.stream()
                .filter(e -> (e.npcA().equals(npcA) && e.npcB().equals(npcB))
                           || (e.npcA().equals(npcB) && e.npcB().equals(npcA)))
                .findFirst();
    }

    public List<NpcRelationshipEdge> getEdgesFor(String npcId) {
        return edges.stream()
                .filter(e -> e.involves(npcId))
                .collect(Collectors.toUnmodifiableList());
    }

    public int getTension(String npcA, String npcB) {
        return getEdge(npcA, npcB).map(NpcRelationshipEdge::tension).orElse(0);
    }

    public int getRespect(String npcA, String npcB) {
        return getEdge(npcA, npcB).map(NpcRelationshipEdge::respect).orElse(50);
    }

    public int getFamiliarity(String npcA, String npcB) {
        return getEdge(npcA, npcB).map(NpcRelationshipEdge::familiarity).orElse(0);
    }

    public List<NpcRelationshipEdge> allEdges() {
        return Collections.unmodifiableList(edges);
    }

    /**
     * Daily tick: tension naturally decays by 2 per day, familiarity grows by 1.
     */
    public void dailyTick() {
        for (NpcRelationshipEdge edge : edges) {
            edge.adjustTension(-2);
            edge.adjustFamiliarity(1);
        }
    }
}
