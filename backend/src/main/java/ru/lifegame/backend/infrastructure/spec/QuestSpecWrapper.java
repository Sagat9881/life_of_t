package ru.lifegame.backend.infrastructure.spec;

import ru.lifegame.backend.domain.narrative.spec.NarrativeSpec;
import ru.lifegame.backend.domain.narrative.spec.QuestSpec;

import java.util.Objects;

/**
 * Transitional wrapper that makes {@link QuestSpec} comply with {@link NarrativeSpec}.
 *
 * <p>Symmetric to {@link NpcSpecWrapper}. Once TASK-BE-018 adds
 * {@code implements NarrativeSpec} to {@code QuestSpec}, this wrapper is
 * superseded and can be deleted.
 *
 * <p>Ref: java-developer-skill.md §7, ADR-001 TASK-BE-018.
 */
public record QuestSpecWrapper(QuestSpec spec, String blockId) implements NarrativeSpec {

    public QuestSpecWrapper {
        Objects.requireNonNull(spec, "spec must not be null");
        Objects.requireNonNull(blockId, "blockId must not be null");
    }

    @Override
    public String getId() {
        return spec.id();
    }

    @Override
    public String getBlockId() {
        return blockId;
    }

    /** Convenience delegate — returns the underlying {@link QuestSpec}. */
    public QuestSpec unwrap() {
        return spec;
    }
}
