package ru.lifegame.backend.infrastructure.spec;

import ru.lifegame.backend.domain.narrative.spec.NarrativeSpec;
import ru.lifegame.backend.domain.narrative.spec.SpecDeserializer;
import ru.lifegame.backend.domain.narrative.spec.SpecLoadException;
import ru.lifegame.backend.domain.npc.spec.NpcSpec;
import ru.lifegame.backend.domain.narrative.parser.NpcSpecParser;

import java.io.InputStream;
import java.util.List;

/**
 * Adapter: wraps {@link NpcSpecParser} to implement
 * {@link SpecDeserializer}{@code <NpcSpec>}.
 *
 * <p>{@link NpcSpec} is not yet a {@link NarrativeSpec} because it is a
 * third-party record we cannot modify here. This adapter acts as a bridge
 * until {@code NpcSpec} implements the interface.
 *
 * <p>Ref: java-developer-skill.md §7 (Infrastructure implements Domain ports).
 *
 * <b>Note:</b> NpcSpec does not yet implement NarrativeSpec — a follow-up
 * task (TASK-BE-018) must add {@code implements NarrativeSpec} to NpcSpec
 * and return real {@code getId()} / {@code getBlockId()} values.
 * Until then this class uses a wrapper.
 */
public class XmlNpcSpecDeserializer implements SpecDeserializer<NpcSpecWrapper> {

    private final NpcSpecParser parser;

    public XmlNpcSpecDeserializer() {
        this.parser = new NpcSpecParser();
    }

    /** Constructor for tests / DI with a pre-configured parser. */
    public XmlNpcSpecDeserializer(NpcSpecParser parser) {
        this.parser = parser;
    }

    @Override
    public List<NpcSpecWrapper> deserialize(InputStream xmlStream, String sourceName)
            throws SpecLoadException {
        try {
            NpcSpec spec = parser.parse(xmlStream, sourceName);
            return List.of(new NpcSpecWrapper(spec, deriveBlockId()));
        } catch (SpecLoadException sle) {
            throw sle;
        } catch (Exception e) {
            throw new SpecLoadException(sourceName,
                    "Failed to parse NPC spec XML: " + sourceName, e);
        }
    }

    private static String deriveBlockId() {
        return "npc-behavior";
    }
}
