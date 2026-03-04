package ru.lifegame.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC configuration for the demo module.
 *
 * <p>Registers the dynamic asset output directory as an additional static-resource
 * location so that generated PNGs are accessible via the normal resource pipeline
 * (e.g. {@code /generated-assets/tanya_idle.png}) in addition to the explicit
 * {@code /api/assets/{id}.png} endpoint in {@code AssetController}.</p>
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
        // Serve generated PNGs from the dynamic output directory.
        // The trailing "/" is required by Spring's resource handler.
        String location = "file:" + assetOutputDir + "/";
        registry.addResourceHandler("/generated-assets/**")
                .addResourceLocations(location)
                .setCachePeriod(0); // No caching during development / demo
    }
}
