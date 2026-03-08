package com.sagat9881.lifeoft.domain.npc;

import java.util.*;

/**
 * NPC memory system — tracks player actions for contextual reactions.
 * Named NPCs have full memory (short-term + long-term).
 * Filler NPCs have memory disabled.
 *
 * Short-term: last N events (sliding window).
 * Long-term: significant events flagged during consolidation.
 */
public class NpcMemory {

    private final boolean enabled;
    private final int shortTermSize;
    private final Deque<String> shortTerm;
    private final List<String> longTerm;

    private NpcMemory(boolean enabled, int shortTermSize) {
        this.enabled = enabled;
        this.shortTermSize = shortTermSize;
        this.shortTerm = new ArrayDeque<>();
        this.longTerm = new ArrayList<>();
    }

    /**
     * Create an active memory with given short-term capacity.
     */
    public static NpcMemory create(int shortTermSize) {
        return new NpcMemory(true, shortTermSize);
    }

    /**
     * Create a disabled memory (for filler NPCs).
     */
    public static NpcMemory disabled() {
        return new NpcMemory(false, 0);
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Record an event in short-term memory.
     * Oldest events are evicted when capacity is reached.
     */
    public void recordEvent(String event) {
        if (!enabled) return;
        shortTerm.addLast(event);
        if (shortTerm.size() > shortTermSize) {
            shortTerm.removeFirst();
        }
    }

    /**
     * Check if player has been ignoring this NPC.
     * True if no "player:" events in short-term memory.
     */
    public boolean isBeingIgnored() {
        if (!enabled) return false;
        return shortTerm.stream()
                .noneMatch(e -> e.startsWith("player:"));
    }

    /**
     * Detect work obsession — player did work-related actions
     * 3+ times in recent memory.
     */
    public boolean detectWorkObsession() {
        if (!enabled) return false;
        long workCount = shortTerm.stream()
                .filter(e -> e.contains("WORK") || e.contains("work"))
                .count();
        return workCount >= 3;
    }

    /**
     * Check if a specific pattern exists in recent memory.
     * Used by ConditionEvaluator for XML-defined conditions.
     */
    public boolean hasPattern(String pattern) {
        if (!enabled) return false;
        return shortTerm.stream()
                .anyMatch(e -> e.contains(pattern));
    }

    /**
     * Count occurrences of a pattern in short-term memory.
     */
    public long countPattern(String pattern) {
        if (!enabled) return 0;
        return shortTerm.stream()
                .filter(e -> e.contains(pattern))
                .count();
    }

    /**
     * Consolidate: move significant short-term events to long-term.
     * Called at end of day.
     */
    public void consolidate() {
        if (!enabled) return;
        for (String event : shortTerm) {
            if (event.startsWith("initiated:") || event.contains("conflict")
                    || event.contains("quest") || event.contains("milestone")) {
                if (!longTerm.contains(event)) {
                    longTerm.add(event);
                }
            }
        }
    }

    /**
     * Get recent events (read-only view).
     */
    public List<String> recentEvents() {
        return List.copyOf(shortTerm);
    }

    /**
     * Get long-term memories (read-only view).
     */
    public List<String> longTermMemories() {
        return Collections.unmodifiableList(longTerm);
    }
}
