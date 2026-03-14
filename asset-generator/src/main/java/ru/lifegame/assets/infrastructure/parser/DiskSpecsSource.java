package ru.lifegame.assets.infrastructure.parser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * {@link SpecsSource} backed by the local filesystem.
 */
public class DiskSpecsSource implements SpecsSource {

    private final Path specsRoot;
    private final XmlAssetSpecParser parser;

    public DiskSpecsSource(Path specsRoot, XmlAssetSpecParser parser) {
        this.specsRoot = specsRoot;
        this.parser = parser;
    }

    @Override
    public InputStream openSpec(String relativePath) throws IOException {
        return Files.newInputStream(specsRoot.resolve(relativePath));
    }

    @Override
    public boolean specExists(String relativePath) {
        return Files.exists(specsRoot.resolve(relativePath));
    }

    @Override
    public Map<String, String> extractColorVars(String relativePath) {
        return parser.extractColorVarsFromFile(specsRoot.resolve(relativePath));
    }
}
