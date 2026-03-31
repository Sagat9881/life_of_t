package ru.lifegame.backend.infrastructure.spec;

import ru.lifegame.backend.domain.narrative.spec.NarrativeSpec;
import ru.lifegame.backend.domain.npc.spec.NpcSpec;

import java.util.Objects;

/**
 * Transitional wrapper — kept for backward compatibility with callers that
 * were written before {@link NpcSpec} implemented {@link NarrativeSpec} directly.
 *
 * <p><b>Deprecated:</b> since TASK-BE-018 {@link NpcSpec} is itself a
 * {@link NarrativeSpec}. Prefer using {@code NpcSpec} directly.
 * This class will be removed once all call sites are migrated.
 *
 * <p>Ref: java-developer-skill.md §7, ADR-001 TASK-BE-018.
 *
 * @deprecated Use {@link NpcSpec} directly — it now implements {@link NarrativeSpec}.
 */
@Deprecated(since = "TASK-BE-018", forRemoval = true)
public record NpcSpecWrapper(NpcSpec spec, String blockId) implements NarrativeSpec {

    public NpcSpecWrapper {
        Objects.requireNonNull(spec, "spec must not be null");
        Objects.requireNonNull(blockId, "blockId must not be null");
    }

    @Override public String getId()      { return spec.getId(); }
    @Override public String getBlockId() { return spec.getBlockId(); }

    /** Returns the underlying {@link NpcSpec}. */
    public NpcSpec unwrap() { return spec; }
}
