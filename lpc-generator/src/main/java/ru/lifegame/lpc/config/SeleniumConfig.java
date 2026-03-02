package ru.lifegame.lpc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Selenium WebDriver.
 * <p>
 * Configure via application.properties:
 * <pre>
 * selenium.headless=true
 * selenium.page-load-timeout=30
 * selenium.canvas-render-timeout=15
 * </pre>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "selenium")
public class SeleniumConfig {

    /**
     * Run Chrome in headless mode (no GUI)
     * Default: true
     */
    private boolean headless = true;

    /**
     * Page load timeout in seconds
     * Default: 30
     */
    private int pageLoadTimeout = 30;

    /**
     * Canvas render wait timeout in seconds
     * Default: 15
     */
    private int canvasRenderTimeout = 15;

    /**
     * Window size for browser
     * Default: 1920x1080
     */
    private String windowSize = "1920,1080";

    /**
     * Additional Chrome arguments
     */
    private String[] additionalArgs = new String[]{
        "--disable-blink-features=AutomationControlled"
    };

    /**
     * Enable verbose logging
     * Default: false
     */
    private boolean verboseLogging = false;
}
