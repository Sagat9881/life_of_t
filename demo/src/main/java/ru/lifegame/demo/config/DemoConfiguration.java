package ru.lifegame.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.lifegame.assets.domain.service.PixelArtRenderer;
import ru.lifegame.assets.domain.service.PixelArtRendererRegistry;
import ru.lifegame.assets.infrastructure.generator.renderers.BedStaticRenderer;
import ru.lifegame.assets.infrastructure.generator.renderers.HomeRoomBgRenderer;
import ru.lifegame.assets.infrastructure.generator.renderers.SamIdleRenderer;
import ru.lifegame.assets.infrastructure.generator.renderers.SamWalkRenderer;
import ru.lifegame.assets.infrastructure.generator.renderers.TanyaIdleRenderer;
import ru.lifegame.assets.infrastructure.generator.renderers.TanyaWalkRenderer;

import java.util.List;

/**
 * Spring MVC configuration for the demo module.
 *
 * <p>Registers the dynamic asset output directory as an additional static-resource
 * location so that generated PNGs are accessible via the normal resource pipeline
 * (e.g. {@code /generated-assets/tanya_idle.png}) in addition to the explicit
 * {@code /api/assets/{id}.png} endpoint in {@code AssetController}.</p>
 *
 * <p>Also wires up the {@link PixelArtRendererRegistry} with all known renderers.</p>
 */
@Configuration
public class DemoConfiguration implements WebMvcConfigurer {

    private final String assetOutputDir;

    public DemoConfiguration(
            @Value("${demo.assets.output-dir:${java.io.tmpdir}/life-of-t-assets}") String assetOutputDir) {
        this.assetOutputDir = assetOutputDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + assetOutputDir + "/";
        registry.addResourceHandler("/generated-assets/**")
                .addResourceLocations(location)
                .setCachePeriod(0);
    }

    @Bean
    public PixelArtRendererRegistry pixelArtRendererRegistry() {
        List<PixelArtRenderer> renderers = List.of(
                new TanyaIdleRenderer(),
                new TanyaWalkRenderer(),
                new SamIdleRenderer(),
                new SamWalkRenderer(),
                new BedStaticRenderer(),
                new HomeRoomBgRenderer()
        );
        return new PixelArtRendererRegistry(renderers);
    }
}
