package ru.lifegame.sprite.scanner.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Base64;

/**
 * Downloads LPC sprites using Selenium WebDriver.
 * <p>
 * Workflow:
 * 1. Open LPC Generator URL with parameters
 * 2. Wait for page to render canvas
 * 3. Extract canvas as PNG via JavaScript
 * 4. Decode base64 to bytes
 * <p>
 * Uses headless Chrome for server-side generation.
 */
@Slf4j
@Service
public class SeleniumSpriteDownloader {

    private WebDriver driver;
    private static final int PAGE_LOAD_TIMEOUT_SECONDS = 30;
    private static final int CANVAS_RENDER_TIMEOUT_SECONDS = 15;

    /**
     * Initialize WebDriver on startup.
     */
    @PostConstruct
    public void init() {
        log.info("Initializing Selenium WebDriver...");

        try {
            // Setup ChromeDriver automatically
            WebDriverManager.chromedriver().setup();

            // Configure Chrome options
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new"); // New headless mode
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            // Create driver
            this.driver = new ChromeDriver(options);
            this.driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(PAGE_LOAD_TIMEOUT_SECONDS));

            log.info("✅ Selenium WebDriver initialized successfully");
        } catch (Exception e) {
            log.error("❌ Failed to initialize Selenium WebDriver", e);
            throw new RuntimeException("Selenium initialization failed", e);
        }
    }

    /**
     * Download sprite from LPC Generator URL.
     *
     * @param lpcUrl Full LPC Generator URL with hash parameters
     * @return PNG sprite as bytes
     * @throws Exception if download fails
     */
    public byte[] downloadSprite(String lpcUrl) throws Exception {
        log.info("Downloading sprite from: {}", lpcUrl);

        try {
            // 1. Navigate to URL
            driver.get(lpcUrl);
            log.debug("Page loaded: {}", driver.getTitle());

            // 2. Wait for canvas to render
            waitForCanvasRender();

            // 3. Extract canvas as base64 PNG
            String base64Image = extractCanvasAsPng();

            // 4. Decode to bytes
            byte[] pngBytes = decodeBase64Image(base64Image);

            log.info("✅ Sprite downloaded: {} bytes", pngBytes.length);
            return pngBytes;

        } catch (Exception e) {
            log.error("❌ Failed to download sprite from: {}", lpcUrl, e);
            throw e;
        }
    }

    /**
     * Wait for LPC Generator canvas to be rendered.
     */
    private void waitForCanvasRender() {
        log.debug("Waiting for canvas render...");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(CANVAS_RENDER_TIMEOUT_SECONDS));

        // Wait for canvas element to exist
        wait.until(d -> {
            Boolean canvasExists = (Boolean) ((JavascriptExecutor) d)
                .executeScript("return document.querySelector('canvas') !== null");
            return canvasExists != null && canvasExists;
        });

        // Additional wait for rendering to complete
        try {
            Thread.sleep(2000); // 2 seconds for sprite composition
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.debug("✅ Canvas rendered");
    }

    /**
     * Extract canvas content as base64-encoded PNG.
     */
    private String extractCanvasAsPng() {
        log.debug("Extracting canvas as PNG...");

        String script = """
            var canvas = document.querySelector('canvas');
            if (!canvas) {
                throw new Error('Canvas not found');
            }
            return canvas.toDataURL('image/png');
        """;

        Object result = ((JavascriptExecutor) driver).executeScript(script);

        if (result == null) {
            throw new RuntimeException("Failed to extract canvas: result is null");
        }

        String base64Image = result.toString();
        log.debug("Canvas extracted: {} chars", base64Image.length());

        return base64Image;
    }

    /**
     * Decode base64 data URL to PNG bytes.
     *
     * @param base64DataUrl Data URL in format: data:image/png;base64,iVBORw0KG...
     * @return Decoded PNG bytes
     */
    private byte[] decodeBase64Image(String base64DataUrl) {
        // Remove data URL prefix
        if (!base64DataUrl.startsWith("data:")) {
            throw new IllegalArgumentException("Invalid data URL: " + base64DataUrl.substring(0, 50));
        }

        // Extract base64 part after comma
        int commaIndex = base64DataUrl.indexOf(',');
        if (commaIndex == -1) {
            throw new IllegalArgumentException("Invalid data URL format: no comma found");
        }

        String base64Data = base64DataUrl.substring(commaIndex + 1);

        // Decode
        byte[] decoded = Base64.getDecoder().decode(base64Data);
        log.debug("Decoded {} base64 chars to {} bytes", base64Data.length(), decoded.length);

        return decoded;
    }

    /**
     * Check if WebDriver is initialized and ready.
     */
    public boolean isReady() {
        return driver != null;
    }

    /**
     * Get current driver (for testing/debugging).
     */
    public WebDriver getDriver() {
        return driver;
    }

    /**
     * Cleanup WebDriver on shutdown.
     */
    @PreDestroy
    public void cleanup() {
        log.info("Cleaning up Selenium WebDriver...");

        if (driver != null) {
            try {
                driver.quit();
                log.info("✅ WebDriver closed");
            } catch (Exception e) {
                log.error("Failed to close WebDriver", e);
            }
        }
    }
}
