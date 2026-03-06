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
     * Common resource locations for generated pixel-art assets.
     * These are produced by asset-generator from asset-specs/ XMLs.
     *
     * - classpath:/generated-assets/ — inside the JAR (copied during build)
     * - file paths — for dev mode when running from project root
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
                // /assets/** — used by React frontend (assetService.ts)
                registry.addResourceHandler("/assets/**")
                        .addResourceLocations(GENERATED_ASSET_LOCATIONS)
                        .setCacheControl(CacheControl.maxAge(Duration.ofDays(7)));

                // /generated-assets/** — used by demo module (index.html)
                registry.addResourceHandler("/generated-assets/**")
                        .addResourceLocations(GENERATED_ASSET_LOCATIONS)
                        .setCacheControl(CacheControl.maxAge(Duration.ofDays(7)));

                // Vite-built frontend JS/CSS (content-hashed filenames)
                registry.addResourceHandler("/static-assets/**")
                        .addResourceLocations("classpath:/static/assets/")
                        .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)));

                // All other static files (index.html, favicon) — no cache
                registry.addResourceHandler("/**")
                        .addResourceLocations("classpath:/static/")
                        .setCacheControl(CacheControl.noCache());
            }
        };
    }
}
