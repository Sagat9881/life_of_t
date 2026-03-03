package ru.lifegame.backend.infrastructure.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * SPA Controller for frontend routing support.
 * Forwards all non-API requests to index.html for React Router.
 */
@Controller
public class SpaController {

    @GetMapping(value = {
        "/",
        "/{path:^(?!api|error|actuator|swagger-ui|v3)[^\\.]*$}",
        "/{path:^(?!api|error|actuator|swagger-ui|v3)[^\\.]*$}/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
