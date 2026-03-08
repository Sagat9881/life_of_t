package com.sagat9881.lifeoft.domain.npc;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Registry of all live NPC instances in a game session.
 * Created from NpcSpec list (parsed from XML).
 * Named NPCs get full brain (6-axis mood, memory, utility AI).
 * Filler NPCs get simplified brain (2-axis mood, fixed schedule, no memory).
 */
public class NpcRegistry {

    private final Map<String, NpcInstance> instances;

    private NpcRegistry(Map<String, NpcInstance> instances) {
        this.instances = new LinkedHashMap<>(instances);
    }

    /**
     * Create registry from a list of NPC specifications.
     * Each spec produces one NpcInstance — named or filler.
     */
    public static NpcRegistry fromSpecs(List<NpcSpec> specs) {
        Map<String, NpcInstance> map = new LinkedHashMap<>();
        for (NpcSpec spec : specs) {
            NpcInstance instance = createInstance(spec);
            map.put(spec.id(), instance);
        }
        return new NpcRegistry(map);
    }

    private static NpcInstance createInstance(NpcSpec spec) {
        NpcMood mood = NpcMood.fromSpec(spec);
        NpcMemory memory = spec.memoryEnabled()
                ? NpcMemory.create(spec.shortTermSize())
                : NpcMemory.disabled();
        NpcSchedule schedule = NpcSchedule.fromSlots(spec.scheduleSlots());
        NpcActivity initialActivity = schedule.activityAt(8); // default start hour

        return new NpcInstance(spec, mood, memory, schedule, initialActivity);
    }

    public Optional<NpcInstance> get(String npcId) {
        return Optional.ofNullable(instances.get(npcId));
    }

    public Collection<NpcInstance> all() {
        return Collections.unmodifiableCollection(instances.values());
    }

    public List<NpcInstance> named() {
        return instances.values().stream()
                .filter(npc -> "named".equals(npc.spec().type()))
                .collect(Collectors.toList());
    }

    public List<NpcInstance> fillers() {
        return instances.values().stream()
                .filter(npc -> "filler".equals(npc.spec().type()))
                .collect(Collectors.toList());
    }

    public List<NpcInstance> byCategory(String category) {
        return instances.values().stream()
                .filter(npc -> category.equals(npc.spec().category()))
                .collect(Collectors.toList());
    }

    /**
     * Update an NPC instance in the registry (after mood/activity change).
     */
    public void update(NpcInstance updated) {
        instances.put(updated.spec().id(), updated);
    }

    public int size() {
        return instances.size();
    }
}
