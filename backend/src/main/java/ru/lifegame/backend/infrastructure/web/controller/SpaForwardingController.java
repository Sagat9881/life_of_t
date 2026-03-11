package ru.lifegame.backend.infrastructure.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * SPA forwarding controller.
 * Forwards all non-API, non-asset, non-static-file requests to index.html
 * so that React Router can handle client-side routing.
 *
 * IMPORTANT: The path regex excludes paths containing a dot (.) to prevent
 * intercepting static files like index.html, *.js, *.css, *.png etc.
 * This avoids an infinite forward loop (forward:/index.html → caught again
 * by this controller → forward:/index.html → StackOverflowError).
 *
 * Also excludes /furniture/ and /characters/ prefixes so that missing
 * sprite-atlas assets return 404 instead of being forwarded to index.html.
 */
@Controller
public class SpaForwardingController {

    /**
     * Forward to index.html for all paths that are:
     * - NOT starting with /api/
     * - NOT starting with /assets/
     * - NOT starting with /actuator/
     * - NOT starting with /error
     * - NOT starting with /furniture/ or /characters/ (static asset roots)
     * - NOT containing a dot (i.e. not a static file like .js, .css, .png, .html)
     */
    @RequestMapping(value = "/{path:^(?!api|assets|actuator|error|_app|furniture|characters|favicon\\.ico)[^\\.]*$}")
    public String forwardSingle() {
        return "forward:/index.html";
    }

    @RequestMapping(value = "/{path:^(?!api|assets|actuator|error|_app|furniture|characters|favicon\\.ico)[^\\.]*$}/**")
    public String forwardNested() {
        return "forward:/index.html";
    }
}
