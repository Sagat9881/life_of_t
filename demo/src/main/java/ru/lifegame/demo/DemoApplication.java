package ru.lifegame.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import ru.lifegame.demo.service.DemoAssetService;

/**
 * Entry point for the Life of T demo Spring Boot application.
 *
 * <p>On startup:
 * <ol>
 *   <li>Generates pixel-art sprite atlas PNGs via {@link DemoAssetService}</li>
 *   <li>Serves the animated home-room page at {@code /}</li>
 *   <li>Exposes REST endpoints at {@code /api/demo/**}</li>
 * </ol>
 */
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    /**
     * Generates assets immediately after the application context is fully started,
     * so that the static-resource handler already has the output path registered.
     */
    @Component
    static class AssetGenerationStartupListener
            implements ApplicationListener<ContextRefreshedEvent> {

        private final DemoAssetService assetService;
        private boolean ran = false;

        AssetGenerationStartupListener(DemoAssetService assetService) {
            this.assetService = assetService;
        }

        @Override
        public void onApplicationEvent(ContextRefreshedEvent event) {
            // Guard against duplicate fires (parent + child contexts in tests).
            if (!ran) {
                ran = true;
                assetService.generateAll();
            }
        }
    }
}
