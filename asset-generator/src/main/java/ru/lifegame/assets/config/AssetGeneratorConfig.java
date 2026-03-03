package ru.lifegame.assets.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.lifegame.assets.AssetGenerator;
import ru.lifegame.assets.GeneratorRegistry;
import ru.lifegame.assets.sprite.LpcSpriteCompositor;
import ru.lifegame.assets.texture.ProceduralTextureGenerator;

import java.util.List;

@Configuration
public class AssetGeneratorConfig {

    @Bean
    public ProceduralTextureGenerator proceduralTextureGenerator() {
        return new ProceduralTextureGenerator();
    }

    @Bean
    public LpcSpriteCompositor lpcSpriteCompositor() {
        return new LpcSpriteCompositor();
    }

    @Bean
    public GeneratorRegistry generatorRegistry(
            ProceduralTextureGenerator textureGenerator,
            LpcSpriteCompositor spriteCompositor) {

        List<AssetGenerator> generators = List.of(textureGenerator, spriteCompositor);
        return new GeneratorRegistry(generators);
    }
}
