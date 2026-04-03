package ru.lifegame.assets.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.lifegame.assets.application.usecase.DocsPreviewUseCase;
import ru.lifegame.assets.application.usecase.GenerateLayeredAssetUseCase;
import ru.lifegame.assets.application.usecase.ScanMissingSpecsUseCase;
import ru.lifegame.assets.application.usecase.UnifyXmlSpecsUseCase;
import ru.lifegame.assets.domain.service.AssetGenerationService;
import ru.lifegame.assets.infrastructure.docs.DocsPreviewJsonWriterAdapter;
import ru.lifegame.assets.infrastructure.docs.DocsPreviewXmlParser;
import ru.lifegame.assets.infrastructure.generator.LayeredAssetGenerator;
import ru.lifegame.assets.infrastructure.generator.UniversalPixelRenderer;
import ru.lifegame.assets.infrastructure.parser.ClasspathSpecsSource;
import ru.lifegame.assets.infrastructure.parser.SpecsSource;
import ru.lifegame.assets.infrastructure.parser.XmlAssetSpecParser;
import ru.lifegame.assets.infrastructure.scanner.PromptDirectoryScanner;
import ru.lifegame.assets.infrastructure.writer.AtlasConfigWriter;
import ru.lifegame.assets.infrastructure.writer.PngLayerWriter;
import ru.lifegame.assets.infrastructure.writer.WebpAtlasWriter;

@Configuration
public class AssetGeneratorConfig {

    // Output mode resolved from --output-mode CLI arg or Spring property.
    // Defaults to STANDARD to preserve backward-compatibility.
    @Value("${assets.output-mode:STANDARD}")
    private String outputModeValue;

    @Bean
    public OutputMode outputMode() {
        try {
            return OutputMode.valueOf(outputModeValue.toUpperCase().replace('-', '_'));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Unknown assets.output-mode '" + outputModeValue
                    + "'. Allowed values: STANDARD, DOCS_PREVIEW.", e);
        }
    }

    @Bean
    public UniversalPixelRenderer universalPixelRenderer() {
        return new UniversalPixelRenderer();
    }

    @Bean
    public PngLayerWriter pngLayerWriter() {
        return new PngLayerWriter();
    }

    @Bean
    public WebpAtlasWriter webpAtlasWriter() {
        return new WebpAtlasWriter();
    }

    @Bean
    public AtlasConfigWriter atlasConfigWriter() {
        return new AtlasConfigWriter();
    }

    @Bean
    public XmlAssetSpecParser xmlAssetSpecParser() {
        return new XmlAssetSpecParser();
    }

    @Bean
    public PromptDirectoryScanner promptDirectoryScanner() {
        return new PromptDirectoryScanner();
    }

    @Bean
    public AssetGenerationService assetGenerationService(
            UniversalPixelRenderer renderer,
            PngLayerWriter pngWriter,
            WebpAtlasWriter atlasWriter,
            AtlasConfigWriter configWriter) {
        return new LayeredAssetGenerator(renderer, pngWriter, atlasWriter, configWriter);
    }

    @Bean
    public GenerateLayeredAssetUseCase generateLayeredAssetUseCase(
            AssetGenerationService service) {
        return new GenerateLayeredAssetUseCase(service);
    }

    @Bean
    public ScanMissingSpecsUseCase scanMissingSpecsUseCase(
            PromptDirectoryScanner scanner) {
        return new ScanMissingSpecsUseCase(scanner);
    }

    @Bean
    public UnifyXmlSpecsUseCase unifyXmlSpecsUseCase(
            XmlAssetSpecParser parser) {
        return new UnifyXmlSpecsUseCase(parser);
    }

    // ── Docs-preview beans ──────────────────────────────────────────────────

    @Bean
    public DocsPreviewXmlParser docsPreviewXmlParser() {
        return new DocsPreviewXmlParser();
    }

    @Bean
    public DocsPreviewJsonWriterAdapter docsPreviewJsonWriterAdapter() {
        return new DocsPreviewJsonWriterAdapter();
    }

    /**
     * The SpecsSource bean used by DocsPreviewUseCase.
     * Uses ClasspathSpecsSource by default; in disk mode override via -Dspecs.dir.
     */
    @Bean
    public SpecsSource specsSource() {
        return new ClasspathSpecsSource(
                Thread.currentThread().getContextClassLoader(),
                "asset-specs/",
                new XmlAssetSpecParser());
    }

    @Bean
    public DocsPreviewUseCase docsPreviewUseCase(
            SpecsSource specsSource,
            DocsPreviewXmlParser docsPreviewXmlParser) {
        return new DocsPreviewUseCase(specsSource, docsPreviewXmlParser);
    }
}
