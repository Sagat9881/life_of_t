package ru.lifegame.backend.domain.npc;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Registry of all live NPC instances in a game session.
 * Replaces hardcoded NpcProfiles — manages N NPCs loaded from XML specs.
 * Supports both named (full brain) and filler (light brain) NPCs.
 */
public class NpcRegistry {
    private final Map<String, NpcInstance> instances;

    private NpcRegistry(Map<String, NpcInstance> instances) {
        this.instances = instances;
    }

    /**
     * Create registry from a list of NPC specs (loaded from XML).
     */
    public static NpcRegistry fromSpecs(List<NpcSpec> specs) {
        Map<String, NpcInstance> map = new LinkedHashMap<>();
        for (NpcSpec spec : specs) {
            map.put(spec.id(), new NpcInstance(spec));
        }
        return new NpcRegistry(map);
    }

    /**
     * Add a filler NPC dynamically (e.g., quest-spawned).
     */
    public void addFiller(NpcSpec fillerSpec) {
        if (!fillerSpec.isFiller()) {
            throw new IllegalArgumentException("Expected filler NPC spec: " + fillerSpec.id());
        }
        instances.put(fillerSpec.id(), new NpcInstance(fillerSpec));
    }

    /**
     * Remove a filler NPC (e.g., quest completed, NPC leaves).
     */
    public void removeFiller(String npcId) {
        NpcInstance inst = instances.get(npcId);
        if (inst != null && inst.spec().isFiller()) {
            instances.remove(npcId);
        }
    }

    public Optional<NpcInstance> get(String npcId) {
        return Optional.ofNullable(instances.get(npcId));
    }

    public Collection<NpcInstance> all() {
        return Collections.unmodifiableCollection(instances.values());
    }

    public List<NpcInstance> named() {
        return instances.values().stream()
            .filter(NpcInstance::isNamed)
            .toList();
    }

    public List<NpcInstance> fillers() {
        return instances.values().stream()
            .filter(npc -> !npc.isNamed())
            .toList();
    }

    public List<NpcInstance> presentAt(int hour) {
        return instances.values().stream()
            .filter(npc -> npc.isPresent(hour))
            .toList();
    }

    /**
     * Observe a player action across all present NPCs.
     */
    public void broadcastPlayerAction(int day, String actionCode, int currentHour) {
        for (NpcInstance npc : presentAt(currentHour)) {
            npc.observePlayerAction(day, actionCode);
        }
    }

    public int size() {
        return instances.size();
    }
}
