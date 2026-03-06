package ru.lifegame;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

/**
 * Web MVC configuration for serving frontend static files.
 * 
 * Статика из classpath:/static/ (куда frontend-maven-plugin
 * копирует dist/ при сборке) раздаётся напрямую.
 * JS/CSS кешируются на 1 год (Vite добавляет hash в имена),
 * index.html — no-cache (чтобы обновления подхватывались).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // JS, CSS, images — long cache (Vite content-hashes them)
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/")
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)));

        // All other static files (index.html, favicon, etc.)
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.noCache());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Allow frontend dev server (localhost:3000) to call backend API
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:8080")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true);
    }
}
