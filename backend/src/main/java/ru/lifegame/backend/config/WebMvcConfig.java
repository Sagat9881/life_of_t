package ru.lifegame.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for serving generated assets.
 * <p>
 * Asset generator creates PNG/JSON files in classpath root directories
 * (characters/, furniture/, locations/, etc.), but Spring Boot only serves
 * static resources from classpath:/static/, /public/, /resources/, /META-INF/resources/
 * by default.
 * <p>
 * This configuration allows serving any request that doesn't match a REST endpoint
 * as a static resource from classpath root.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve all non-API requests from classpath root
        // This includes generated assets: characters/, furniture/, locations/, etc.
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/")
                .setCachePeriod(3600) // 1 hour cache
                .resourceChain(true);
    }
}
