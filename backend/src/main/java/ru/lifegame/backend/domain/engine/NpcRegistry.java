package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipGraph;

import java.util.*;

public class NpcRegistry {

    private final Map<String, NpcInstance> instances = new LinkedHashMap<>();
    private final NpcRelationshipGraph relationshipGraph = new NpcRelationshipGraph();

    public void registerAll(List<NpcSpec> specs) {
        for (var spec : specs) {
            instances.put(spec.id(), NpcInstance.fromSpec(spec));
        }
    }

    public Optional<NpcInstance> get(String npcId) {
        return Optional.ofNullable(instances.get(npcId));
    }

    public Collection<NpcInstance> all() {
        return Collections.unmodifiableCollection(instances.values());
    }

    public NpcInstance getOrThrow(String npcId) {
        return Optional.ofNullable(instances.get(npcId))
            .orElseThrow(() -> new IllegalArgumentException("NPC not found: " + npcId));
    }

    public NpcRelationshipGraph relationshipGraph() {
        return relationshipGraph;
    }

    public int size() { return instances.size(); }
}
