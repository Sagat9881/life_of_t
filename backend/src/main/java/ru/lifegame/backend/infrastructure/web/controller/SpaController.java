package ru.lifegame.backend.infrastructure.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * SPA Controller для обработки фронтенд роутинга.
 * Перенаправляет все не-API запросы на index.html для React Router.
 * 
 * Роуты основаны на docs/prompts/screens/SCREENS_SPECIFICATION.xml:
 * - Primary Flow: room, actions, relationships, stats
 * - Locations: room, office, park
 * - Test pages: test/
 */
@Controller
public class SpaController {

    /**
     * Forward все маршруты (кроме API и статических ресурсов) на index.html.
     * Это позволяет React Router обрабатывать клиентский роутинг.
     * 
     * Используем {path:[^\\.]*} чтобы исключить файлы с расширениями (.js, .css, .png).
     */
    @GetMapping(value = {
        "/",
        "/{path:^(?!api|error|actuator|swagger-ui|v3)[^\\.]*$}",
        "/{path:^(?!api|error|actuator|swagger-ui|v3)[^\\.]*$}/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
