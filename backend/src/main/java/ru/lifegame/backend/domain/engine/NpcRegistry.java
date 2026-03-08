package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.engine.runtime.NpcUtilityBrain;
import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipGraph;

import java.util.*;
import java.util.stream.Collectors;

public class NpcRegistry {

    private final Map<String, NpcInstance> instances = new LinkedHashMap<>();
    private final NpcRelationshipGraph relationshipGraph = new NpcRelationshipGraph();

    public void registerAll(List<NpcSpec> specs) {
        for (NpcSpec spec : specs) {
            instances.put(spec.id(), NpcInstance.fromSpec(spec));
        }
    }

    public Optional<NpcInstance> get(String npcId) {
        return Optional.ofNullable(instances.get(npcId));
    }

    public List<NpcInstance> allNamed() {
        return instances.values().stream()
                .filter(i -> "named".equals(i.spec().type()))
                .collect(Collectors.toList());
    }

    public List<NpcInstance> all() {
        return new ArrayList<>(instances.values());
    }

    public NpcRelationshipGraph relationshipGraph() {
        return relationshipGraph;
    }

    public int size() { return instances.size(); }
}
