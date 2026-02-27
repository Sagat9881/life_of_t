package ru.lifegame.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для управления жизненным циклом приложения.
 * Позволяет выключить demo приложение через HTTP запрос.
 */
@RestController
public class ShutdownController {

    @Autowired
    private ConfigurableApplicationContext context;

    /**
 * Выключает приложение.
     * Вызывается из фронтенда по нажатию кнопки "Выключить".
     */
    @PostMapping("/api/shutdown")
    public void shutdown() {
        System.out.println("\n🛑 Получен запрос на выключение приложения...");
        System.out.println("👋 Спасибо за использование Life of T Demo!\n");
        
        // Запускаем выключение в отдельном потоке, чтобы успеть отправить ответ
        new Thread(() -> {
            try {
                Thread.sleep(500); // Даём время на отправку ответа
                context.close();
                System.exit(0);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
