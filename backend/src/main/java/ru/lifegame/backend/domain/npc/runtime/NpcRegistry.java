package ru.lifegame.backend.domain.npc.runtime;


import ru.lifegame.backend.domain.npc.graph.NpcRelationshipGraph;
import ru.lifegame.backend.domain.npc.spec.NpcSpec;

import java.util.*;
import java.util.stream.Collectors;

public class NpcRegistry {

    private final Map<String, NpcInstance> instances = new LinkedHashMap<>();
    private final NpcRelationshipGraph relationshipGraph = new NpcRelationshipGraph();

    public void registerFromSpecs(List<NpcSpec> specs) {
        for (NpcSpec spec : specs) {
            NpcInstance instance = NpcInstance.fromSpec(spec);
            instances.put(spec.id(), instance);
        }
    }

    public Optional<NpcInstance> get(String npcId) {
        return Optional.ofNullable(instances.get(npcId));
    }

    public Collection<NpcInstance> getAll() {
        return Collections.unmodifiableCollection(instances.values());
    }

    public NpcInstance getOrThrow(String npcId) {
        NpcInstance inst = instances.get(npcId);
        if (inst == null) throw new IllegalArgumentException("NPC not found: " + npcId);
        return inst;
    }

    public List<NpcInstance> getByCategory(String category) {
        return instances.values().stream()
                .filter(i -> i.spec().category().equals(category))
                .collect(Collectors.toList());
    }

    public NpcRelationshipGraph getRelationshipGraph() {
        return relationshipGraph;
    }

    public int size() {
        return instances.size();
    }
}
