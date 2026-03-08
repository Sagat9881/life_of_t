package com.sagat9881.lifeoft.domain.npc.engine;

import com.sagat9881.lifeoft.domain.npc.model.NpcInstance;
import com.sagat9881.lifeoft.domain.npc.model.NpcMood;
import com.sagat9881.lifeoft.domain.npc.model.NpcMemory;
import com.sagat9881.lifeoft.domain.npc.model.NpcSchedule;
import com.sagat9881.lifeoft.domain.npc.spec.NpcSpec;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Registry of all NPC instances in a game session.
 * Creates NpcInstance objects from NpcSpec definitions loaded from XML.
 * Named NPCs get full brain (6-axis mood, memory, utility AI).
 * Filler NPCs get light brain (2-axis mood, fixed schedule, no memory).
 */
public class NpcRegistry {

    private final Map<String, NpcInstance> instances;

    private NpcRegistry(Map<String, NpcInstance> instances) {
        this.instances = new LinkedHashMap<>(instances);
    }

    /**
     * Create registry from list of NPC specifications (loaded from XML).
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
        NpcMood mood = createMood(spec);
        NpcMemory memory = createMemory(spec);
        NpcSchedule schedule = NpcSchedule.fromSlots(spec.scheduleSlots());
        String initialLocation = schedule.getSlotForHour(8)
                .map(NpcSchedule.ScheduleSlot::location)
                .orElse("home");

        return new NpcInstance(spec, mood, memory, schedule, null, initialLocation);
    }

    private static NpcMood createMood(NpcSpec spec) {
        if ("named".equals(spec.type())) {
            // Full 6-axis mood from XML initial values
            var init = spec.moodInitial();
            return new NpcMood(
                    init.getOrDefault("happiness", 50.0),
                    init.getOrDefault("anxiety", 20.0),
                    init.getOrDefault("loneliness", 20.0),
                    init.getOrDefault("irritability", 10.0),
                    init.getOrDefault("energy", 80.0),
                    init.getOrDefault("affection", 50.0)
            );
        } else {
            // Filler: only happiness + energy, rest zeroed
            var init = spec.moodInitial();
            return new NpcMood(
                    init.getOrDefault("happiness", 50.0),
                    0, 0, 0,
                    init.getOrDefault("energy", 70.0),
                    0
            );
        }
    }

    private static NpcMemory createMemory(NpcSpec spec) {
        if ("named".equals(spec.type()) && spec.memoryEnabled()) {
            return NpcMemory.create(spec.shortTermSize());
        }
        return null; // Filler NPCs have no memory
    }

    public Optional<NpcInstance> get(String npcId) {
        return Optional.ofNullable(instances.get(npcId));
    }

    public Collection<NpcInstance> all() {
        return Collections.unmodifiableCollection(instances.values());
    }

    public List<NpcInstance> named() {
        return instances.values().stream()
                .filter(i -> "named".equals(i.spec().type()))
                .collect(Collectors.toList());
    }

    public List<NpcInstance> fillers() {
        return instances.values().stream()
                .filter(i -> "filler".equals(i.spec().type()))
                .collect(Collectors.toList());
    }

    public void update(String npcId, NpcInstance updated) {
        if (instances.containsKey(npcId)) {
            instances.put(npcId, updated);
        }
    }

    public int size() {
        return instances.size();
    }

    /**
     * Observe a player action — notify all NPC memories.
     */
    public void observePlayerAction(String actionId, int day) {
        for (var entry : instances.entrySet()) {
            NpcInstance npc = entry.getValue();
            if (npc.memory() != null) {
                npc.memory().record(actionId, day);
            }
        }
    }
}
