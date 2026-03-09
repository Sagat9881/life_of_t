package ru.lifegame.backend.domain.conflict.spec;

import java.util.List;

/**
 * Data-driven conflict specification loaded from conflicts.xml.
 * Replaces hardcoded trigger classes with declarative XML config.
 */
public record ConflictSpec(
        String id,
        ConflictMeta meta,
        ConflictTriggerSpec trigger,
        List<ConflictTacticSpec> tactics
) {
}
