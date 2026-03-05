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
 * Scans asset-specs/ for XML specs, generates PNGs into target/generated-assets/.
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

        XmlAssetSpecParser parser = new XmlAssetSpecParser();
        UniversalPixelRenderer renderer = new UniversalPixelRenderer();
        AssetGenerationService generator = new LayeredAssetGenerator(
                renderer,
                new PngLayerWriter(),
                new WebpAtlasWriter(),
                new AtlasConfigWriter());

        PromptDirectoryScanner scanner = new PromptDirectoryScanner();
        List<Path> entityDirs = scanner.discoverEntities(specsDir);

        int totalFiles = 0;
        for (Path entityDir : entityDirs) {
            Path specFile = entityDir.resolve("visual-specs.xml");
            if (!Files.exists(specFile)) {
                log.warn("No visual-specs.xml in {}, skipping", entityDir);
                continue;
            }
            AssetSpec spec = parser.parse(specFile);
            List<Path> generated = generator.generateAsset(spec, outputDir);
            totalFiles += generated.size();
            log.info("  {}/{}: {} files", spec.entityType(), spec.entityName(), generated.size());
        }

        log.info("Asset generation complete. Total files: {}", totalFiles);
    }
}
