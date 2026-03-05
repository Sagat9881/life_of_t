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
 * Discovers pre-generated pixel-art assets produced by the asset-generator module.
 * Assets are already built as PNG + atlas files in the output directory.
 */
@Service
public class DemoAssetService {

    private static final Logger log = LoggerFactory.getLogger(DemoAssetService.class);

    private final Path outputDir;
    private final Map<String, Path> assetPaths = new LinkedHashMap<>();
    private final List<AssetInfoDto> assetInfos = new ArrayList<>();

    public DemoAssetService(
            @Value("${demo.assets.output-dir:${user.dir}/asset-generator/target/generated-assets}") String outputDirStr) {
        this.outputDir = Path.of(outputDirStr).toAbsolutePath().normalize();
    }

    /**
     * Scans the output directory for generated assets and indexes them.
     */
    public void generateAll() {
        if (!Files.exists(outputDir)) {
            log.warn("Asset output directory does not exist: {}. "
                    + "Run 'mvn clean package' from project root first.", outputDir);
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
                            : prefix + "/" + entry.getFileName();
                    scanDirectory(entry, subPrefix);
                } else if (entry.toString().endsWith(".png")) {
                    String relativePath = prefix.isEmpty()
                            ? entry.getFileName().toString()
                            : prefix + "/" + entry.getFileName();
                    String id = relativePath.replace(".png", "");
                    assetPaths.put(id, entry);
                    assetInfos.add(new AssetInfoDto(
                            id,
                            "/generated-assets/" + relativePath,
                            0, 0, 1, 1
                    ));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to scan asset directory: " + dir, e);
        }
    }

    public List<AssetInfoDto> listAssetInfos() {
        return List.copyOf(assetInfos);
    }

    public Path resolveAssetPath(String id) {
        return assetPaths.get(id);
    }

    public Path getOutputDir() {
        return outputDir;
    }
}
