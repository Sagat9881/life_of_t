package ru.lifegame.backend.infrastructure.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for Single Page Application routing
 * Forwards all non-API routes to index.html for React Router
 */
@Controller
public class SpaController {

    /**
     * Forward all routes (except API and static resources) to index.html
     * This allows React Router to handle client-side routing
     */
    @GetMapping(value = {
        "/",
        "/room",
        "/test/**",
        "/{path:^(?!api|assets|static).*}/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
