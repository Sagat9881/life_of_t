package ru.lifegame.assets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lifegame.assets.application.usecase.DocsPreviewUseCase;
import ru.lifegame.assets.config.OutputMode;
import ru.lifegame.assets.domain.model.asset.AssetSpec;
import ru.lifegame.assets.domain.model.docs.DocsPreviewResult;
import ru.lifegame.assets.domain.service.AssetGenerationService;
import ru.lifegame.assets.infrastructure.docs.DocsPreviewJsonWriterAdapter;
import ru.lifegame.assets.infrastructure.docs.DocsPreviewXmlParser;
import ru.lifegame.assets.infrastructure.generator.LayeredAssetGenerator;
import ru.lifegame.assets.infrastructure.generator.UniversalPixelRenderer;
import ru.lifegame.assets.infrastructure.parser.*;
import ru.lifegame.assets.infrastructure.scanner.EntityRef;
import ru.lifegame.assets.infrastructure.scanner.PromptDirectoryScanner;
import ru.lifegame.assets.infrastructure.scanner.SpecsManifestReader;
import ru.lifegame.assets.infrastructure.writer.AtlasConfigWriter;
import ru.lifegame.assets.infrastructure.writer.PngLayerWriter;
import ru.lifegame.assets.infrastructure.writer.WebpAtlasWriter;

import java.nio.file.Path;
import java.util.List;

/**
 * CLI entry point for asset generation.
 *
 * <p>Two modes, selected by the presence of the {@code specs.dir} system property:
 * <ul>
 *   <li><b>Disk mode</b> ({@code -Dspecs.dir=/path/to/asset-specs}).</li>
 *   <li><b>Classpath mode</b> (no {@code specs.dir}).</li>
 * </ul>
 *
 * <p>Output mode is selected via the system property
 * {@code -Dassets.output-mode=docs-preview} (default: {@code standard}):
 * <ul>
 *   <li>{@code standard} — generates PNG + sprite-atlas.json (original behaviour).</li>
 *   <li>{@code docs-preview} — generates {@code docs-preview.json} only; no PNG output.
 *       Reads every non-abstract entity from {@code specs-manifest.xml} and writes one
 *       JSON entry per entity. Zero hardcoded entity IDs (ADR-001).</li>
 * </ul>
 *
 * <p>Output directory is taken from the first CLI argument, or from the
 * {@code output.dir} system property, defaulting to {@code target/generated-assets}.
 */
public class AssetGeneratorRunner {

    private static final Logger log = LoggerFactory.getLogger(AssetGeneratorRunner.class);

    public static void main(String[] args) throws Exception {
        String specsDirProp  = System.getProperty("specs.dir");
        String outputModeProp = System.getProperty("assets.output-mode", "standard");
        Path outputDir = args.length > 0
                ? Path.of(args[0])
                : Path.of(System.getProperty("output.dir", "target/generated-assets"));

        OutputMode mode = resolveOutputMode(outputModeProp);
        log.info("Output mode: {}", mode);

        SpecsSource source = buildSpecsSource(specsDirProp);

        if (mode == OutputMode.DOCS_PREVIEW) {
            runDocsPreview(source, outputDir);
        } else {
            runStandard(source, specsDirProp, outputDir);
        }
    }

    // ── docs-preview mode ───────────────────────────────────────────────────

    private static void runDocsPreview(SpecsSource source, Path outputDir) throws Exception {
        log.info("Asset generation started in DOCS_PREVIEW mode. output={}", outputDir);

        DocsPreviewUseCase useCase = new DocsPreviewUseCase(source, new DocsPreviewXmlParser());
        DocsPreviewResult result = useCase.execute();

        DocsPreviewJsonWriterAdapter writer = new DocsPreviewJsonWriterAdapter();
        Path target = writer.write(result, outputDir);

        log.info("DOCS_PREVIEW complete. {} entities → {}", result.descriptors().size(), target);
    }

    // ── standard mode ──────────────────────────────────────────────────────────

    private static void runStandard(SpecsSource source, String specsDirProp, Path outputDir) {
        log.info("Asset generation started in STANDARD mode. output={}", outputDir);

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

        int totalFiles  = 0;
        int entityCount = 0;
        int skipped     = 0;
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

    // ── shared helpers ───────────────────────────────────────────────────────────────

    private static OutputMode resolveOutputMode(String value) {
        try {
            return OutputMode.valueOf(value.toUpperCase().replace('-', '_'));
        } catch (IllegalArgumentException e) {
            log.warn("Unknown assets.output-mode='{}'; falling back to STANDARD.", value);
            return OutputMode.STANDARD;
        }
    }

    private static SpecsSource buildSpecsSource(String specsDirProp) {
        if (specsDirProp != null) {
            log.info("Specs source: DISK [{}]", specsDirProp);
            return new DiskSpecsSource(Path.of(specsDirProp), new XmlAssetSpecParser());
        }
        log.info("Specs source: CLASSPATH [asset-specs/]");
        return new ClasspathSpecsSource(
                Thread.currentThread().getContextClassLoader(),
                "asset-specs/",
                new XmlAssetSpecParser());
    }

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
