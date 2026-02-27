package ru.lifegame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.awt.Desktop;
import java.net.URI;

/**
 * Главный класс приложения Life of T.
 * Запускает Spring Boot приложение с backend API и frontend статикой.
 */
@SpringBootApplication(scanBasePackages = "ru.lifegame")
public class Application {

    public static void main(String[] args) {
        // Запуск Spring Boot
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        
        // Получаем порт
        String port = context.getEnvironment().getProperty("server.port", "8080");
        String url = "http://localhost:" + port;
        
        System.out.println("\n" +
            "┌──────────────────────────────────────────────────┐\n" +
            "│                                                  │\n" +
            "│       🎮 Life of T - Telegram Mini App 🎮        │\n" +
            "│                                                  │\n" +
            "│  Симулятор жизни для Telegram WebApp         │\n" +
            "│                                                  │\n" +
            "└──────────────────────────────────────────────────┘\n" +
            "\n" +
            "🌐 Приложение доступно: " + url + "\n" +
            "🚀 Backend API: " + url + "/api/v1/game\n" +
            "📝 Swagger UI: " + url + "/swagger-ui.html\n" +
            "💚 Health Check: " + url + "/actuator/health\n" +
            "\n" +
            "📦 Возможности:\n" +
            "   • Управление персонажем и его статами\n" +
            "   • Система отношений с NPC\n" +
            "   • Динамические события и конфликты\n" +
            "   • Уход за питомцами\n" +
            "   • Система времени и ресурсов\n" +
            "\n" +
            "⏸️  Для остановки: Ctrl+C\n" +
            "\n"
        );
        
        // Автоматически открываем браузер только в dev режиме
        String profile = context.getEnvironment().getProperty("spring.profiles.active", "default");
        if ("dev".equals(profile) || "default".equals(profile)) {
            openBrowser(url);
        }
    }
    
    private static void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                System.out.println("✅ Браузер открыт автоматически\n");
            } else {
                System.out.println("⚠️ Откройте вручную: " + url + "\n");
            }
        } catch (Exception e) {
            System.out.println("⚠️ Не удалось открыть браузер. Откройте вручную: " + url + "\n");
        }
    }
}
