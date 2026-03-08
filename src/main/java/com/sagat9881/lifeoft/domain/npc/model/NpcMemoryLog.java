package com.sagat9881.lifeoft.domain.npc.model;

import java.util.*;

/**
 * NPC memory: short-term (recent actions) + long-term (flagged significant events).
 * Used by Utility AI for pattern detection.
 * 
 * Named NPCs: full memory with pattern detection.
 * Filler NPCs: disabled (no memory).
 */
public class NpcMemoryLog {

    private final boolean enabled;
    private final int maxShortTerm;
    private final Deque<MemoryEntry> shortTerm;
    private final List<MemoryEntry> longTerm;
    private int lastInteractionDay = -1;

    public NpcMemoryLog(int maxShortTerm) {
        this.enabled = true;
        this.maxShortTerm = maxShortTerm;
        this.shortTerm = new ArrayDeque<>(maxShortTerm);
        this.longTerm = new ArrayList<>();
    }

    private NpcMemoryLog() {
        this.enabled = false;
        this.maxShortTerm = 0;
        this.shortTerm = new ArrayDeque<>();
        this.longTerm = new ArrayList<>();
    }

    public static NpcMemoryLog disabled() {
        return new NpcMemoryLog();
    }

    public void recordAction(String actionId, int day) {
        if (!enabled) return;
        MemoryEntry entry = new MemoryEntry(actionId, day, false);
        shortTerm.addFirst(entry);
        while (shortTerm.size() > maxShortTerm) {
            shortTerm.removeLast();
        }
        lastInteractionDay = day;
    }

    public void recordSignificant(String actionId, int day) {
        if (!enabled) return;
        recordAction(actionId, day);
        longTerm.add(new MemoryEntry(actionId, day, true));
    }

    public boolean hasInteractionToday() {
        return lastInteractionDay >= 0;
    }

    public void resetDailyInteraction() {
        lastInteractionDay = -1;
    }

    /**
     * Detect if player is repeating the same action type excessively.
     */
    public boolean detectObsession(String actionPrefix, int threshold) {
        if (!enabled) return false;
        long count = shortTerm.stream()
                .filter(e -> e.actionId().startsWith(actionPrefix))
                .count();
        return count >= threshold;
    }

    /**
     * Check if NPC is being ignored (no interaction for N days).
     */
    public boolean isBeingIgnored(int currentDay, int dayThreshold) {
        if (!enabled) return false;
        if (shortTerm.isEmpty()) return currentDay >= dayThreshold;
        int lastDay = shortTerm.peekFirst().day();
        return (currentDay - lastDay) >= dayThreshold;
    }

    /**
     * Count how many times a specific action appeared in short-term memory.
     */
    public int countAction(String actionId) {
        return (int) shortTerm.stream()
                .filter(e -> e.actionId().equals(actionId))
                .count();
    }

    public List<MemoryEntry> recentEntries(int count) {
        List<MemoryEntry> result = new ArrayList<>();
        Iterator<MemoryEntry> it = shortTerm.iterator();
        while (it.hasNext() && result.size() < count) {
            result.add(it.next());
        }
        return result;
    }

    public List<MemoryEntry> significantEvents() {
        return Collections.unmodifiableList(longTerm);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public record MemoryEntry(String actionId, int day, boolean significant) {
    }
}
