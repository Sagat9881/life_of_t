package ru.lifegame;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Serves placeholder assets when real generated assets are not yet available.
 * This controller has lower priority than static resource handlers —
 * it only activates when the file is NOT found in generated-assets/.
 *
 * Returns:
 * - For atlas-config.json: a valid JSON array with a single placeholder animation
 * - For .png: a tiny 1x1 magenta pixel (clearly indicating "placeholder")
 * - For location images: a solid-color placeholder
 */
@Controller
public class PlaceholderAssetController {

    // 1x1 magenta PNG (89 50 4E 47 ... IEND) - 68 bytes
    private static final byte[] PLACEHOLDER_PNG = {
        (byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
        0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
        0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
        0x08, 0x02, 0x00, 0x00, 0x00, (byte)0x90, 0x77, 0x53,
        (byte)0xDE, 0x00, 0x00, 0x00, 0x0C, 0x49, 0x44, 0x41,
        0x54, 0x08, (byte)0xD7, 0x63, (byte)0xF8, (byte)0xCF, (byte)0xC0, 0x00,
        0x00, 0x00, 0x02, 0x00, 0x01, (byte)0xE2, 0x21, (byte)0xBC,
        0x33, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E,
        0x44, (byte)0xAE, 0x42, 0x60, (byte)0x82
    };

    private static final String PLACEHOLDER_ATLAS_CONFIG = """
        [
          {
            "name": "idle",
            "file": "idle_atlas.png",
            "frameWidth": 16,
            "frameHeight": 16,
            "frames": 1,
            "fps": 1,
            "loop": true
          }
        ]
        """;

    /**
     * Fallback for atlas-config.json requests.
     * Only reached if the real file doesn't exist in generated-assets.
     */
    @GetMapping("/assets/{type}/{name}/animations/atlas-config.json")
    @ResponseBody
    public ResponseEntity<String> placeholderAtlasConfig(
            @PathVariable String type,
            @PathVariable String name) {

        // Check if real asset exists
        if (realAssetExists(type, name, "animations/atlas-config.json")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .header("X-Placeholder", "true")
                .body(PLACEHOLDER_ATLAS_CONFIG);
    }

    /**
     * Fallback for PNG atlas requests.
     */
    @GetMapping("/assets/{type}/{name}/animations/{filename}.png")
    @ResponseBody
    public ResponseEntity<byte[]> placeholderAtlasPng(
            @PathVariable String type,
            @PathVariable String name,
            @PathVariable String filename) {

        if (realAssetExists(type, name, "animations/" + filename + ".png")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .header("X-Placeholder", "true")
                .body(PLACEHOLDER_PNG);
    }

    /**
     * Fallback for location composite images.
     */
    @GetMapping("/assets/{type}/{name}/{name2}.png")
    @ResponseBody
    public ResponseEntity<byte[]> placeholderCompositePng(
            @PathVariable String type,
            @PathVariable String name,
            @PathVariable String name2) {

        if (realAssetExists(type, name, name2 + ".png")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .header("X-Placeholder", "true")
                .body(PLACEHOLDER_PNG);
    }

    private boolean realAssetExists(String type, String name, String subPath) {
        // Check multiple possible locations
        Path[] candidates = {
            Path.of("asset-generator/target/generated-assets", type, name, subPath),
            Path.of("../asset-generator/target/generated-assets", type, name, subPath),
        };
        for (Path p : candidates) {
            if (Files.exists(p)) return true;
        }
        // Also check classpath (would need more complex logic, skip for now)
        return false;
    }
}
