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

    public NpcRegistry(List<NpcSpec> specs, NpcRelationshipGraph relationshipGraph) {
        this.relationshipGraph = relationshipGraph;
        for (NpcSpec spec : specs) {
            instances.put(spec.id(), NpcInstance.fromSpec(spec));
        }
    }

    public NpcInstance get(String npcId) {
        return instances.get(npcId);
    }

    public Collection<NpcInstance> allNamed() {
        return instances.values().stream()
                .filter(npc -> "named".equals(npc.spec().type()))
                .collect(Collectors.toList());
    }

    public Collection<NpcInstance> allFiller() {
        return instances.values().stream()
                .filter(npc -> "filler".equals(npc.spec().type()))
                .collect(Collectors.toList());
    }

    public Collection<NpcInstance> all() {
        return Collections.unmodifiableCollection(instances.values());
    }

    public NpcRelationshipGraph relationshipGraph() {
        return relationshipGraph;
    }
}
