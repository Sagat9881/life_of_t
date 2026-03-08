package ru.lifegame.assets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lifegame.assets.domain.model.asset.AssetSpec;
import ru.lifegame.assets.domain.service.AssetGenerationService;
import ru.lifegame.assets.infrastructure.generator.LayeredAssetGenerator;
import ru.lifegame.assets.infrastructure.generator.UniversalPixelRenderer;
import ru.lifegame.assets.infrastructure.parser.XmlAssetSpecParser;
import ru.lifegame.assets.infrastructure.scanner.PromptDirectoryScanner;
import ru.lifegame.assets.infrastructure.writer.AtlasConfigWriter;
import ru.lifegame.assets.infrastructure.writer.PngLayerWriter;
import ru.lifegame.assets.infrastructure.writer.WebpAtlasWriter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * CLI entry point for asset generation.
 * Recursively scans asset-specs/ for ALL visual-specs.xml files,
 * generating assets into target/generated-assets/.
 * Output directory structure mirrors the input structure.
 * Supports abstract spec inheritance via extends= attribute.
 *
 * Usage: java -cp ... ru.lifegame.assets.AssetGeneratorRunner [specsDir] [outputDir]
 */
public class AssetGeneratorRunner {

    private static final Logger log = LoggerFactory.getLogger(AssetGeneratorRunner.class);

    public static void main(String[] args) {
        Path specsDir = args.length > 0
                ? Path.of(args[0])
                : Path.of("asset-specs");
        Path outputDir = args.length > 1
                ? Path.of(args[1])
                : Path.of("target/generated-assets");

        log.info("Asset generation started. specs={}, output={}", specsDir, outputDir);

        // Pass specsDir to parser for inheritance resolution
        XmlAssetSpecParser parser = new XmlAssetSpecParser(specsDir);
        UniversalPixelRenderer renderer = new UniversalPixelRenderer();
        AssetGenerationService generator = new LayeredAssetGenerator(
                renderer,
                new PngLayerWriter(),
                new WebpAtlasWriter(),
                new AtlasConfigWriter());

        PromptDirectoryScanner scanner = new PromptDirectoryScanner();
        List<Path> entityDirs = scanner.discoverEntities(specsDir);

        if (entityDirs.isEmpty()) {
            log.warn("No entity directories with visual-specs.xml found under {}", specsDir);
            return;
        }

        int totalFiles = 0;
        int entityCount = 0;
        int skipped = 0;
        for (Path entityDir : entityDirs) {
            // Skip abstract specs — they are templates, not renderable entities
            Path relativePath = specsDir.relativize(entityDir);
            if (relativePath.startsWith("abstract")) {
                log.debug("Skipping abstract spec: {}", entityDir);
                skipped++;
                continue;
            }

            Path specFile = entityDir.resolve("visual-specs.xml");
            if (!Files.exists(specFile)) {
                log.warn("No visual-specs.xml in {}, skipping", entityDir);
                continue;
            }

            try {
                AssetSpec spec = parser.parse(specFile);
                List<Path> generated = generator.generateAsset(spec, outputDir);
                totalFiles += generated.size();
                entityCount++;
                log.info("  [{}/{}] {}/{}: {} files",
                        entityCount, entityDirs.size() - skipped,
                        spec.entityType(), spec.entityName(), generated.size());
            } catch (Exception e) {
                log.error("Failed to generate asset for {}: {}", entityDir, e.getMessage(), e);
            }
        }

        log.info("Asset generation complete. Entities: {}, Skipped abstract: {}, Total files: {}",
                entityCount, skipped, totalFiles);
    }
}
