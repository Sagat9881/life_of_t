package ru.lifegame.backend.infrastructure.spec;

import ru.lifegame.backend.domain.narrative.spec.NarrativeSpec;
import ru.lifegame.backend.domain.npc.spec.NpcSpec;

import java.util.Objects;

/**
 * Transitional wrapper that makes {@link NpcSpec} comply with {@link NarrativeSpec}
 * without modifying the existing record.
 *
 * <p>This class is intentionally minimal. Once TASK-BE-018 is completed and
 * {@code NpcSpec} directly implements {@code NarrativeSpec}, this wrapper
 * can be removed and {@link XmlNpcSpecDeserializer} updated accordingly.
 *
 * <p>Ref: java-developer-skill.md §7 (infrastructure adapters),
 *         ADR-001 TASK-BE-018 follow-up.
 */
public record NpcSpecWrapper(NpcSpec spec, String blockId) implements NarrativeSpec {

    public NpcSpecWrapper {
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

    /** Convenience delegate — returns the underlying {@link NpcSpec}. */
    public NpcSpec unwrap() {
        return spec;
    }
}
