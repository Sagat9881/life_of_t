# Selenium Setup Guide

## 🚀 Quick Start

### Prerequisites

- Java 21
- Chrome/Chromium browser installed

**WebDriverManager** автоматически скачает ChromeDriver — ручная установка **НЕ нужна**! 🎉

### Installation

```bash
cd lpc-generator
mvn clean install
```

WebDriverManager скачает нужную версию ChromeDriver автоматически при первом запуске.

---

## 👨‍💻 Development Setup

### Option 1: Local Chrome (рекомендуется)

**Windows**:
```bash
# Chrome usually installed by default
# Verify:
chrome --version
```

**macOS**:
```bash
brew install --cask google-chrome
```

**Linux**:
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install chromium-browser

# Fedora
sudo dnf install chromium
```

### Option 2: Chromium (lightweight)

**macOS**:
```bash
brew install chromium
```

**Linux**:
```bash
sudo apt install chromium-browser  # Ubuntu/Debian
sudo dnf install chromium          # Fedora
```

---

## ⚙️ Configuration

### application.properties

```properties
# Selenium settings
selenium.headless=true
selenium.page-load-timeout=30
selenium.canvas-render-timeout=15
selenium.window-size=1920,1080
selenium.verbose-logging=false
```

### Environment Variables

```bash
# Force specific Chrome binary (optional)
export CHROME_BIN=/path/to/chrome

# Disable headless for debugging
export SELENIUM_HEADLESS=false
```

---

## 🖥️ Server Deployment

### Docker

**Dockerfile**:
```dockerfile
FROM openjdk:21-slim

# Install Chrome
RUN apt-get update && apt-get install -y \
    wget \
    gnupg \
    unzip \
    && wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - \
    && echo "deb http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list \
    && apt-get update \
    && apt-get install -y google-chrome-stable \
    && rm -rf /var/lib/apt/lists/*

# Copy application
COPY target/*.jar app.jar

# Run
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**docker-compose.yml**:
```yaml
version: '3.8'
services:
  app:
    build: .
    environment:
      - SELENIUM_HEADLESS=true
    volumes:
      - ./assets:/app/assets
```

### Linux Server (без Docker)

```bash
# Install Chrome
wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
sudo dpkg -i google-chrome-stable_current_amd64.deb
sudo apt-get install -f

# Verify
google-chrome --version

# Run application
java -jar application.jar
```

---

## 🐛 Troubleshooting

### Issue: "ChromeDriver not found"

**Solution**: WebDriverManager должен скачать автоматически. Если нет:
```bash
# Manually download (temporary)
wget https://chromedriver.storage.googleapis.com/LATEST_RELEASE
wget https://chromedriver.storage.googleapis.com/$(cat LATEST_RELEASE)/chromedriver_linux64.zip
unzip chromedriver_linux64.zip
sudo mv chromedriver /usr/local/bin/
```

### Issue: "Chrome binary not found"

**Solution**: Укажи путь к Chrome:
```bash
export CHROME_BIN=/usr/bin/google-chrome
# или
export CHROME_BIN=/usr/bin/chromium-browser
```

### Issue: "Failed to initialize WebDriver"

**Debug mode**: Отключи headless для проверки:
```properties
selenium.headless=false
selenium.verbose-logging=true
```

### Issue: Slow generation

**Optimize**:
```properties
# Reduce timeouts
selenium.page-load-timeout=15
selenium.canvas-render-timeout=10

# Smaller window
selenium.window-size=1280,720
```

### Issue: Out of memory on server

**Solution**: Ограничь Java heap:
```bash
java -Xmx512m -jar application.jar
```

---

## 📊 Performance Tips

### 1. Reuse WebDriver

✅ **Good**: WebDriver создается один раз (@PostConstruct)  
❌ **Bad**: Создавать новый WebDriver для каждого спрайта

### 2. Batch Generation

```java
// Generate all at once
autoGenerator.generateMissingSprites();

// NOT one-by-one in loop
```

### 3. Headless Mode

Всегда используй `headless=true` на production для скорости.

### 4. Cache Sprites

```java
request.setOverwrite(false); // Skip existing
```

---

## 🧪 Testing

### Unit Test

```java
@SpringBootTest
class SeleniumSpriteDownloaderTest {

    @Autowired
    private SeleniumSpriteDownloader downloader;

    @Test
    void testDownloadSprite() throws Exception {
        String url = "https://liberatedpixelcup.github.io/Universal-LPC-Spritesheet-Character-Generator/#sex=female&body=Body_Color_light";
        
        byte[] sprite = downloader.downloadSprite(url);
        
        assertNotNull(sprite);
        assertTrue(sprite.length > 1000); // PNG should be >1KB
    }
}
```

### Manual Test

```bash
# Run demo
cd demo
mvn spring-boot:run

# Check logs for:
# ✅ Selenium WebDriver initialized successfully
# ✅ Sprite downloaded: 45678 bytes
```

---

## 📚 References

- [Selenium Documentation](https://www.selenium.dev/documentation/)
- [WebDriverManager](https://github.com/bonigarcia/webdrivermanager)
- [Chrome Options](https://chromedriver.chromium.org/capabilities)

---

**Status**: 🟢 Ready for Production
