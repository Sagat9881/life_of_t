package ru.lifegame.backend.infrastructure.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * SPA Controller для обработки фронтенд роутинга.
 * Перенаправляет все не-API запросы на index.html для React Router.
 */
@Controller
public class SpaController {

    @RequestMapping(value = {
        "/{path:[^\\.]*}",
        "/**/{path:[^\\.]*}"
    })
    public String forward(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // НЕ перенаправлять технические пути
        if (path.startsWith("/api/") || 
            path.startsWith("/error") ||
            path.startsWith("/actuator/") ||
            path.startsWith("/swagger-ui") ||
            path.startsWith("/v3/api-docs")) {
            // Пропускаем дальше по цепочке фильтров
            return null;
        }
        
        // Все остальное → index.html (React Router)
        return "forward:/index.html";
    }
}
