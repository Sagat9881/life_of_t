package ru.lifegame.backend.domain.narrative.spec;

import java.util.List;
import java.util.Objects;

/**
 * Value Object — represents a parsed narrative block manifest.
 *
 * <p>A manifest describes one "block" of the narrative (e.g. {@code quests},
 * {@code npc}, {@code conflicts}, {@code world-events}) and lists all the
 * entities that belong to it, together with their spec file paths.
 *
 * <p>The backend obtains block structure exclusively from manifests —
 * never by scanning subdirectory trees directly (java-developer-skill.md §5.5).
 *
 * <p>Immutable by construction (record). No outbound dependencies.
 *
 * <p>Ref: java-developer-skill.md §5.5. TASK-BE-016.
 */
public record BlockManifest(
        String blockId,
        String version,
        List<SpecEntry> entries
) {
    public BlockManifest {
        Objects.requireNonNull(blockId, "blockId must not be null");
        Objects.requireNonNull(version, "version must not be null");
        Objects.requireNonNull(entries, "entries must not be null");
        if (blockId.isBlank()) throw new IllegalArgumentException("blockId must not be blank");
        entries = List.copyOf(entries); // defensive copy + immutability
    }

    /** Returns all entries matching a given entityType filter. */
    public List<SpecEntry> entriesByType(String entityType) {
        Objects.requireNonNull(entityType, "entityType must not be null");
        return entries.stream()
                .filter(e -> entityType.equals(e.entityType()))
                .toList();
    }

    /** Returns true if this manifest has no entries. */
    public boolean isEmpty() { return entries.isEmpty(); }

    /** Convenience: number of entities described by this manifest. */
    public int size() { return entries.size(); }
}
