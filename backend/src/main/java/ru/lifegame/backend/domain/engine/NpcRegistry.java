package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipGraph;
import ru.lifegame.backend.domain.engine.spec.NpcSpec;

import java.util.*;

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

    public Collection<NpcInstance> all() {
        return Collections.unmodifiableCollection(instances.values());
    }

    public NpcInstance getOrThrow(String npcId) {
        return Optional.ofNullable(instances.get(npcId))
            .orElseThrow(() -> new IllegalArgumentException("NPC not found: " + npcId));
    }

    public Map<String, NpcInstance> allAsMap() {
        return Collections.unmodifiableMap(instances);
    }

    public NpcRelationshipGraph relationshipGraph() {
        return relationshipGraph;
    }
}
