package ru.lifegame.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Serves generated assets from the asset-generator output directory.
 *
 * Maps {@code /assets/generated/**} to the filesystem directory configured
 * by {@code demo.assets.output-dir}. Returns proper 404 for missing files
 * instead of Spring's default 500 NoResourceFoundException.
 *
 * This controller replaces the static resource handler approach from
 * DemoConfiguration, which could not gracefully handle missing files.
 */
@RestController
@RequestMapping("/assets/generated")
public class GeneratedAssetController {

    private static final Logger log = LoggerFactory.getLogger(GeneratedAssetController.class);

    private final Path outputDir;

    public GeneratedAssetController(
            @Value("${demo.assets.output-dir:${user.dir}/asset-generator/target/generated-assets}") String outputDirStr) {
        this.outputDir = Path.of(outputDirStr).toAbsolutePath().normalize();
        log.info("GeneratedAssetController serving from: {}", this.outputDir);
    }

    @GetMapping("/**")
    public ResponseEntity<Resource> serveAsset(HttpServletRequest request) {
        // Extract the path after /assets/generated/
        String fullPath = request.getRequestURI();
        String prefix = "/assets/generated/";
        int idx = fullPath.indexOf(prefix);
        if (idx < 0) {
            return ResponseEntity.notFound().build();
        }
        String relativePath = fullPath.substring(idx + prefix.length());

        // Security: prevent path traversal
        if (relativePath.contains("..") || relativePath.startsWith("/")) {
            return ResponseEntity.badRequest().build();
        }

        Path filePath = outputDir.resolve(relativePath).normalize();

        // Ensure resolved path is still within outputDir
        if (!filePath.startsWith(outputDir)) {
            return ResponseEntity.badRequest().build();
        }

        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            log.debug("Asset not found: {}", relativePath);
            return ResponseEntity.notFound().build();
        }

        // Determine content type
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (relativePath.endsWith(".png")) {
            mediaType = MediaType.IMAGE_PNG;
        } else if (relativePath.endsWith(".json")) {
            mediaType = MediaType.APPLICATION_JSON;
        } else if (relativePath.endsWith(".xml")) {
            mediaType = MediaType.APPLICATION_XML;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .cacheControl(CacheControl.noCache())
                .body(new FileSystemResource(filePath));
    }
}
