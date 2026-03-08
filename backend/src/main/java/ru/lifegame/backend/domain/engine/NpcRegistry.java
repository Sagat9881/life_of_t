package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.engine.runtime.NpcUtilityBrain;
import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipGraph;

import java.util.*;

public class NpcRegistry {

    private final Map<String, NpcInstance> instances = new LinkedHashMap<>();
    private final NpcRelationshipGraph relationshipGraph = new NpcRelationshipGraph();

    public void registerFromSpec(NpcSpec spec) {
        NpcInstance instance = NpcInstance.fromSpec(spec);
        instances.put(spec.id(), instance);
    }

    public Optional<NpcInstance> get(String npcId) {
        return Optional.ofNullable(instances.get(npcId));
    }

    public Collection<NpcInstance> all() {
        return Collections.unmodifiableCollection(instances.values());
    }

    public void update(String npcId, NpcInstance updated) {
        instances.put(npcId, updated);
    }

    public NpcRelationshipGraph relationshipGraph() {
        return relationshipGraph;
    }
}
