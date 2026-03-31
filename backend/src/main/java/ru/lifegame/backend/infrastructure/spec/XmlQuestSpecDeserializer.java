package ru.lifegame.backend.infrastructure.spec;

import ru.lifegame.backend.domain.narrative.spec.QuestSpec;
import ru.lifegame.backend.domain.narrative.spec.SpecDeserializer;
import ru.lifegame.backend.domain.narrative.spec.SpecLoadException;
import ru.lifegame.backend.infrastructure.spec.parser.QuestSpecParser;

import java.io.InputStream;
import java.util.List;

/**
 * Adapter: wraps {@link QuestSpecParser} to implement
 * {@link SpecDeserializer}{@code <QuestSpec>}.
 *
 * <p>Since TASK-BE-018 {@link QuestSpec} implements {@link
 * ru.lifegame.backend.domain.narrative.spec.NarrativeSpec} directly.
 *
 * <p>Uses {@link QuestSpecParser} from {@code infrastructure/spec/parser/}
 * (moved from domain per TASK-BE-026).
 *
 * <p>Ref: java-developer-skill.md §7.
 */
public class XmlQuestSpecDeserializer implements SpecDeserializer<QuestSpec> {

    private final QuestSpecParser parser;

    public XmlQuestSpecDeserializer() {
        this.parser = new QuestSpecParser();
    }

    public XmlQuestSpecDeserializer(QuestSpecParser parser) {
        this.parser = parser;
    }

    @Override
    public List<QuestSpec> deserialize(InputStream xmlStream, String sourceName)
            throws SpecLoadException {
        try {
            return parser.parseAll(xmlStream, sourceName);
        } catch (SpecLoadException sle) {
            throw sle;
        } catch (Exception e) {
            throw new SpecLoadException(sourceName,
                    "Failed to parse Quest spec XML: " + sourceName, e);
        }
    }
}
