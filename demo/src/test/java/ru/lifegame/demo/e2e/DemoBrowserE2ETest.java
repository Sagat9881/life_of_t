package ru.lifegame.demo.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Browser-based E2E test using Testcontainers' {@link BrowserWebDriverContainer}.
 *
 * <p>Starts a headless Chrome browser inside a Docker container and loads the
 * demo page, verifying that the DOM contains the expected sprite elements and
 * the REST API data is rendered.</p>
 *
 * <p><strong>Prerequisites:</strong> Docker must be available in the CI/test
 * environment. The test is skipped automatically by Testcontainers if Docker
 * is not present.</p>
 *
 * <p>The Spring Boot app runs on a random port; the Chrome container connects
 * to it via the host bridge network using the {@code host.testcontainers.internal}
 * hostname that Testcontainers maps to the host machine.</p>
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Demo Browser E2E Tests (Testcontainers + Selenium Chrome)")
class DemoBrowserE2ETest {

    @Container
    @SuppressWarnings("resource")
    static final BrowserWebDriverContainer<?> chrome =
            new BrowserWebDriverContainer<>()
                    .withCapabilities(new org.openqa.selenium.chrome.ChromeOptions());

    @LocalServerPort
    int port;

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private String baseUrl() {
        // Testcontainers maps host → host.testcontainers.internal inside containers
        return "http://host.testcontainers.internal:" + port;
    }

    private RemoteWebDriver driver() {
        return chrome.getWebDriver();
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Home page loads and has correct title")
    void homePageHasCorrectTitle() {
        RemoteWebDriver wd = driver();
        wd.get(baseUrl() + "/");
        String title = wd.getTitle();
        assertThat(title).containsIgnoringCase("Life of T");
    }

    @Test
    @DisplayName("Room container element is present in the DOM")
    void roomContainerIsPresentInDom() {
        RemoteWebDriver wd = driver();
        wd.get(baseUrl() + "/");
        WebElement roomContainer = wd.findElement(By.id("room-container"));
        assertThat(roomContainer).isNotNull();
    }

    @Test
    @DisplayName("Tanya sprite element is present with correct CSS class")
    void tanyaSpriteIsPresent() {
        RemoteWebDriver wd = driver();
        wd.get(baseUrl() + "/");
        WebElement tanya = wd.findElement(By.id("tanya"));
        assertThat(tanya.getAttribute("class")).contains("sprite");
    }

    @Test
    @DisplayName("Sam the dog sprite element is present in the DOM")
    void samDogSpriteIsPresent() {
        RemoteWebDriver wd = driver();
        wd.get(baseUrl() + "/");
        WebElement sam = wd.findElement(By.id("sam"));
        assertThat(sam).isNotNull();
        assertThat(sam.getAttribute("class")).contains("sprite");
    }

    @Test
    @DisplayName("Bed furniture element is present in the DOM")
    void bedFurnitureIsPresent() {
        RemoteWebDriver wd = driver();
        wd.get(baseUrl() + "/");
        WebElement bed = wd.findElement(By.id("bed"));
        assertThat(bed).isNotNull();
    }

    @Test
    @DisplayName("Side panel contains stats panel")
    void statsPanelIsPresent() {
        RemoteWebDriver wd = driver();
        wd.get(baseUrl() + "/");
        WebElement statsPanel = wd.findElement(By.id("stats-panel"));
        assertThat(statsPanel).isNotNull();
        // At minimum, energy bar should be in the DOM
        WebElement energyBar = wd.findElement(By.id("bar-energy"));
        assertThat(energyBar).isNotNull();
    }

    @Test
    @DisplayName("Quest panel is present with at least one quest item")
    void questPanelHasAtLeastOneQuest() {
        RemoteWebDriver wd = driver();
        wd.get(baseUrl() + "/");
        // Allow brief time for JS fetch to populate the quest list
        try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        WebElement questPanel = wd.findElement(By.id("quest-panel"));
        assertThat(questPanel).isNotNull();
        List<WebElement> questItems = wd.findElements(By.className("quest-item"));
        assertThat(questItems).isNotEmpty();
    }

    @Test
    @DisplayName("Relationships panel shows husband and father entries")
    void relationshipsPanelShowsBothNpcs() {
        RemoteWebDriver wd = driver();
        wd.get(baseUrl() + "/");
        WebElement relsPanel = wd.findElement(By.id("rels-panel"));
        assertThat(relsPanel.getText()).contains("Муж");
        assertThat(relsPanel.getText()).contains("Папа");
    }

    @Test
    @DisplayName("Game time header element is present")
    void gameTimeHeaderIsPresent() {
        RemoteWebDriver wd = driver();
        wd.get(baseUrl() + "/");
        WebElement gameTime = wd.findElement(By.id("game-time"));
        assertThat(gameTime.getText()).containsPattern("День \\d+");
    }

    @Test
    @DisplayName("/api/demo/status returns valid JSON with character data")
    void statusApiReturnsValidJson() {
        RemoteWebDriver wd = driver();
        wd.get(baseUrl() + "/api/demo/status");
        String body = wd.getPageSource();
        assertThat(body).contains("character");
        assertThat(body).contains("Tanya");
        assertThat(body).contains("gameTime");
    }
}
