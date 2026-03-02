package ru.lifegame.lpc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.lifegame.lpc.model.LpcCharacterConfig;
import ru.lifegame.lpc.model.LpcSpriteRequest;
import ru.lifegame.lpc.url.LpcUrlBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Main service for generating LPC character sprites.
 * <p>
 * This service provides:
 * - URL generation from character configs
 * - Sprite downloading via LPC Generator API
 * - File storage management
 * - Integration with prompt system
 */
@Slf4j
@Service
public class LpcGeneratorService {

    private final RestTemplate restTemplate;
    private final LpcUrlBuilder urlBuilder;

    // Base URL for LPC Generator (can be configured)
    private static final String LPC_GENERATOR_BASE_URL = 
        "https://liberatedpixelcup.github.io/Universal-LPC-Spritesheet-Character-Generator/";

    // API endpoint for sprite generation (if available)
    // Note: LPC Generator is client-side, so we'll need to simulate API or use headless browser
    private static final String LPC_API_ENDPOINT = 
        "https://liberatedpixelcup.github.io/Universal-LPC-Spritesheet-Character-Generator/api/generate";

    public LpcGeneratorService() {
        this.restTemplate = new RestTemplate();
        this.urlBuilder = new LpcUrlBuilder();
    }

    /**
     * Generate LPC sprite URL from character configuration
     *
     * @param config Character configuration (body, hair, clothes, etc.)
     * @return Full LPC Generator URL with parameters
     */
    public String generateUrl(LpcCharacterConfig config) {
        log.info("Generating LPC URL for character: {}", config.getCharacterId());
        String url = urlBuilder.build(config);
        log.debug("Generated URL: {}", url);
        return url;
    }

    /**
     * Generate sprite from request and save to file
     *
     * @param request Sprite generation request
     * @return Path to generated sprite file
     * @throws IOException if file operations fail
     */
    public Path generateSprite(LpcSpriteRequest request) throws IOException {
        log.info("Generating sprite for: {}/{}", 
                 request.getCharacterId(), 
                 request.getAnimationName());

        // Generate URL
        String url = generateUrl(request.getConfig());

        // Download sprite
        byte[] spriteData = downloadSprite(url);

        // Save to file
        Path outputPath = buildOutputPath(request);
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, spriteData);

        log.info("Sprite saved to: {}", outputPath);
        return outputPath;
    }

    /**
     * Check if sprite already exists
     *
     * @param request Sprite request
     * @return true if sprite file exists
     */
    public boolean spriteExists(LpcSpriteRequest request) {
        Path path = buildOutputPath(request);
        boolean exists = Files.exists(path);
        log.debug("Sprite exists check for {}: {}", path, exists);
        return exists;
    }

    /**
     * Download sprite from LPC Generator
     * <p>
     * Note: Since LPC Generator is client-side JavaScript, we have two options:
     * 1. Use Selenium/Playwright to render page and extract canvas
     * 2. Implement server-side sprite composition using LPC assets
     * <p>
     * For now, this is a placeholder that will be implemented based on chosen approach.
     *
     * @param url LPC Generator URL
     * @return Sprite image bytes
     */
    private byte[] downloadSprite(String url) {
        log.warn("downloadSprite is not fully implemented yet. URL: {}", url);
        
        // TODO: Implement actual sprite download
        // Option 1: Use Selenium WebDriver
        // Option 2: Call external API service
        // Option 3: Implement server-side LPC sprite composer
        
        // Placeholder: return empty PNG
        return new byte[0];
    }

    /**
     * Build output file path based on request
     *
     * @param request Sprite request
     * @return Path to output file
     */
    private Path buildOutputPath(LpcSpriteRequest request) {
        // Pattern: assets/characters/{characterId}/animations/{animationName}.png
        String filename = String.format(
            "assets/characters/%s/animations/%s.png",
            request.getCharacterId(),
            request.getAnimationName()
        );
        return Paths.get(filename);
    }

    /**
     * Get LPC Generator URL for viewing in browser
     *
     * @param config Character configuration
     * @return Full URL that can be opened in browser
     */
    public String getViewUrl(LpcCharacterConfig config) {
        return generateUrl(config);
    }
}
