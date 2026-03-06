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
                // Vite-built assets (JS/CSS with content hashes) — long cache
                registry.addResourceHandler("/assets/**")
                        .addResourceLocations(
                                "classpath:/static/assets/",
                                "file:../asset-generator/target/generated-assets/",
                                "classpath:/generated-assets/"
                        )
                        .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)));

                // All other static files (index.html, favicon, etc.) — no cache
                registry.addResourceHandler("/**")
                        .addResourceLocations("classpath:/static/")
                        .setCacheControl(CacheControl.noCache());
            }
        };
    }
}
