package ru.lifegame.assets.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.lifegame.assets.application.usecase.GenerateLayeredAssetUseCase;
import ru.lifegame.assets.domain.service.AssetGenerationService;
import ru.lifegame.assets.infrastructure.generator.LayeredAssetGenerator;
import ru.lifegame.assets.infrastructure.generator.UniversalPixelRenderer;
import ru.lifegame.assets.infrastructure.writer.AtlasConfigWriter;
import ru.lifegame.assets.infrastructure.writer.PngLayerWriter;
import ru.lifegame.assets.infrastructure.writer.WebpAtlasWriter;

@Configuration
public class AssetGeneratorConfig {

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
}
