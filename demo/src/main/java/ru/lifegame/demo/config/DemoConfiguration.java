package ru.lifegame.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC configuration for the demo module.
 *
 * <p>Registers the generated asset output directory as an additional static-resource
 * location so that pre-built PNGs are accessible via the normal resource pipeline
 * (e.g. {@code /generated-assets/characters/tanya/tanya.png}).</p>
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
}
