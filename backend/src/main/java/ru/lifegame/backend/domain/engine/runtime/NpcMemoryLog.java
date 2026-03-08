package ru.lifegame.backend.domain.engine.runtime;

import java.util.*;

/**
 * Memory system for named NPCs.
 * Short-term: last N player actions observed.
 * Long-term: flagged significant events.
 * Pattern detection for contextual reactions.
 */
public class NpcMemoryLog {
    private static final int SHORT_TERM_SIZE = 10;

    private final Deque<MemoryEntry> shortTerm = new ArrayDeque<>();
    private final List<MemoryEntry> longTerm = new ArrayList<>();

    public record MemoryEntry(String actionId, int gameDay, int gameHour, String context) {}

    public void observe(String actionId, int day, int hour) {
        observe(actionId, day, hour, null);
    }

    public void observe(String actionId, int day, int hour, String context) {
        MemoryEntry entry = new MemoryEntry(actionId, day, hour, context);
        shortTerm.addLast(entry);
        if (shortTerm.size() > SHORT_TERM_SIZE) shortTerm.removeFirst();
    }

    public void flagAsSignificant(MemoryEntry entry) {
        longTerm.add(entry);
    }

    public boolean detectPattern(String actionId, int minOccurrences) {
        long count = shortTerm.stream()
                .filter(e -> actionId.equals(e.actionId()))
                .count();
        return count >= minOccurrences;
    }

    public boolean detectWorkObsession() {
        return detectPattern("GO_TO_WORK", 3);
    }

    public boolean isBeingIgnored(String interactionAction, int daysSince) {
        return shortTerm.stream()
                .noneMatch(e -> interactionAction.equals(e.actionId()));
    }

    public int daysSinceAction(String actionId, int currentDay) {
        return shortTerm.stream()
                .filter(e -> actionId.equals(e.actionId()))
                .mapToInt(MemoryEntry::gameDay)
                .max()
                .map(lastDay -> currentDay - lastDay)
                .orElse(999);
    }

    public List<MemoryEntry> recentEntries() {
        return List.copyOf(shortTerm);
    }

    public List<MemoryEntry> significantMemories() {
        return Collections.unmodifiableList(longTerm);
    }
}
