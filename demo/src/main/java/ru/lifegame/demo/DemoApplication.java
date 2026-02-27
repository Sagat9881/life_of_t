package ru.lifegame.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.awt.Desktop;
import java.net.URI;

/**
 * Демо-приложение для презентации UI компонентов и тестирования backend API.
 * Запускает веб-сервер и автоматически открывает браузер.
 */
@SpringBootApplication(scanBasePackages = {"ru.lifegame.demo", "ru.lifegame.backend"})
public class DemoApplication {

    public static void main(String[] args) {
        // Запуск Spring Boot
        ConfigurableApplicationContext context = SpringApplication.run(DemoApplication.class, args);
        
        // Получаем порт
        String port = context.getEnvironment().getProperty("server.port", "3000");
        String url = "http://localhost:" + port;
        
        System.out.println("\n" +
            "┌──────────────────────────────────────────────────┐\n" +
            "│                                                  │\n" +
            "│       🎮 Life of T - Component Demo 🎮        │\n" +
            "│                                                  │\n" +
            "│  Демо-приложение для разработчиков и дизайнеров  │\n" +
            "│                                                  │\n" +
            "└──────────────────────────────────────────────────┘\n" +
            "\n" +
            "🌐 Демо доступно: " + url + "\n" +
            "🚀 Backend API: " + url + "/api/v1/game\n" +
            "📝 Swagger UI: " + url + "/swagger-ui.html\n" +
            "\n" +
            "📋 Что внутри:\n" +
            "   • Все UI компоненты с интерактивными примерами\n" +
            "   • Цветовая палитра и типографика\n" +
            "   • Анимации и переходы\n" +
            "   • Haptic feedback демо\n" +
            "   • Backend REST API с игровой логикой\n" +
            "\n" +
            "⏸️  Для остановки: Ctrl+C или кнопка 'Выключить' в браузере\n" +
            "\n"
        );
        
        // Автоматически открываем браузер
        openBrowser(url);
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
