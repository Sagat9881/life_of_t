package ru.lifegame.assets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lifegame.assets.domain.model.asset.AssetSpec;
import ru.lifegame.assets.domain.service.AssetGenerationService;
import ru.lifegame.assets.infrastructure.generator.LayeredAssetGenerator;
import ru.lifegame.assets.infrastructure.generator.UniversalPixelRenderer;
import ru.lifegame.assets.infrastructure.parser.ClasspathSpecsSource;
import ru.lifegame.assets.infrastructure.parser.DiskSpecsSource;
import ru.lifegame.assets.infrastructure.parser.SpecsSource;
import ru.lifegame.assets.infrastructure.parser.XmlAssetSpecParser;
import ru.lifegame.assets.infrastructure.scanner.EntityRef;
import ru.lifegame.assets.infrastructure.scanner.PromptDirectoryScanner;
import ru.lifegame.assets.infrastructure.scanner.SpecsManifestReader;
import ru.lifegame.assets.infrastructure.writer.AtlasConfigWriter;
import ru.lifegame.assets.infrastructure.writer.PngLayerWriter;
import ru.lifegame.assets.infrastructure.writer.WebpAtlasWriter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * CLI entry point for asset generation.
 *
 * <p>Two modes, selected by the presence of the {@code specs.dir} system property:
 * <ul>
 *   <li><b>Disk mode</b> ({@code -Dspecs.dir=/path/to/asset-specs}): reads specs from
 *       the local filesystem via {@link DiskSpecsSource}.  Falls back to
 *       {@link PromptDirectoryScanner} if {@code specs-manifest.xml} is absent.</li>
 *   <li><b>Classpath mode</b> (no {@code specs.dir}): reads specs bundled inside the
 *       fat JAR (e.g. via the {@code life-of-t} game-content dependency) using
 *       {@link ClasspathSpecsSource} and requires {@code specs-manifest.xml} to be
 *       present on the classpath under {@code asset-specs/}.</li>
 * </ul>
 *
 * <p>Output directory is taken from the first CLI argument, or from the
 * {@code output.dir} system property, defaulting to {@code target/generated-assets}.
 */
public class AssetGeneratorRunner {

    private static final Logger log = LoggerFactory.getLogger(AssetGeneratorRunner.class);

    public static void main(String[] args) {
        String specsDirProp = System.getProperty("specs.dir");
        Path outputDir = args.length > 0
                ? Path.of(args[0])
                : Path.of(System.getProperty("output.dir", "target/generated-assets"));

        SpecsSource source;
        if (specsDirProp != null) {
            log.info("Specs source: DISK [{}]", specsDirProp);
            source = new DiskSpecsSource(Path.of(specsDirProp), new XmlAssetSpecParser());
        } else {
            log.info("Specs source: CLASSPATH [asset-specs/]");
            source = new ClasspathSpecsSource(
                    Thread.currentThread().getContextClassLoader(),
                    "asset-specs/",
                    new XmlAssetSpecParser());
        }

        log.info("Asset generation started. output={}", outputDir);

        XmlAssetSpecParser parser = new XmlAssetSpecParser(source);
        UniversalPixelRenderer renderer = new UniversalPixelRenderer();
        AssetGenerationService generator = new LayeredAssetGenerator(
                renderer,
                new PngLayerWriter(),
                new WebpAtlasWriter(),
                new AtlasConfigWriter());

        List<EntityRef> entities = resolveEntities(source, specsDirProp);

        if (entities.isEmpty()) {
            log.warn("No entity refs found — nothing to generate");
            return;
        }

        int totalFiles = 0;
        int entityCount = 0;
        int skipped = 0;
        int total = (int) entities.stream().filter(e -> !e.isAbstract()).count();

        for (EntityRef ref : entities) {
            if (ref.isAbstract()) {
                log.debug("Skipping abstract spec: {}", ref.path());
                skipped++;
                continue;
            }

            String specRelative = ref.path() + "/visual-specs.xml";
            if (!source.specExists(specRelative)) {
                log.warn("No visual-specs.xml for entity '{}' — skipping", ref.path());
                continue;
            }

            try {
                AssetSpec spec;
                try (var is = source.openSpec(specRelative)) {
                    spec = parser.parseFromStream(is);
                }
                List<Path> generated = generator.generateAsset(spec, outputDir);
                totalFiles += generated.size();
                entityCount++;
                log.info("  [{}/{}] {}/{}: {} files",
                        entityCount, total,
                        spec.entityType(), spec.entityName(), generated.size());
            } catch (Exception e) {
                log.error("Failed to generate asset for '{}': {}", ref.path(), e.getMessage(), e);
            }
        }

        log.info("Asset generation complete. Entities: {}, Skipped abstract: {}, Total files: {}",
                entityCount, skipped, totalFiles);
    }

    /**
     * Resolves the list of entity refs.
     * Prefers {@link SpecsManifestReader} (classpath-safe).
     * Falls back to {@link PromptDirectoryScanner} in disk mode when manifest is absent.
     */
    private static List<EntityRef> resolveEntities(SpecsSource source, String specsDirProp) {
        SpecsManifestReader manifestReader = new SpecsManifestReader(source);
        if (source.specExists("specs-manifest.xml")) {
            return manifestReader.readManifest();
        }

        if (specsDirProp != null) {
            log.warn("specs-manifest.xml not found — falling back to PromptDirectoryScanner");
            Path specsDir = Path.of(specsDirProp);
            PromptDirectoryScanner scanner = new PromptDirectoryScanner();
            List<Path> dirs = scanner.discoverEntities(specsDir);
            return dirs.stream()
                    .map(dir -> {
                        String rel = specsDir.relativize(dir).toString().replace('\\', '/');
                        boolean isAbstract = rel.startsWith("abstract");
                        return new EntityRef(rel, isAbstract);
                    })
                    .toList();
        }

        log.error("specs-manifest.xml is required in classpath mode but was not found");
        return List.of();
    }
}
