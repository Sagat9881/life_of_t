package ru.lifegame.assets.infrastructure.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * {@link SpecsSource} backed by the classloader — supports nested JARs and
 * standard classpath resources without any filesystem access.
 */
public class ClasspathSpecsSource implements SpecsSource {

    private static final Logger log = LoggerFactory.getLogger(ClasspathSpecsSource.class);

    private final ClassLoader classLoader;
    private final String resourcePrefix;
    private final XmlAssetSpecParser parser;

    /**
     * @param classLoader    classloader to resolve resources from
     * @param resourcePrefix prefix prepended to every relative path before lookup,
     *                       e.g. {@code "asset-specs/"} (must end with {@code /})
     * @param parser         parser instance used for color-var extraction
     */
    public ClasspathSpecsSource(ClassLoader classLoader, String resourcePrefix,
                                XmlAssetSpecParser parser) {
        this.classLoader = classLoader;
        this.resourcePrefix = resourcePrefix.endsWith("/") ? resourcePrefix : resourcePrefix + "/";
        this.parser = parser;
    }

    @Override
    public InputStream openSpec(String relativePath) throws IOException {
        String resource = resourcePrefix + relativePath;
        InputStream is = classLoader.getResourceAsStream(resource);
        if (is == null) {
            throw new IOException("Classpath resource not found: " + resource);
        }
        return is;
    }

    @Override
    public boolean specExists(String relativePath) {
        return classLoader.getResource(resourcePrefix + relativePath) != null;
    }

    @Override
    public Map<String, String> extractColorVars(String relativePath) {
        String resource = resourcePrefix + relativePath;
        try (InputStream is = classLoader.getResourceAsStream(resource)) {
            if (is == null) return Map.of();
            return parser.extractColorVarsFromStream(is);
        } catch (Exception e) {
            log.warn("Failed to extract color vars from classpath resource {}: {}", resource, e.getMessage());
            return Map.of();
        }
    }
}
