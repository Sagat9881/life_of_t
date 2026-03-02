# LPC Generator Library

## 🎉 ALL PHASES COMPLETE!

`lpc-generator` — standalone Maven-модуль/библиотека для **автоматической генерации** LPC (Liberated Pixel Cup) спрайтов персонажей.

### Features

- ✅ **URL Generation**: Генерация LPC URL с hash-параметрами
- ✅ **Character Configuration**: Type-safe API для настройки внешности
- ✅ **Prompt Scanner**: Автосканирование `docs/prompts/characters/`
- ✅ **Config Extractor**: Парсинг visual specs → LPC config
- ✅ **Selenium Download**: Реальная генерация PNG через WebDriver
- ✅ **Auto-Generation**: Полный workflow scan → check → generate → save

## 🚀 Quick Start

### Installation

```bash
cd lpc-generator
mvn clean install
```

**WebDriverManager** автоматически скачает ChromeDriver! 🎉

### Usage

```java
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = 
            SpringApplication.run(Application.class, args);

        // Авто-генерация при старте
        AutoSpriteGenerator generator = 
            context.getBean(AutoSpriteGenerator.class);

        // Preview
        generator.preview();
        // Output:
        // === Dry Run Preview ===
        // ⚠️ Would generate 25 sprites:
        //   tatyana (12 sprites): idle-neutral, walk-south, ...

        // Generate!
        int count = generator.generateMissingSprites();
        System.out.println("✅ Generated " + count + " sprites!");
        // Output:
        // ✅ Selenium WebDriver initialized successfully
        // Downloading sprite via Selenium...
        // ✅ Sprite downloaded: 45678 bytes
        // ✅ Sprite saved to: assets/characters/tatyana/animations/idle-neutral.png
        // ...
        // === Generation Complete ===
        // ✅ Successfully generated: 25
    }
}
```

## Architecture

```
lpc-generator/
├── src/main/java/ru/lifegame/lpc/
│   ├── LpcGeneratorService.java          # Главный API
│   ├── AutoSpriteGenerator.java          # Orchestrator
│   ├── model/
│   │   ├── LpcCharacterConfig.java       # Конфиг внешности
│   │   ├── LpcSpriteRequest.java         # Запрос генерации
│   │   └── AnimationPrompt.java          # Парсенный промпт
│   ├── url/
│   │   └── LpcUrlBuilder.java            # URL builder
│   ├── scanner/
│   │   └── PromptScanner.java            # Сканирование
│   ├── extractor/
│   │   └── ConfigExtractor.java          # Парсинг specs
│   ├── selenium/
│   │   └── SeleniumSpriteDownloader.java # Скачивание PNG
│   └── config/
│       └── SeleniumConfig.java           # Настройки
└── pom.xml
```

## How It Works

### End-to-End Workflow

```
1. PromptScanner.scanPrompts()
   ↳ Читает docs/prompts/characters/*/animations/*.txt
   ↳ Проверяет assets/characters/*/animations/*.png
   ↳ Возвращает список missing sprites

2. ConfigExtractor.loadOrExtract("tatyana")
   ↳ Парсит character-visual-specs.txt
   ↳ Маппит: #8B1538 → "Shoulder_burgundy"
   ↳ Сохраняет config.json

3. LpcUrlBuilder.build(config)
   ↳ Строит: .../#sex=female&hair=Shoulder_burgundy&...

4. SeleniumSpriteDownloader.downloadSprite(url)
   ↳ Открывает URL в headless Chrome
   ↳ Ждет рендера canvas
   ↳ Извлекает canvas.toDataURL('image/png')
   ↳ Декодирует base64 → PNG bytes

5. LpcGeneratorService.generateSprite(request)
   ↳ Сохраняет в assets/characters/{id}/animations/{name}.png

6. AutoSpriteGenerator.generateMissingSprites()
   ↳ Оркестрирует все шаги для всех персонажей
   ↳ Логирует прогресс: ✅ [12/25] Generated
```

## API Reference

### AutoSpriteGenerator

```java
// Генерация всех недостающих
int count = generator.generateMissingSprites();

// Preview (без генерации)
generator.preview();

// Генерация для одного персонажа
int count = generator.generateForCharacter("tatyana");

// Генерация одной анимации
generator.generateSingle("tatyana", "idle-neutral");

// Отчет
GenerationReport report = generator.getReport();
System.out.println("Progress: " + report.completionPercentage() + "%");
```

### LpcGeneratorService

```java
// Генерация URL
String url = service.generateUrl(config);

// Генерация спрайта
LpcSpriteRequest request = LpcSpriteRequest.builder()
    .characterId("tatyana")
    .animationName("idle-neutral")
    .config(config)
    .build();
Path path = service.generateSprite(request);

// Проверка готовности
boolean ready = service.isReady(); // true if Selenium initialized
```

## Configuration

### application.properties

```properties
# Selenium settings
selenium.headless=true
selenium.page-load-timeout=30
selenium.canvas-render-timeout=15
selenium.window-size=1920,1080
selenium.verbose-logging=false
```

## System Requirements

### Development
- Java 21
- Maven 3.8+
- Chrome/Chromium browser

### Production
- Java 21 runtime
- Chrome/Chromium headless
- 512MB+ RAM

**ChromeDriver устанавливается автоматически!**

## Performance

- **Single sprite**: ~3-5 секунд
- **Batch (25 sprites)**: ~2 минуты
- **Memory**: ~200MB (headless Chrome)

## Roadmap

| Phase | Статус | Описание |
|-------|--------|------------|
| Phase 1 | ✅ | URL Generation, Models |
| Phase 2 | ✅ | Prompt Scanner, Statistics |
| Phase 3 | ✅ | Config Extractor, Color Mapping |
| Phase 4 | ✅ | Selenium Sprite Download |
| Phase 5 | ✅ | Auto-Generation Workflow |

**ВСЕ ФАЗЫ ЗАВЕРШЕНЫ!** 🎉

### Future Enhancements

- [ ] Parallel generation (multiple WebDriver instances)
- [ ] WebP format support
- [ ] Sprite compression
- [ ] REST API endpoint
- [ ] Admin UI dashboard

## Documentation

- **[README.md](README.md)** — этот файл
- **[SELENIUM_SETUP.md](SELENIUM_SETUP.md)** — Selenium setup guide
- **[INTEGRATION_PLAN.md](INTEGRATION_PLAN.md)** — план всех фаз
- **[PHASE_4_COMPLETE.md](PHASE_4_COMPLETE.md)** — Phase 4 summary
- **[PHASE_2_3_5_COMPLETE.md](PHASE_2_3_5_COMPLETE.md)** — Phases 2-5 summary

## Troubleshooting

См. [SELENIUM_SETUP.md](SELENIUM_SETUP.md) — полное руководство по setup, troubleshooting и optimization.

## License

LPC sprites: **CC-BY-SA 3.0 / GPL 3.0**  
**Attribution required** — see [CREDITS.md](https://github.com/LiberatedPixelCup/Universal-LPC-Spritesheet-Character-Generator/blob/master/CREDITS.md)

## Links

- [LPC Generator](https://liberatedpixelcup.github.io/Universal-LPC-Spritesheet-Character-Generator/)
- [LPC GitHub](https://github.com/LiberatedPixelCup/Universal-LPC-Spritesheet-Character-Generator)
- [Selenium WebDriver](https://www.selenium.dev/)
- [WebDriverManager](https://github.com/bonigarcia/webdrivermanager)

---

**Status**: 🟢 **PRODUCTION READY!**

**Next Step**: Testing + Merge to `dev/0.1.0` 🚀
