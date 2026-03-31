package ru.lifegame.backend.infrastructure.spec;

import ru.lifegame.backend.domain.narrative.spec.NarrativeSpec;
import ru.lifegame.backend.domain.narrative.spec.QuestSpec;

import java.util.Objects;

/**
 * Transitional wrapper — kept for backward compatibility with callers that
 * were written before {@link QuestSpec} implemented {@link NarrativeSpec} directly.
 *
 * <p><b>Deprecated:</b> since TASK-BE-018 {@link QuestSpec} is itself a
 * {@link NarrativeSpec}. Prefer using {@code QuestSpec} directly.
 * This class will be removed once all call sites are migrated.
 *
 * <p>Ref: java-developer-skill.md §7, ADR-001 TASK-BE-018.
 *
 * @deprecated Use {@link QuestSpec} directly — it now implements {@link NarrativeSpec}.
 */
@Deprecated(since = "TASK-BE-018", forRemoval = true)
public record QuestSpecWrapper(QuestSpec spec, String blockId) implements NarrativeSpec {

    public QuestSpecWrapper {
        Objects.requireNonNull(spec, "spec must not be null");
        Objects.requireNonNull(blockId, "blockId must not be null");
    }

    @Override public String getId()      { return spec.getId(); }
    @Override public String getBlockId() { return spec.getBlockId(); }

    /** Returns the underlying {@link QuestSpec}. */
    public QuestSpec unwrap() { return spec; }
}
