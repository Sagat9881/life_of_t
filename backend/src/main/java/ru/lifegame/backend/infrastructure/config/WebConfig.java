package ru.lifegame.backend.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Web MVC configuration for the backend module.
 * Handles CORS for frontend development and serves generated assets
 * from the filesystem at /assets/**.
 */
@Configuration
public class WebConfig {

    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

    @Value("${assets.output-dir:${user.dir}/asset-generator/target/generated-assets}")
    private String assetsOutputDir;

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:3000", "http://localhost:8080")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }

            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                // Serve generated assets from filesystem.
                // Generator writes to: {assetsOutputDir}/assets/{entityType}/{entityName}/...
                // Frontend requests:   /assets/{entityType}/{entityName}/...
                // So we map /assets/** → file:{assetsOutputDir}/assets/
                Path assetsPath = Path.of(assetsOutputDir, "assets").toAbsolutePath();
                String fileUrl = "file:" + assetsPath.toString().replace('\\', '/') + "/";

                log.info("Serving generated assets from: {}", fileUrl);

                registry.addResourceHandler("/assets/**")
                        .addResourceLocations(fileUrl)
                        .setCachePeriod(0); // No caching in dev
            }
        };
    }
}
