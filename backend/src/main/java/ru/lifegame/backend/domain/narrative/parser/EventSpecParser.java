package ru.lifegame.backend.domain.narrative.parser;

import ru.lifegame.backend.domain.narrative.spec.EventSpec;

import java.io.File;
import java.io.InputStream;

/**
 * @deprecated Moved to {@link ru.lifegame.backend.infrastructure.spec.parser.EventSpecParser}
 * as part of TASK-BE-026. XML parsing is an infrastructure concern
 * (java-developer-skill.md §7). This class is a forwarding stub kept for
 * binary compatibility. Remove once all callers are updated.
 */
@Deprecated(since = "TASK-BE-026", forRemoval = true)
public class EventSpecParser {

    private final ru.lifegame.backend.infrastructure.spec.parser.EventSpecParser delegate
            = new ru.lifegame.backend.infrastructure.spec.parser.EventSpecParser();

    /** @deprecated Use {@code infrastructure.spec.parser.EventSpecParser} instead. */
    @Deprecated(since = "TASK-BE-026", forRemoval = true)
    public EventSpec parse(InputStream xmlStream, String filename) throws Exception {
        return delegate.parse(xmlStream, filename);
    }

    /** @deprecated Use {@code infrastructure.spec.parser.EventSpecParser} instead. */
    @Deprecated(since = "TASK-BE-026", forRemoval = true)
    public EventSpec parse(File xmlFile) throws Exception {
        return delegate.parse(xmlFile);
    }
}
