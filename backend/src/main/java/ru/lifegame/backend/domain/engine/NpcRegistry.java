package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.engine.runtime.NpcUtilityBrain;
import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipGraph;

import java.util.*;
import java.util.stream.Collectors;

public class NpcRegistry {

    private final Map<String, NpcInstance> instances = new LinkedHashMap<>();
    private NpcRelationshipGraph relationshipGraph;

    public void registerAll(List<NpcSpec> specs) {
        for (NpcSpec spec : specs) {
            instances.put(spec.id(), NpcInstance.fromSpec(spec));
        }
    }

    public Optional<NpcInstance> get(String npcId) {
        return Optional.ofNullable(instances.get(npcId));
    }

    public List<NpcInstance> getAll() {
        return new ArrayList<>(instances.values());
    }

    public List<NpcInstance> getNamed() {
        return instances.values().stream()
            .filter(i -> "named".equals(i.spec().type()))
            .collect(Collectors.toList());
    }

    public List<NpcInstance> getFillers() {
        return instances.values().stream()
            .filter(i -> "filler".equals(i.spec().type()))
            .collect(Collectors.toList());
    }

    public NpcRelationshipGraph getRelationshipGraph() {
        return relationshipGraph;
    }

    public void setRelationshipGraph(NpcRelationshipGraph graph) {
        this.relationshipGraph = graph;
    }
}
