package ru.lifegame.backend.infrastructure.spec;

import ru.lifegame.backend.domain.narrative.spec.NarrativeSpec;
import ru.lifegame.backend.domain.narrative.spec.SpecDeserializer;
import ru.lifegame.backend.domain.narrative.spec.SpecLoadException;
import ru.lifegame.backend.domain.narrative.spec.QuestSpec;
import ru.lifegame.backend.domain.narrative.parser.QuestSpecParser;

import java.io.InputStream;
import java.util.List;

/**
 * Adapter: wraps {@link QuestSpecParser} to implement
 * {@link SpecDeserializer}{@code <QuestSpecWrapper>}.
 *
 * <p>Parallel to {@link XmlNpcSpecDeserializer}: delegates all XML parsing
 * to the existing {@link QuestSpecParser}; this class only bridges the
 * {@link NarrativeSpec} contract.
 *
 * <p>Ref: java-developer-skill.md §7.
 */
public class XmlQuestSpecDeserializer implements SpecDeserializer<QuestSpecWrapper> {

    private final QuestSpecParser parser;

    public XmlQuestSpecDeserializer() {
        this.parser = new QuestSpecParser();
    }

    /** Constructor for tests / DI with a pre-configured parser. */
    public XmlQuestSpecDeserializer(QuestSpecParser parser) {
        this.parser = parser;
    }

    @Override
    public List<QuestSpecWrapper> deserialize(InputStream xmlStream, String sourceName)
            throws SpecLoadException {
        try {
            List<QuestSpec> specs = parser.parseAll(xmlStream, sourceName);
            return specs.stream()
                    .map(s -> new QuestSpecWrapper(s, deriveBlockId()))
                    .toList();
        } catch (SpecLoadException sle) {
            throw sle;
        } catch (Exception e) {
            throw new SpecLoadException(sourceName,
                    "Failed to parse Quest spec XML: " + sourceName, e);
        }
    }

    private static String deriveBlockId() {
        return "quests";
    }
}
