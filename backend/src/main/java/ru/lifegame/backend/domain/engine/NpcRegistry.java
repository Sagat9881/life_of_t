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
        for (NpcSpec spec : specs) {
            instances.put(spec.id(), NpcInstance.fromSpec(spec));
        }
    }

    public Optional<NpcInstance> get(String npcId) {
        return Optional.ofNullable(instances.get(npcId));
    }

    public Collection<NpcInstance> allNpcs() {
        return Collections.unmodifiableCollection(instances.values());
    }

    public List<NpcInstance> namedNpcs() {
        return instances.values().stream()
            .filter(n -> "named".equals(n.spec().type()))
            .collect(Collectors.toList());
    }

    public List<NpcInstance> fillerNpcs() {
        return instances.values().stream()
            .filter(n -> "filler".equals(n.spec().type()))
            .collect(Collectors.toList());
    }

    public NpcRelationshipGraph relationshipGraph() {
        return relationshipGraph;
    }
}
