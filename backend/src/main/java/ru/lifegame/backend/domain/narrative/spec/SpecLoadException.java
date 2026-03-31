package ru.lifegame.backend.domain.narrative.spec;

/**
 * Typed exception thrown by {@link SpecDeserializer} and
 * {@link ru.lifegame.backend.infrastructure.spec.SpecLoader} when a spec
 * file cannot be found or parsed.
 *
 * <p>Using a typed exception instead of {@code NullPointerException} or raw
 * {@code RuntimeException} lets callers distinguish loading failures from
 * programming errors.
 *
 * <p>Ref: java-developer-skill.md §5.1 (no NPE leakage from loading code).
 */
public class SpecLoadException extends RuntimeException {

    private final String sourceName;

    public SpecLoadException(String sourceName, String message) {
        super("[" + sourceName + "] " + message);
        this.sourceName = sourceName;
    }

    public SpecLoadException(String sourceName, String message, Throwable cause) {
        super("[" + sourceName + "] " + message, cause);
        this.sourceName = sourceName;
    }

    /** The classpath pattern or filename that triggered this failure. */
    public String getSourceName() {
        return sourceName;
    }
}
