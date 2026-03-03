package ru.lifegame.assets.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.lifegame.assets.application.usecase.GenerateLayeredAssetUseCase;
import ru.lifegame.assets.application.usecase.ScanMissingSpecsUseCase;
import ru.lifegame.assets.application.usecase.UnifyXmlSpecsUseCase;
import ru.lifegame.assets.domain.service.AssetGenerationService;
import ru.lifegame.assets.infrastructure.generator.LayeredAssetGenerator;
import ru.lifegame.assets.infrastructure.parser.XmlAssetSpecParser;
import ru.lifegame.assets.infrastructure.scanner.PromptDirectoryScanner;
import ru.lifegame.assets.infrastructure.writer.AtlasConfigWriter;
import ru.lifegame.assets.infrastructure.writer.PngLayerWriter;
import ru.lifegame.assets.infrastructure.writer.WebpAtlasWriter;

/**
 * Spring {@link Configuration} that wires together all asset-generator beans.
 *
 * <p>Explicit {@code @Bean} declarations are used here (rather than relying
 * solely on {@code @Component} scanning) so that the wiring is visible in
 * one place and can be easily swapped in tests.</p>
 */
@Configuration
public class AssetGeneratorConfiguration {

    @Bean
    public XmlAssetSpecParser xmlAssetSpecParser() {
        return new XmlAssetSpecParser();
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
    public LayeredAssetGenerator layeredAssetGenerator(PngLayerWriter pngWriter,
                                                        WebpAtlasWriter atlasWriter,
                                                        AtlasConfigWriter configWriter) {
        return new LayeredAssetGenerator(pngWriter, atlasWriter, configWriter);
    }

    @Bean
    public AssetGenerationService assetGenerationService(LayeredAssetGenerator generator) {
        return generator;
    }

    @Bean
    public PromptDirectoryScanner promptDirectoryScanner() {
        return new PromptDirectoryScanner();
    }

    @Bean
    public GenerateLayeredAssetUseCase generateLayeredAssetUseCase(
            XmlAssetSpecParser parser, AssetGenerationService service) {
        return new GenerateLayeredAssetUseCase(parser, service);
    }

    @Bean
    public ScanMissingSpecsUseCase scanMissingSpecsUseCase(PromptDirectoryScanner scanner) {
        return new ScanMissingSpecsUseCase(scanner);
    }

    @Bean
    public UnifyXmlSpecsUseCase unifyXmlSpecsUseCase(PromptDirectoryScanner scanner,
                                                      XmlAssetSpecParser parser) {
        return new UnifyXmlSpecsUseCase(scanner, parser);
    }
}
