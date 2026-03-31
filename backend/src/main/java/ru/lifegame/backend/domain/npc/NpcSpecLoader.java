package ru.lifegame.backend.domain.npc;

import ru.lifegame.backend.domain.npc.spec.NpcSpec;
import ru.lifegame.backend.infrastructure.spec.NpcSpecLoader;

import java.util.List;

/**
 * @deprecated Moved to {@link ru.lifegame.backend.infrastructure.spec.NpcSpecLoader}
 * as part of TASK-BE-017. Loading specs is an infrastructure concern;
 * domain must not depend on Spring's
 * {@code PathMatchingResourcePatternResolver} or hardcoded classpath strings
 * (java-developer-skill.md §5.1, §7, ADR-001).
 *
 * <p>This class is a forwarding stub kept for binary compatibility.
 * Remove once all callers are updated to use
 * {@link ru.lifegame.backend.infrastructure.spec.NpcSpecLoader}.
 */
@Deprecated(since = "TASK-BE-017", forRemoval = true)
public class NpcSpecLoader {

    private final ru.lifegame.backend.infrastructure.spec.NpcSpecLoader delegate
            = new ru.lifegame.backend.infrastructure.spec.NpcSpecLoader();

    /**
     * @deprecated Use {@link ru.lifegame.backend.infrastructure.spec.NpcSpecLoader#loadAll()}.
     */
    @Deprecated(since = "TASK-BE-017", forRemoval = true)
    public List<NpcSpec> loadAll() {
        return delegate.loadAll();
    }
}
