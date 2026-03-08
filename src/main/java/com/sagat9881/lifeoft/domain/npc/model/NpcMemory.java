package com.sagat9881.lifeoft.domain.npc.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * NPC memory system: short-term (recent events) and long-term (flagged significant).
 * Named NPCs have full memory; filler NPCs have null memory.
 * Supports pattern detection for contextual NPC reactions.
 */
public class NpcMemory {

    private final int shortTermCapacity;
    private final List<MemoryEntry> shortTerm;
    private final List<MemoryEntry> longTerm;

    private NpcMemory(int shortTermCapacity) {
        this.shortTermCapacity = shortTermCapacity;
        this.shortTerm = new ArrayList<>();
        this.longTerm = new ArrayList<>();
    }

    public static NpcMemory create(int shortTermCapacity) {
        return new NpcMemory(shortTermCapacity);
    }

    /**
     * Record a player action or game event in short-term memory.
     */
    public void record(String eventId, int day) {
        MemoryEntry entry = new MemoryEntry(eventId, day, false);
        shortTerm.add(0, entry);
        if (shortTerm.size() > shortTermCapacity) {
            shortTerm.remove(shortTerm.size() - 1);
        }
    }

    /**
     * Flag a memory entry as significant (moves to long-term).
     */
    public void flagAsSignificant(String eventId) {
        shortTerm.stream()
                .filter(e -> e.eventId().equals(eventId))
                .findFirst()
                .ifPresent(e -> longTerm.add(new MemoryEntry(e.eventId(), e.day(), true)));
    }

    /**
     * Is the NPC being ignored? True if no player interaction in last N days.
     */
    public boolean isBeingIgnored(int dayThreshold) {
        if (shortTerm.isEmpty()) return true;
        int latestDay = shortTerm.stream().mapToInt(MemoryEntry::day).max().orElse(0);
        int currentDay = shortTerm.get(0).day();
        return (currentDay - latestDay) >= dayThreshold || shortTerm.isEmpty();
    }

    /**
     * Detect a pattern: e.g., player did the same action 3+ times recently.
     */
    public boolean detectPattern(String eventIdPattern) {
        long count = shortTerm.stream()
                .filter(e -> e.eventId().contains(eventIdPattern))
                .count();
        return count >= 3;
    }

    /**
     * Has there been a recent interaction matching the pattern within N entries?
     */
    public boolean hasRecentInteraction(String pattern, int withinEntries) {
        int limit = Math.min(withinEntries, shortTerm.size());
        for (int i = 0; i < limit; i++) {
            if ("any".equals(pattern) || shortTerm.get(i).eventId().contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Clean up entries older than given day.
     */
    public void cleanupOlderThan(int minDay) {
        shortTerm.removeIf(e -> e.day() < minDay);
    }

    public List<MemoryEntry> shortTermEntries() {
        return Collections.unmodifiableList(shortTerm);
    }

    public List<MemoryEntry> longTermEntries() {
        return Collections.unmodifiableList(longTerm);
    }

    /**
     * A single memory entry.
     */
    public record MemoryEntry(
            String eventId,
            int day,
            boolean significant
    ) {}
}
