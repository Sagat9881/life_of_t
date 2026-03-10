package ru.lifegame.backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // API CORS
                registry.addMapping("/api/**")
                        .allowedOrigins(
                            "http://localhost:5173",  // Vite dev server
                            "http://localhost:3000",  // Legacy port
                            "http://localhost:8080"   // Backend itself
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);

                // Assets CORS (for Vite proxy)
                registry.addMapping("/assets/**")
                        .allowedOrigins(
                            "http://localhost:5173",
                            "http://localhost:3000",
                            "http://localhost:8080"
                        )
                        .allowedMethods("GET", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
            // NOTE: /assets/** ResourceHandler is defined in StaticResourceConfig.
            // Do NOT add a second handler here — Spring MVC applies only the first
            // match, so having two registrations causes undefined behavior.
        };
    }
}
