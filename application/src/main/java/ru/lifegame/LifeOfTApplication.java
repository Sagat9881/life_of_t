package ru.lifegame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import ru.lifegame.assets.config.AssetGeneratorConfig;

@SpringBootApplication
@ComponentScan(basePackages = {"ru.lifegame.backend", "ru.lifegame.assets"})
public class LifeOfTApplication {
    public static void main(String[] args) {
        // Check if asset generation is requested
        boolean generateAssets = false;
        for (String arg : args) {
            if ("--generate-assets".equals(arg)) {
                generateAssets = true;
                break;
            }
        }

        SpringApplication app = new SpringApplication(LifeOfTApplication.class);
        if (generateAssets) {
            app.setAdditionalProfiles("generate-assets");
        }
        app.run(args);
    }
}
