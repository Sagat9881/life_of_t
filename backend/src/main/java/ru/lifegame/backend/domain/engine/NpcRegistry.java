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
        for (NpcSpec spec : specs) {
            instances.put(spec.id(), NpcInstance.fromSpec(spec));
        }
        this.relationshipGraph = new NpcRelationshipGraph();
    }

    public Optional<NpcInstance> get(String npcId) {
        return Optional.ofNullable(instances.get(npcId));
    }

    public NpcInstance require(String npcId) {
        return Optional.ofNullable(instances.get(npcId))
                .orElseThrow(() -> new IllegalArgumentException("NPC not found: " + npcId));
    }

    public Collection<NpcInstance> all() {
        return Collections.unmodifiableCollection(instances.values());
    }

    public List<NpcInstance> named() {
        return instances.values().stream()
                .filter(i -> "named".equals(i.spec().type()))
                .collect(Collectors.toList());
    }

    public NpcRelationshipGraph relationshipGraph() { return relationshipGraph; }
}
