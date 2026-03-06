package ru.lifegame.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Global handler that converts NoResourceFoundException from Spring's static
 * resource serving into a clean 404 JSON response instead of a 500 stacktrace.
 *
 * This is critical for the demo: the asset-generator may not have produced all
 * animation atlases yet (e.g. idle_atlas.png), and the frontend handles missing
 * sprites gracefully with placeholder colors.
 */
@ControllerAdvice
public class AssetErrorConfig {

    private static final Logger log = LoggerFactory.getLogger(AssetErrorConfig.class);

    @ExceptionHandler(NoResourceFoundException.class)
    public void handleNoResource(NoResourceFoundException ex,
                                  HttpServletRequest request,
                                  HttpServletResponse response) throws IOException {
        String uri = request.getRequestURI();

        // Only log at debug for expected missing assets
        if (uri.contains("/assets/") || uri.contains("favicon") || uri.contains(".well-known")) {
            log.debug("Asset not found: {}", uri);
        } else {
            log.warn("Resource not found: {}", uri);
        }

        response.setStatus(HttpStatus.NOT_FOUND.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"not_found\",\"path\":\"" + uri + "\"}");
    }
}
