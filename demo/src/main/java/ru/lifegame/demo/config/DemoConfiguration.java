package ru.lifegame.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

/**
 * Spring MVC configuration for the demo module.
 *
 * <p>Registers the generated asset output directory as an additional static-resource
 * location so that pre-built PNGs from asset-generator are accessible via
 * {@code /assets/generated/characters/tanya/tanya.png} etc.</p>
 *
 * <p>In standalone demo mode, assets are loaded from the filesystem.
 * In the combined application JAR, assets are already packaged inside
 * {@code classpath:/static/assets/generated/} by the frontend module.</p>
 */
@Configuration
public class DemoConfiguration implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(DemoConfiguration.class);

    private final String assetOutputDir;

    public DemoConfiguration(
            @Value("${demo.assets.output-dir:${user.dir}/asset-generator/target/generated-assets}") String assetOutputDir) {
        this.assetOutputDir = assetOutputDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path resolved = Path.of(assetOutputDir).toAbsolutePath().normalize();
        String location = "file:" + resolved + "/";
        log.info("Serving generated assets from: {}", location);
        registry.addResourceHandler("/assets/generated/**")
                .addResourceLocations(location)
                .setCachePeriod(0);
    }
}
