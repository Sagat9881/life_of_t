package com.sagat9881.lifeoft.domain.npc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Extended NPC memory with short-term and long-term storage.
 * Short-term: sliding window of recent player actions (configurable size from XML).
 * Long-term: flagged significant events that persist throughout the game.
 * Pattern detection methods are generic — they operate on action IDs, not hardcoded names.
 */
public class NpcMemoryLog {

    public record MemoryEntry(String actionId, int day, int hour, boolean significant) {}

    private final int shortTermSize;
    private final LinkedList<MemoryEntry> shortTerm;
    private final List<MemoryEntry> longTerm;
    private int daysSinceLastInteraction;

    public NpcMemoryLog(int shortTermSize) {
        this.shortTermSize = shortTermSize;
        this.shortTerm = new LinkedList<>();
        this.longTerm = new ArrayList<>();
        this.daysSinceLastInteraction = 0;
    }

    public static NpcMemoryLog disabled() {
        return new NpcMemoryLog(0);
    }

    public boolean isEnabled() {
        return shortTermSize > 0;
    }

    public void recordAction(String actionId, int day, int hour, boolean significant) {
        if (!isEnabled()) return;
        MemoryEntry entry = new MemoryEntry(actionId, day, hour, significant);
        shortTerm.addFirst(entry);
        while (shortTerm.size() > shortTermSize) {
            shortTerm.removeLast();
        }
        if (significant) {
            longTerm.add(entry);
        }
        daysSinceLastInteraction = 0;
    }

    public void onDayEnd() {
        daysSinceLastInteraction++;
    }

    /**
     * Detects if a specific action dominates recent memory (>= 60% of entries).
     * Used for patterns like "work obsession" — but the engine doesn't know
     * what "work" is; it just checks frequency of any action ID.
     */
    public boolean detectObsession(String actionId) {
        if (shortTerm.size() < 3) return false;
        long count = shortTerm.stream()
                .filter(e -> e.actionId().equals(actionId))
                .count();
        return (double) count / shortTerm.size() >= 0.6;
    }

    /**
     * Detects obsession with any action (returns the dominant action ID or null).
     */
    public String detectAnyObsession() {
        if (shortTerm.size() < 3) return null;
        Map<String, Long> counts = shortTerm.stream()
                .collect(Collectors.groupingBy(MemoryEntry::actionId, Collectors.counting()));
        return counts.entrySet().stream()
                .filter(e -> (double) e.getValue() / shortTerm.size() >= 0.6)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    // Legacy compatibility methods used by ConditionEvaluator
    public boolean detectWorkObsession() {
        return detectObsession("GO_TO_WORK");
    }

    public boolean isBeingIgnored() {
        return daysSinceLastInteraction >= 3;
    }

    public int recentInteractionCount() {
        return shortTerm.size();
    }

    public int daysSinceLastInteraction() {
        return daysSinceLastInteraction;
    }

    public List<MemoryEntry> shortTermEntries() {
        return List.copyOf(shortTerm);
    }

    public List<MemoryEntry> longTermEntries() {
        return List.copyOf(longTerm);
    }

    /**
     * Checks if a specific action was performed in the last N entries.
     */
    public boolean recentlyDid(String actionId, int withinLast) {
        return shortTerm.stream()
                .limit(withinLast)
                .anyMatch(e -> e.actionId().equals(actionId));
    }
}
