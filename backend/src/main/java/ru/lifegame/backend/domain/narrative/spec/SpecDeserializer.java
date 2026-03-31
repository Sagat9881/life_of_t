package ru.lifegame.backend.domain.narrative.spec;

import java.io.InputStream;
import java.util.List;

/**
 * Port: contract for deserializing one or many {@link NarrativeSpec} instances
 * from an XML {@link InputStream}.
 *
 * <p>Defined in the domain layer so that the domain is independent of concrete
 * parsing technology (Dependency Inversion Principle).
 * Implementations live in {@code infrastructure/spec/} and are injected via
 * constructor into {@link ru.lifegame.backend.infrastructure.spec.SpecLoader}.
 *
 * <p>Ref: java-developer-skill.md §7 (Infrastructure implements Domain interfaces),
 *         §5.1 (no switch/if per entity type — caller selects the right
 *         deserializer, not this interface).
 *
 * @param <T> concrete spec type, must implement {@link NarrativeSpec}
 */
public interface SpecDeserializer<T extends NarrativeSpec> {

    /**
     * Deserializes all specs contained in the given stream.
     *
     * @param xmlStream  open stream of a single XML file; caller is responsible for closing it
     * @param sourceName human-readable source label used only in error messages (filename)
     * @return non-null, possibly empty list of parsed specs
     * @throws SpecLoadException if parsing fails for any reason
     */
    List<T> deserialize(InputStream xmlStream, String sourceName) throws SpecLoadException;
}
