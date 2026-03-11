package ru.lifegame.backend.domain.npc.runtime;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Short-term and long-term memory for named NPCs.
 * Disabled for filler NPCs.
 */
public class NpcMemory {

    public record MemoryEntry(String actionId, int day, int hour, String context) {}

    private final Deque<MemoryEntry> shortTerm;
    private final List<MemoryEntry>  longTerm;
    private final boolean enabled;
    private final int shortTermCapacity;

    public NpcMemory(int shortTermCapacity) {
        this.shortTermCapacity = shortTermCapacity;
        this.shortTerm = new ArrayDeque<>(shortTermCapacity);
        this.longTerm  = new ArrayList<>();
        this.enabled   = true;
    }

    /** Copy constructor for per-session deep copy. */
    public NpcMemory(NpcMemory source) {
        this.shortTermCapacity = source.shortTermCapacity;
        this.shortTerm = new ArrayDeque<>(source.shortTerm);
        this.longTerm  = new ArrayList<>(source.longTerm);
        this.enabled   = source.enabled;
    }

    private NpcMemory() {
        this.shortTermCapacity = 0;
        this.shortTerm = new ArrayDeque<>(0);
        this.longTerm  = new ArrayList<>();
        this.enabled   = false;
    }

    public static NpcMemory disabled() {
        return new NpcMemory();
    }

    public void observe(MemoryEntry entry) {
        if (!enabled) return;
        if (shortTerm.size() >= shortTermCapacity && shortTermCapacity > 0) {
            MemoryEntry evicted = shortTerm.pollFirst();
            if (evicted != null) longTerm.add(evicted);
        }
        shortTerm.addLast(entry);
    }

    /**
     * Returns true if the given actionId appears at least {@code threshold} times
     * across short-term and long-term memory combined.
     * Used by NpcUtilityBrain to detect repetitive patterns (e.g. work obsession).
     */
    public boolean detectPattern(String actionId, int threshold) {
        if (!enabled) return false;
        long count = shortTerm.stream().filter(e -> actionId.equals(e.actionId())).count()
                   + longTerm.stream().filter(e -> actionId.equals(e.actionId())).count();
        return count >= threshold;
    }

    /**
     * Returns true if {@code interactionActionId} has NOT appeared in memory
     * at least {@code threshold} times — indicating the NPC is being ignored.
     * Used by NpcUtilityBrain for "being_ignored" memory condition.
     */
    public boolean isBeingIgnored(String interactionActionId, int threshold) {
        if (!enabled) return false;
        long count = shortTerm.stream().filter(e -> interactionActionId.equals(e.actionId())).count()
                   + longTerm.stream().filter(e -> interactionActionId.equals(e.actionId())).count();
        return count < threshold;
    }

    public List<MemoryEntry> shortTerm() { return List.copyOf(shortTerm); }
    public List<MemoryEntry> longTerm()  { return List.copyOf(longTerm); }
    public boolean isEnabled()           { return enabled; }
}
