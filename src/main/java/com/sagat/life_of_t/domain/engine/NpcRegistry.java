package com.sagat.life_of_t.domain.engine;

import com.sagat.life_of_t.domain.engine.runtime.NpcInstance;
import com.sagat.life_of_t.domain.engine.runtime.NpcRelationshipGraph;
import com.sagat.life_of_t.domain.engine.spec.NpcSpec;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Registry of all NPC instances. Loaded from XML specs at startup.
 * No hardcoded NPC IDs — the registry is purely data-driven.
 */
public class NpcRegistry {
    private final Map<String, NpcInstance> instances = new LinkedHashMap<>();
    private final NpcRelationshipGraph relationshipGraph = new NpcRelationshipGraph();

    public void register(NpcSpec spec) {
        NpcInstance instance = spec.isNamed()
                ? NpcInstance.createNamed(spec)
                : NpcInstance.createFiller(spec);
        instances.put(spec.entityId(), instance);
    }

    public Optional<NpcInstance> get(String entityId) {
        return Optional.ofNullable(instances.get(entityId));
    }

    public Collection<NpcInstance> allNpcs() {
        return Collections.unmodifiableCollection(instances.values());
    }

    public List<NpcInstance> namedNpcs() {
        return instances.values().stream()
                .filter(n -> n.spec().isNamed())
                .collect(Collectors.toUnmodifiableList());
    }

    public List<NpcInstance> fillerNpcs() {
        return instances.values().stream()
                .filter(n -> !n.spec().isNamed())
                .collect(Collectors.toUnmodifiableList());
    }

    public NpcRelationshipGraph relationshipGraph() {
        return relationshipGraph;
    }

    public void initializeRelation(String npcA, String npcB, int respect, int tension, int familiarity) {
        relationshipGraph.setRelation(npcA, npcB,
                new NpcRelationshipGraph.NpcRelation(respect, tension, familiarity));
    }

    public int size() {
        return instances.size();
    }
}
