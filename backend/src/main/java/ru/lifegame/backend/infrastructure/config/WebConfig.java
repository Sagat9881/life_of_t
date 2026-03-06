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

    @Bean
    public WebMvcConfigurer corsConfigurer() {
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
                // Frontend requests assets as /assets/{type}/{name}/...
                // Asset generator outputs to target/generated-assets/{type}/{name}/...
                // Map /assets/** -> generated-assets directories
                registry.addResourceHandler("/assets/**")
                        .addResourceLocations(
                                "file:./asset-generator/target/generated-assets/",
                                "file:../asset-generator/target/generated-assets/",
                                "classpath:/generated-assets/"
                        )
                        .setCacheControl(CacheControl.maxAge(Duration.ofDays(7)));

                // Also support legacy /generated-assets/** path (used by demo)
                registry.addResourceHandler("/generated-assets/**")
                        .addResourceLocations(
                                "file:./asset-generator/target/generated-assets/",
                                "file:../asset-generator/target/generated-assets/",
                                "classpath:/generated-assets/"
                        )
                        .setCacheControl(CacheControl.maxAge(Duration.ofDays(7)));

                // Vite-built frontend assets (JS/CSS with content hashes) — long cache
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
