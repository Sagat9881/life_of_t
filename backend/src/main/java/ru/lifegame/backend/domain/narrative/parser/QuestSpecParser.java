package ru.lifegame.backend.domain.narrative.parser;

import ru.lifegame.backend.domain.narrative.spec.QuestSpec;

import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * @deprecated Moved to {@link ru.lifegame.backend.infrastructure.spec.parser.QuestSpecParser}
 * as part of TASK-BE-026. XML parsing is an infrastructure concern
 * (java-developer-skill.md §7). This class is a forwarding stub kept for
 * binary compatibility. Remove once all callers are updated.
 */
@Deprecated(since = "TASK-BE-026", forRemoval = true)
public class QuestSpecParser {

    private final ru.lifegame.backend.infrastructure.spec.parser.QuestSpecParser delegate
            = new ru.lifegame.backend.infrastructure.spec.parser.QuestSpecParser();

    /** @deprecated Use {@code infrastructure.spec.parser.QuestSpecParser} instead. */
    @Deprecated(since = "TASK-BE-026", forRemoval = true)
    public QuestSpec parseOne(InputStream xmlStream, String filename) throws Exception {
        return delegate.parseOne(xmlStream, filename);
    }

    /** @deprecated Use {@code infrastructure.spec.parser.QuestSpecParser} instead. */
    @Deprecated(since = "TASK-BE-026", forRemoval = true)
    public List<QuestSpec> parseAll(InputStream xmlStream, String filename) throws Exception {
        return delegate.parseAll(xmlStream, filename);
    }

    /** @deprecated Use {@code infrastructure.spec.parser.QuestSpecParser} instead. */
    @Deprecated(since = "TASK-BE-026", forRemoval = true)
    public List<QuestSpec> parseAll(File xmlFile) throws Exception {
        return delegate.parseAll(xmlFile);
    }
}
