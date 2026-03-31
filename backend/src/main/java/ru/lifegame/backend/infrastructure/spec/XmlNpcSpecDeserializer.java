package ru.lifegame.backend.infrastructure.spec;

import ru.lifegame.backend.domain.narrative.spec.SpecDeserializer;
import ru.lifegame.backend.domain.narrative.spec.SpecLoadException;
import ru.lifegame.backend.domain.npc.spec.NpcSpec;
import ru.lifegame.backend.infrastructure.spec.parser.NpcSpecParser;

import java.io.InputStream;
import java.util.List;

/**
 * Adapter: wraps {@link NpcSpecParser} to implement
 * {@link SpecDeserializer}{@code <NpcSpec>}.
 *
 * <p>Since TASK-BE-018 {@link NpcSpec} implements {@link
 * ru.lifegame.backend.domain.narrative.spec.NarrativeSpec} directly,
 * so this deserializer works with {@code NpcSpec} without any wrapper.
 *
 * <p>Uses {@link NpcSpecParser} from {@code infrastructure/spec/parser/}
 * (moved from domain per TASK-BE-026).
 *
 * <p>Ref: java-developer-skill.md §7.
 */
public class XmlNpcSpecDeserializer implements SpecDeserializer<NpcSpec> {

    private final NpcSpecParser parser;

    public XmlNpcSpecDeserializer() {
        this.parser = new NpcSpecParser();
    }

    public XmlNpcSpecDeserializer(NpcSpecParser parser) {
        this.parser = parser;
    }

    @Override
    public List<NpcSpec> deserialize(InputStream xmlStream, String sourceName)
            throws SpecLoadException {
        try {
            return List.of(parser.parse(xmlStream, sourceName));
        } catch (SpecLoadException sle) {
            throw sle;
        } catch (Exception e) {
            throw new SpecLoadException(sourceName,
                    "Failed to parse NPC spec XML: " + sourceName, e);
        }
    }
}
