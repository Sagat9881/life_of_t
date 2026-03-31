package ru.lifegame.backend.domain.narrative.parser;

import ru.lifegame.backend.domain.npc.spec.NpcSpec;

import java.io.File;
import java.io.InputStream;

/**
 * @deprecated Moved to {@link ru.lifegame.backend.infrastructure.spec.parser.NpcSpecParser}
 * as part of TASK-BE-026. XML parsing is an infrastructure concern
 * (java-developer-skill.md §7). This class is a forwarding stub kept for
 * binary compatibility. Remove once all callers are updated.
 */
@Deprecated(since = "TASK-BE-026", forRemoval = true)
public class NpcSpecParser {

    private final ru.lifegame.backend.infrastructure.spec.parser.NpcSpecParser delegate
            = new ru.lifegame.backend.infrastructure.spec.parser.NpcSpecParser();

    /** @deprecated Use {@code infrastructure.spec.parser.NpcSpecParser} instead. */
    @Deprecated(since = "TASK-BE-026", forRemoval = true)
    public NpcSpec parse(InputStream xmlStream, String filename) throws Exception {
        return delegate.parse(xmlStream, filename);
    }

    /** @deprecated Use {@code infrastructure.spec.parser.NpcSpecParser} instead. */
    @Deprecated(since = "TASK-BE-026", forRemoval = true)
    public NpcSpec parse(File xmlFile) throws Exception {
        return delegate.parse(xmlFile);
    }
}
