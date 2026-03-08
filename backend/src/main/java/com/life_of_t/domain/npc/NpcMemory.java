package com.life_of_t.domain.npc;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * NPC memory system. Short-term tracks recent player actions (rolling window).
 * Long-term stores significant events (flagged by the engine).
 * Pattern detection provides context-aware NPC reactions.
 * For filler NPCs, memory is disabled (empty, no-op).
 */
public class NpcMemory {

    public record MemoryEntry(
            String actionId,
            int dayOccurred,
            int hourOccurred,
            String context
    ) {}

    private final Deque<MemoryEntry> shortTerm;
    private final List<MemoryEntry> longTerm;
    private final int shortTermCapacity;
    private final boolean enabled;

    /**
     * Create enabled memory with given short-term capacity (from XML spec).
     */
    public NpcMemory(int shortTermCapacity) {
        this.shortTermCapacity = shortTermCapacity;
        this.shortTerm = new ArrayDeque<>(shortTermCapacity);
        this.longTerm = new ArrayList<>();
        this.enabled = true;
    }

    /**
     * Create disabled memory for filler NPCs — all operations are no-op.
     */
    public static NpcMemory disabled() {
        NpcMemory mem = new NpcMemory(0);
        return new NpcMemory(0) {
            @Override public void observe(MemoryEntry entry) { /* no-op */ }
            @Override public void flagAsSignificant(MemoryEntry entry) { /* no-op */ }
            @Override public boolean detectPattern(String actionId, int minCount) { return false; }
        };
    }

    /**
     * Record a player action observed by this NPC.
     */
    public void observe(MemoryEntry entry) {
        if (!enabled || shortTermCapacity == 0) return;
        if (shortTerm.size() >= shortTermCapacity) {
            shortTerm.pollFirst();
        }
        shortTerm.addLast(entry);
    }

    /**
     * Flag an entry as significant (moves to long-term, e.g., first date, big fight).
     */
    public void flagAsSignificant(MemoryEntry entry) {
        if (!enabled) return;
        longTerm.add(entry);
    }

    /**
     * Detect if a specific action appears at least minCount times in short-term memory.
     * Used for pattern detection: "player worked 3 days in a row".
     */
    public boolean detectPattern(String actionId, int minCount) {
        if (!enabled) return false;
        long count = shortTerm.stream()
                .filter(e -> e.actionId().equals(actionId))
                .count();
        return count >= minCount;
    }

    /**
     * Check if a specific action was the most recent entry.
     */
    public boolean lastActionWas(String actionId) {
        if (shortTerm.isEmpty()) return false;
        return shortTerm.peekLast().actionId().equals(actionId);
    }

    /**
     * Count consecutive recent occurrences of a specific action (from most recent).
     */
    public int consecutiveCount(String actionId) {
        int count = 0;
        var iter = shortTerm.descendingIterator();
        while (iter.hasNext()) {
            if (iter.next().actionId().equals(actionId)) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    /**
     * Detect work obsession: player did work-related actions in 3+ of last 5 entries.
     */
    public boolean detectWorkObsession() {
        return detectPattern("GO_TO_WORK", 3);
    }

    /**
     * Detect if NPC is being ignored: no interaction with this NPC in last N entries.
     * The interactionActionId is loaded from XML (e.g., "DATE_WITH_HUSBAND" for Alexander).
     */
    public boolean isBeingIgnored(String interactionActionId, int threshold) {
        if (shortTerm.isEmpty()) return false;
        long interactionCount = shortTerm.stream()
                .filter(e -> e.actionId().equals(interactionActionId))
                .count();
        return interactionCount == 0 && shortTerm.size() >= threshold;
    }

    /**
     * Get the dominant action in short-term memory (most frequent).
     */
    public String dominantAction() {
        if (shortTerm.isEmpty()) return "none";
        return shortTerm.stream()
                .collect(Collectors.groupingBy(MemoryEntry::actionId, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("none");
    }

    /**
     * Days since last interaction with a specific action.
     * Returns Integer.MAX_VALUE if never interacted.
     */
    public int daysSinceAction(String actionId, int currentDay) {
        return shortTerm.stream()
                .filter(e -> e.actionId().equals(actionId))
                .mapToInt(MemoryEntry::dayOccurred)
                .max()
                .map(lastDay -> currentDay - lastDay)
                .orElse(Integer.MAX_VALUE);
    }

    public List<MemoryEntry> shortTermEntries() { return List.copyOf(shortTerm); }
    public List<MemoryEntry> longTermEntries() { return List.copyOf(longTerm); }
    public boolean isEnabled() { return enabled; }
}
