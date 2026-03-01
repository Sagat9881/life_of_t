package ru.lifegame.backend.infrastructure.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * SPA Controller для обработки фронтенд роутинга.
 * Перенаправляет все не-API запросы на index.html для React Router.
 */
@Controller
public class SpaController {

    /**
     * Forward все маршруты (кроме API и статических ресурсов) на index.html
     * Это позволяет React Router обрабатывать клиентский роутинг
     */
    @GetMapping(value = {
        "/",
        "/room/**",
        "/test/**",
        "/park/**",
        "/game/**"
    })
    public String forward(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // НЕ перенаправлять технические пути
        if (path.startsWith("/api/") || 
            path.startsWith("/error") ||
            path.startsWith("/actuator/") ||
            path.startsWith("/swagger-ui") ||
            path.startsWith("/v3/api-docs") ||
            path.contains(".")) {  // файлы с расширениями (js, css, png и т.д.)
            return null;
        }
        
        // Все остальное → index.html (React Router)
        return "forward:/index.html";
    }
}
