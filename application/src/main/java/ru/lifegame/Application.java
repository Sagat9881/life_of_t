package ru.lifegame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.awt.Desktop;
import java.net.URI;

/**
 * Main application entry point.
 * Combines backend API with asset generation.
 *
 * Scans:
 * - ru.lifegame.backend — full game backend API (/api/v1/game)
 * - ru.lifegame.assets  — asset generator classes
 */
@SpringBootApplication(scanBasePackages = {
        "ru.lifegame.backend",
        "ru.lifegame.assets"
})
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        
        String port = context.getEnvironment().getProperty("server.port", "8080");
        String url = "http://localhost:" + port;
        
        System.out.println("\n" +
            "======================================================\n" +
            "  \uD83C\uDFAE Life of T — \u0416\u0438\u0437\u043D\u044C \u0422\u0430\u0442\u044C\u044F\u043D\u044B \uD83C\uDFAE\n" +
            "======================================================\n" +
            "\n" +
            "\uD83D\uDE80 Backend API: " + url + "/api/v1/game\n" +
            "\uD83D\uDC9A Health: " + url + "/actuator/health\n" +
            "\n" +
            "\u23F8\uFE0F  Ctrl+C \u0434\u043B\u044F \u043E\u0441\u0442\u0430\u043D\u043E\u0432\u043A\u0438\n" +
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
            System.out.println("\u26A0\uFE0F \u041E\u0442\u043A\u0440\u043E\u0439\u0442\u0435 \u0432\u0440\u0443\u0447\u043D\u0443\u044E: " + url + "\n");
        }
    }
}
