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
 * Spring configuration for the asset generator module.
 * Wires up all infrastructure and use-case beans.
 */
@Configuration
public class AssetGeneratorConfiguration {

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
            PngLayerWriter pngLayerWriter,
            WebpAtlasWriter webpAtlasWriter,
            AtlasConfigWriter atlasConfigWriter) {
        return new LayeredAssetGenerator(pngLayerWriter, webpAtlasWriter, atlasConfigWriter);
    }

    @Bean
    public GenerateLayeredAssetUseCase generateLayeredAssetUseCase(
            AssetGenerationService assetGenerationService) {
        return new GenerateLayeredAssetUseCase(assetGenerationService);
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
}
