package ru.lifegame.assets.infrastructure.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Abstraction over the physical location of asset spec files.
 * Implementations may read from disk ({@link DiskSpecsSource}) or from a
 * classpath / nested JAR ({@link ClasspathSpecsSource}).
 */
public interface SpecsSource {

    /**
     * Opens a stream for the spec file at the given relative path.
     *
     * @param relativePath path relative to the specs root, e.g.
     *                     {@code "abstract/entities/human/visual-specs.xml"}
     * @return open InputStream — caller is responsible for closing
     * @throws IOException if the resource cannot be opened
     */
    InputStream openSpec(String relativePath) throws IOException;

    /**
     * Returns {@code true} if the spec file at the given relative path exists.
     *
     * @param relativePath path relative to the specs root
     */
    boolean specExists(String relativePath);

    /**
     * Extracts {@code $}-prefixed color variable mappings from the spec at
     * the given relative path.  Returns an empty map on any parse error.
     *
     * @param relativePath path relative to the specs root
     * @return map of {@code "$varName"} → {@code "#hexValue"}
     */
    Map<String, String> extractColorVars(String relativePath);
}
