package ru.lifegame.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.lifegame.demo.dto.DemoDtos.AssetInfoDto;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Discovers pre-generated pixel-art assets produced by the asset-generator module
 * during {@code mvn compile}.
 *
 * <p>No runtime rendering — assets are already built as PNG + atlas files
 * in the output directory. This service simply indexes them and provides
 * metadata for the demo UI.</p>
 */
@Service
public class DemoAssetService {

    private static final Logger log = LoggerFactory.getLogger(DemoAssetService.class);

    private final Path outputDir;
    private final Map<String, Path> assetPaths = new LinkedHashMap<>();
    private final List<AssetInfoDto> assetInfos = new ArrayList<>();

    public DemoAssetService(
            @Value("${demo.assets.output-dir:${java.io.tmpdir}/life-of-t-assets}") String outputDirStr) {
        this.outputDir = Path.of(outputDirStr);
    }

    /**
     * Scans the output directory for generated assets and indexes them.
     * Idempotent — safe to call multiple times.
     */
    public void generateAll() {
        if (!Files.exists(outputDir)) {
            log.warn("Asset output directory does not exist: {}. "
                    + "Run 'mvn compile' on asset-generator first.", outputDir);
            return;
        }

        scanDirectory(outputDir, "");
        log.info("Indexed {} pre-generated assets from {}", assetPaths.size(), outputDir);
    }

    private void scanDirectory(Path dir, String prefix) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    String subPrefix = prefix.isEmpty()
                            ? entry.getFileName().toString()
                            : prefix + "/" + entry.getFileName().toString();
                    scanDirectory(entry, subPrefix);
                } else if (entry.toString().endsWith(".png") && !entry.toString().contains("atlas")) {
                    String id = prefix + "/" + entry.getFileName().toString()
                            .replace(".png", "");
                    assetPaths.put(id, entry);
                    assetInfos.add(new AssetInfoDto(
                            id,
                            "/generated-assets/" + prefix + "/" + entry.getFileName(),
                            0, 0, 1, 1  // static asset defaults
                    ));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to scan asset directory: " + dir, e);
        }
    }

    /** Returns metadata DTOs for all discovered assets. */
    public List<AssetInfoDto> listAssetInfos() {
        return List.copyOf(assetInfos);
    }

    /**
     * Resolves the filesystem path for a given asset id,
     * or {@code null} if not found.
     */
    public Path resolveAssetPath(String id) {
        return assetPaths.get(id);
    }

    /** Absolute path to the generated assets directory. */
    public Path getOutputDir() {
        return outputDir;
    }
}
