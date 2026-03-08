package com.sagat9881.lifeoft.domain.npc.engine;

import com.sagat9881.lifeoft.domain.npc.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Registry of all NPC instances in a game session.
 * Created from NpcSpec list (loaded from XML).
 * 
 * Named NPCs get full brain (6-axis mood, memory, utility AI).
 * Filler NPCs get simplified brain (2-axis mood, fixed schedule, no memory).
 */
public class NpcRegistry {

    private final Map<String, NpcInstance> npcs;

    private NpcRegistry(Map<String, NpcInstance> npcs) {
        this.npcs = new LinkedHashMap<>(npcs);
    }

    /**
     * Create registry from list of specs (loaded from XML).
     */
    public static NpcRegistry fromSpecs(List<NpcSpec> specs) {
        Map<String, NpcInstance> instances = new LinkedHashMap<>();
        for (NpcSpec spec : specs) {
            NpcInstance instance = createInstance(spec);
            instances.put(spec.id(), instance);
        }
        return new NpcRegistry(instances);
    }

    private static NpcInstance createInstance(NpcSpec spec) {
        NpcMood mood;
        NpcMemoryLog memory;

        if ("named".equals(spec.type())) {
            // Named NPC: full 6-axis mood from spec initial values
            mood = NpcMood.fromSpec(spec.moodInitial());
            memory = spec.memoryEnabled()
                    ? new NpcMemoryLog(spec.shortTermSize())
                    : NpcMemoryLog.disabled();
        } else {
            // Filler NPC: simplified 2-axis mood
            mood = NpcMood.simplified(
                    spec.moodInitial().getOrDefault("happiness", 50.0),
                    spec.moodInitial().getOrDefault("energy", 50.0)
            );
            memory = NpcMemoryLog.disabled();
        }

        NpcSchedule schedule = NpcSchedule.fromSlots(spec.scheduleSlots());
        NpcActivity initialActivity = NpcActivity.idle(spec.defaultLocation());

        return new NpcInstance(spec, mood, memory, schedule, initialActivity);
    }

    public Optional<NpcInstance> get(String npcId) {
        return Optional.ofNullable(npcs.get(npcId));
    }

    public Collection<NpcInstance> all() {
        return Collections.unmodifiableCollection(npcs.values());
    }

    public List<NpcInstance> named() {
        return npcs.values().stream()
                .filter(npc -> "named".equals(npc.spec().type()))
                .collect(Collectors.toList());
    }

    public List<NpcInstance> fillers() {
        return npcs.values().stream()
                .filter(npc -> "filler".equals(npc.spec().type()))
                .collect(Collectors.toList());
    }

    public List<NpcInstance> byCategory(String category) {
        return npcs.values().stream()
                .filter(npc -> category.equals(npc.spec().category()))
                .collect(Collectors.toList());
    }

    /**
     * Record an observed player action for all NPC memories.
     */
    public void observePlayerAction(String actionId, int day) {
        for (NpcInstance npc : npcs.values()) {
            npc.memory().recordAction(actionId, day);
        }
    }

    public int size() {
        return npcs.size();
    }
}
