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
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }

            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                // Serve generated pixel-art assets from asset-generator output
                // In dev: file:../asset-generator/target/generated-assets/
                // In prod: classpath:/generated-assets/ (copied during build)
                registry.addResourceHandler("/assets/**")
                        .addResourceLocations(
                                "file:../asset-generator/target/generated-assets/",
                                "classpath:/generated-assets/"
                        )
                        .setCacheControl(CacheControl.maxAge(Duration.ofDays(7)));
            }
        };
    }
}
