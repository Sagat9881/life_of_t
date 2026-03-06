package ru.lifegame;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * SPA (Single Page Application) forwarding controller.
 * 
 * Все маршруты, которые не являются API-запросами, файлами статики
 * или actuator-эндпоинтами, перенаправляются на index.html.
 * Это необходимо для React Router (HTML5 History Mode).
 * 
 * Без этого контроллера при прямом открытии /game или /settings
 * Spring Boot вернёт 404, потому что не найдёт соответствующий
 * серверный маршрут.
 */
@Controller
public class SpaWebController {

    /**
     * Forward all non-API, non-static routes to index.html.
     * 
     * Regex breakdown:
     * - Не начинается с /api/ (бэкенд API)
     * - Не начинается с /actuator/ (Spring Actuator)
     * - Не содержит точку в последнем сегменте (значит это не файл: .js, .css, .png)
     */
    @GetMapping(value = {
        "/",
        "/{path:[^\\.]*}",
        "/{path1:[^\\.]*}/{path2:[^\\.]*}",
        "/{path1:[^\\.]*}/{path2:[^\\.]*}/{path3:[^\\.]*}"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
