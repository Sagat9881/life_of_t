package ru.lifegame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.awt.Desktop;
import java.net.URI;

/**
 * Main application entry point.
 * Combines demo frontend (pixel-art scene) with full backend API.
 *
 * Scans:
 * - ru.lifegame.demo    — demo frontend controllers, asset service, config
 * - ru.lifegame.backend — full game backend API (/api/v1/game)
 * - ru.lifegame.assets  — asset generator classes
 */
@SpringBootApplication(scanBasePackages = {
        "ru.lifegame.demo",
        "ru.lifegame.backend",
        "ru.lifegame.assets"
})
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        
        String port = context.getEnvironment().getProperty("server.port", "8080");
        String url = "http://localhost:" + port;
        
        System.out.println("\n" +
            "┌──────────────────────────────────────────────────┐\n" +
            "│                                                  │\n" +
            "│       🎮 Life of T — Жизнь Татьяны 🎮        │\n" +
            "│                                                  │\n" +
            "└──────────────────────────────────────────────────┘\n" +
            "\n" +
            "🌐 Игра: " + url + "\n" +
            "🚀 Backend API: " + url + "/api/v1/game\n" +
            "🎮 Demo API: " + url + "/api/demo\n" +
            "💚 Health: " + url + "/actuator/health\n" +
            "\n" +
            "⏸️  Ctrl+C для остановки\n" +
            "\n"
        );
        
        String profile = context.getEnvironment().getProperty("spring.profiles.active", "default");
        if ("dev".equals(profile) || "default".equals(profile)) {
            openBrowser(url);
        }
    }
    
    private static void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception e) {
            System.out.println("⚠️ Откройте вручную: " + url + "\n");
        }
    }
}
