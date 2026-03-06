package ru.lifegame.backend.infrastructure.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * SPA forwarding controller.
 * Forwards all non-API, non-asset, non-actuator requests to index.html
 * so that React Router can handle client-side routing.
 */
@Controller
public class SpaForwardingController {

    /**
     * Forward to index.html for all paths that are:
     * - NOT starting with /api/
     * - NOT starting with /assets/
     * - NOT starting with /actuator/
     * - NOT containing a dot (i.e. not a static file like .js, .css, .png)
     */
    @RequestMapping(value = {
            "/",
            "/{path:^(?!api|assets|actuator|_app|favicon\\.ico).*$}/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
