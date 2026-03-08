package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.engine.runtime.NpcUtilityBrain;
import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipGraph;

import java.util.*;
import java.util.stream.Collectors;

public class NpcRegistry {

    private final Map<String, NpcInstance> instances = new LinkedHashMap<>();
    private final NpcRelationshipGraph relationshipGraph;

    public NpcRegistry(List<NpcSpec> specs) {
        this.relationshipGraph = new NpcRelationshipGraph();
        if (specs != null) {
            for (NpcSpec spec : specs) {
                instances.put(spec.id(), NpcInstance.fromSpec(spec));
            }
        }
    }

    public Optional<NpcInstance> get(String npcId) {
        return Optional.ofNullable(instances.get(npcId));
    }

    public NpcInstance getOrThrow(String npcId) {
        return Optional.ofNullable(instances.get(npcId))
                .orElseThrow(() -> new IllegalArgumentException("NPC not found: " + npcId));
    }

    public List<NpcInstance> all() {
        return List.copyOf(instances.values());
    }

    public List<NpcInstance> byCategory(String category) {
        return instances.values().stream()
                .filter(i -> i.spec().category().equals(category))
                .collect(Collectors.toList());
    }

    public NpcRelationshipGraph relationshipGraph() {
        return relationshipGraph;
    }
}
