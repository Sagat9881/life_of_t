package ru.lifegame.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Контроллер для демо-функций
 */
@RestController
@RequestMapping("/api/demo")
public class DemoController {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Выключение демо-приложения
     */
    @PostMapping("/shutdown")
    public ResponseEntity<Map<String, String>> shutdown() {
        // Запускаем выключение в отдельном потоке чтобы успеть вернуть ответ
        new Thread(() -> {
            try {
                Thread.sleep(500); // Даем время отправить ответ
                System.out.println("\n🔴 Остановка демо-приложения...");
                System.out.println("✅ Спасибо за использование Life of T Demo!\n");
                SpringApplication.exit(applicationContext, () -> 0);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        
        return ResponseEntity.ok(Map.of(
            "status", "shutting_down",
            "message", "Демо-приложение выключается..."
        ));
    }
}
