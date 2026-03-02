package ru.lifegame.sprite.scanner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.lifegame.sprite.scanner.model.LpcCharacterConfig;
import ru.lifegame.sprite.scanner.model.LpcSpriteRequest;
import ru.lifegame.sprite.scanner.selenium.SeleniumSpriteDownloader;
import ru.lifegame.sprite.scanner.url.LpcUrlBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main service for generating LPC character sprites.
 * <p>
 * This service provides:
 * - URL generation from character configs
 * - Sprite downloading via Selenium WebDriver
 * - File storage management
 * - Integration with prompt system
 */
@Slf4j
@Service
public class LpcGeneratorService {

    @Autowired
    private SeleniumSpriteDownloader seleniumDownloader;

    private final LpcUrlBuilder urlBuilder;

    // Base URL for LPC Generator
    private static final String LPC_GENERATOR_BASE_URL = 
        "https://liberatedpixelcup.github.io/Universal-LPC-Spritesheet-Character-Generator/";

    public LpcGeneratorService() {
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
     * @throws Exception if generation or file operations fail
     */
    public Path generateSprite(LpcSpriteRequest request) throws Exception {
        log.info("Generating sprite for: {}/{}", 
                 request.getCharacterId(), 
                 request.getAnimationName());

        // Check if already exists and not overwrite mode
        Path outputPath = buildOutputPath(request);
        if (Files.exists(outputPath) && !request.isOverwrite()) {
            log.info("Sprite already exists (skip): {}", outputPath);
            return outputPath;
        }

        // Generate URL
        String url = generateUrl(request.getConfig());
        log.info("LPC URL: {}", url);

        // Download sprite via Selenium
        byte[] spriteData = downloadSprite(url);

        if (spriteData == null || spriteData.length == 0) {
            throw new IOException("Downloaded sprite is empty");
        }

        // Save to file
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, spriteData);

        log.info("✅ Sprite saved to: {} ({} bytes)", outputPath, spriteData.length);
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
     * Download sprite from LPC Generator using Selenium.
     *
     * @param url LPC Generator URL
     * @return Sprite image bytes
     * @throws Exception if download fails
     */
    private byte[] downloadSprite(String url) throws Exception {
        log.info("Downloading sprite via Selenium...");

        if (!seleniumDownloader.isReady()) {
            throw new IllegalStateException("Selenium WebDriver is not initialized");
        }

        try {
            byte[] spriteData = seleniumDownloader.downloadSprite(url);
            log.info("✅ Sprite downloaded: {} bytes", spriteData.length);
            return spriteData;
        } catch (Exception e) {
            log.error("❌ Sprite download failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Build output file path based on request
     *
     * @param request Sprite request
     * @return Path to output file
     */
    private Path buildOutputPath(LpcSpriteRequest request) {
        // Override output directory if specified
        if (request.getOutputDirectory() != null && !request.getOutputDirectory().isEmpty()) {
            return Paths.get(
                request.getOutputDirectory(),
                request.getAnimationName() + "." + request.getFormat()
            );
        }

        // Default pattern: assets/characters/{characterId}/animations/{animationName}.png
        String filename = String.format(
            "assets/characters/%s/animations/%s.%s",
            request.getCharacterId(),
            request.getAnimationName(),
            request.getFormat()
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

    /**
     * Check if Selenium is ready for sprite generation
     */
    public boolean isReady() {
        return seleniumDownloader != null && seleniumDownloader.isReady();
    }
}
