package ru.lifegame.backend.domain.npc;

import java.util.*;

/**
 * Tracks what the player has been doing from NPC's perspective.
 * Enables context-aware reactions ("You've been working non-stop!").
 */
public class NpcMemory {
    private final LinkedList<MemoryEntry> entries = new LinkedList<>();
    private static final int MAX_ENTRIES = 10;

    public record MemoryEntry(int day, String actionCode, String detail) {}

    public void record(int day, String actionCode, String detail) {
        entries.addLast(new MemoryEntry(day, actionCode, detail));
        if (entries.size() > MAX_ENTRIES) entries.removeFirst();
    }

    public List<MemoryEntry> recent(int count) {
        int from = Math.max(0, entries.size() - count);
        return List.copyOf(entries.subList(from, entries.size()));
    }

    /**
     * Count how many times an action appeared in the last N entries.
     */
    public int countAction(String actionCode, int lastN) {
        return (int) recent(lastN).stream()
            .filter(e -> e.actionCode().equals(actionCode))
            .count();
    }

    /**
     * Detect if player is ignoring this NPC (no direct interaction in last N entries).
     */
    public boolean isBeingIgnored(Set<String> relevantActions, int lastN) {
        return recent(lastN).stream()
            .noneMatch(e -> relevantActions.contains(e.actionCode()));
    }

    /**
     * Detect work-obsession pattern (>60% of recent actions are work).
     */
    public boolean detectWorkObsession(int lastN) {
        List<MemoryEntry> r = recent(lastN);
        if (r.isEmpty()) return false;
        long workCount = r.stream().filter(e -> e.actionCode().equals("GO_TO_WORK")).count();
        return (double) workCount / r.size() > 0.6;
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public int size() {
        return entries.size();
    }
}