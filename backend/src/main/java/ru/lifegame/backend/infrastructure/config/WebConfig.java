package ru.lifegame.backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

@Configuration
public class WebConfig {

    /**
     * Resource locations for generated pixel-art assets.
     * Produced by asset-generator from asset-specs/ XMLs.
     */
    private static final String[] GENERATED_ASSET_LOCATIONS = {
            "classpath:/generated-assets/",
            "file:./asset-generator/target/generated-assets/",
            "file:../asset-generator/target/generated-assets/",
    };

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
                // /assets/** — game generated assets (characters, locations, furniture)
                // Produced by asset-generator module during build
                registry.addResourceHandler("/assets/**")
                        .addResourceLocations(GENERATED_ASSET_LOCATIONS)
                        .setCacheControl(CacheControl.maxAge(Duration.ofDays(7)));

                // /generated-assets/** — same assets, used by demo module
                registry.addResourceHandler("/generated-assets/**")
                        .addResourceLocations(GENERATED_ASSET_LOCATIONS)
                        .setCacheControl(CacheControl.maxAge(Duration.ofDays(7)));

                // /_app/** — Vite-built JS/CSS bundles (content-hashed filenames)
                // Vite assetsDir is set to '_app' to avoid conflict with /assets/
                registry.addResourceHandler("/_app/**")
                        .addResourceLocations("classpath:/static/_app/")
                        .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)));
            }
        };
    }
}
