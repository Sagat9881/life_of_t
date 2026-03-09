package ru.lifegame.backend.domain.conflict.spec;

/**
 * Metadata for conflict: labels, descriptions, opponent info.
 */
public record ConflictMeta(
        String label,
        String description,
        String opponentId,      // "HUSBAND", "FATHER", "SELF"
        String category         // "relationship", "internal", "work"
) {
}
