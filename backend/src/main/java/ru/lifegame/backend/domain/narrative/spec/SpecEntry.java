package ru.lifegame.backend.domain.narrative.spec;

import java.util.Objects;

/**
 * Value Object — describes a single narrative entity within a block manifest.
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code entityId}   — machine-readable identifier, e.g. {@code "morning_routine"}</li>
 *   <li>{@code entityType} — block-level type token, e.g. {@code "quest"}, {@code "npc"}</li>
 *   <li>{@code specPath}   — classpath-relative path to the spec file,
 *                            e.g. {@code "narrative/quest/morning_routine/spec.xml"}</li>
 * </ul>
 *
 * <p>Immutable by construction (record). No dependencies outside domain.
 *
 * <p>Ref: java-developer-skill.md §5.5 (all structure from manifest, not filesystem).
 *         TASK-BE-016.
 */
public record SpecEntry(
        String entityId,
        String entityType,
        String specPath
) {
    public SpecEntry {
        Objects.requireNonNull(entityId,   "entityId must not be null");
        Objects.requireNonNull(entityType, "entityType must not be null");
        Objects.requireNonNull(specPath,   "specPath must not be null");
        if (entityId.isBlank())   throw new IllegalArgumentException("entityId must not be blank");
        if (entityType.isBlank()) throw new IllegalArgumentException("entityType must not be blank");
        if (specPath.isBlank())   throw new IllegalArgumentException("specPath must not be blank");
    }
}
