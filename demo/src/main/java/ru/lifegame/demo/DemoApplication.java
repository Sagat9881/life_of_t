package ru.lifegame.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.awt.Desktop;
import java.net.URI;

/**
 * Демо-приложение для презентации UI компонентов.
 * Запускает веб-сервер и автоматически открывает браузер.
 */
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        // Запуск Spring Boot
        ConfigurableApplicationContext context = SpringApplication.run(DemoApplication.class, args);
        
        // Получаем порт
        String port = context.getEnvironment().getProperty("server.port", "3000");
        String url = "http://localhost:" + port;
        
        System.out.println("\n" +
            "\u250c\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2510\n" +
            "\u2502                                                  \u2502\n" +
            "\u2502       🎮 Life of T - Component Demo 🎮        \u2502\n" +
            "\u2502                                                  \u2502\n" +
            "\u2502  Демо-приложение для разработчиков и дизайнеров  \u2502\n" +
            "\u2502                                                  \u2502\n" +
            "\u2514\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2518\n" +
            "\n" +
            "\ud83c\udf10 Демо доступно: " + url + "\n" +
            "\n" +
            "\ud83d\udcdd Что внутри:\n" +
            "   • Все UI компоненты с интерактивными примерами\n" +
            "   • Цветовая палитра и типографика\n" +
            "   • Анимации и переходы\n" +
            "   • Haptic feedback демо\n" +
            "\n" +
            "⏸️  Для остановки: Ctrl+C\n" +
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
