# LPC Generator Library

## Overview

`lpc-generator` — standalone Maven-модуль/библиотека для автоматической генерации LPC (Liberated Pixel Cup) спрайтов персонажей.

### Features

- ✅ **URL Generation**: Генерация LPC URL с hash-параметрами
- ✅ **Character Configuration**: Type-safe API для настройки внешности
- ✅ **Prompt Scanner**: Автосканирование `docs/prompts/characters/`
- ✅ **Config Extractor**: Парсинг visual specs → LPC config
- ✅ **Auto-Generation**: Полный workflow scan → check → generate
- 🚧 **API Integration**: Скачивание спрайтов через API (Phase 4 - TODO)

## Installation

### As Maven Dependency

```xml
<dependency>
    <groupId>ru.lifegame</groupId>
    <artifactId>lpc-generator</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## Quick Start

### 1. Auto-Generate All Missing Sprites

```java
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);

        // Авто-генерация при старте
        AutoSpriteGenerator generator = context.getBean(AutoSpriteGenerator.class);
        int generated = generator.generateMissingSprites();
        
        System.out.println("✅ Generated " + generated + " sprites!");
    }
}
```

### 2. Preview Missing Sprites (Dry Run)

```java
AutoSpriteGenerator generator = context.getBean(AutoSpriteGenerator.class);
generator.preview();

// Output:
// === Dry Run Preview ===
// ⚠️ Would generate 12 sprites:
//   tatyana (8 sprites):
//     - idle-neutral
//     - walk-south
//     - work-computer
//     ...
//   sam (4 sprites):
//     - idle-neutral
//     ...
```

### 3. Generate for Specific Character

```java
AutoSpriteGenerator generator = context.getBean(AutoSpriteGenerator.class);
int count = generator.generateForCharacter("tatyana");
System.out.println("Generated " + count + " sprites for Tatyana");
```

### 4. Manual URL Generation

```java
LpcGeneratorService service = context.getBean(LpcGeneratorService.class);

LpcCharacterConfig config = LpcCharacterConfig.builder()
    .sex("female")
    .body("Body_Color_light")
    .hair(List.of("Shoulder_burgundy"))
    .build();

config.addClothing("tops", "Longsleeve_beige");

String url = service.generateUrl(config);
// https://liberatedpixelcup.github.io/Universal-LPC-Spritesheet-Character-Generator/#sex=female&body=Body_Color_light&hair=Shoulder_burgundy&torso=Longsleeve_beige
```

## Architecture

```
lpc-generator/
├── src/main/java/ru/lifegame/lpc/
│   ├── LpcGeneratorService.java          # Главный API
│   ├── AutoSpriteGenerator.java          # Orchestrator workflow
│   ├── model/
│   │   ├── LpcCharacterConfig.java       # Конфиг внешности
│   │   ├── LpcSpriteRequest.java         # Запрос генерации
│   │   └── AnimationPrompt.java          # Парсенный промпт
│   ├── url/
│   │   └── LpcUrlBuilder.java            # URL с параметрами
│   ├── scanner/
│   │   └── PromptScanner.java            # Сканирование prompts
│   ├── extractor/
│   │   └── ConfigExtractor.java          # Парсинг visual specs
│   └── api/
│       └── (TODO: Selenium/API client)
└── pom.xml
```

## How It Works

### Workflow

```
1. PromptScanner.scanPrompts()
   ↳ Читает docs/prompts/characters/*/animations/*.txt
   ↳ Создает AnimationPrompt для каждого файла
   ↳ Проверяет наличие assets/characters/*/animations/*.png

2. ConfigExtractor.extractFromPrompt()
   ↳ Парсит character-visual-specs.txt
   ↳ Извлекает: цвет волос, одежду, аксессуары
   ↳ Маппит на LPC слои: #8B1538 → "Shoulder_burgundy"
   ↳ Сохраняет assets/characters/{id}/config.json

3. LpcGeneratorService.generateUrl()
   ↳ Строит URL: .../#sex=female&hair=Shoulder_burgundy&...

4. (TODO) LpcGeneratorService.generateSprite()
   ↳ Скачивает PNG через Selenium/API
   ↳ Сохраняет в assets/characters/{id}/animations/{name}.png

5. AutoSpriteGenerator.generateMissingSprites()
   ↳ Оркестрирует все шаги
   ↳ Batch generation для всех персонажей
```

### Directory Structure

**Prompts** (источник правды):
```
docs/prompts/characters/
└── tatyana/
    ├── character-visual-specs.txt    # Внешность
    └── animations/
        ├── idle-neutral.txt
        ├── walk-south.txt
        └── work-computer.txt
```

**Assets** (генерируемые):
```
assets/characters/
└── tatyana/
    ├── config.json                   # LPC config (auto-generated)
    └── animations/
        ├── idle-neutral.png           # Sprite (auto-generated)
        ├── walk-south.png
        └── work-computer.png
```

**Naming**: `{animation}.txt` → `{animation}.png`

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

// Получить отчет
GenerationReport report = generator.getReport();
System.out.println("Progress: " + report.completionPercentage() + "%");
```

### PromptScanner

```java
// Сканировать все промпты
Map<String, List<AnimationPrompt>> prompts = scanner.scanPrompts();

// Найти недостающие спрайты
List<AnimationPrompt> missing = scanner.findMissingSprites();

// Статистика
ScanStatistics stats = scanner.getStatistics();
System.out.printf("%d/%d sprites exist (%.1f%%)%n",
    stats.existingSprites(),
    stats.totalPrompts(),
    stats.completionPercentage());
```

### ConfigExtractor

```java
// Извлечь из prompt
LpcCharacterConfig config = extractor.extractFromPrompt("tatyana");

// Сохранить в JSON
extractor.saveConfig("tatyana", config);

// Загрузить из JSON
LpcCharacterConfig config = extractor.loadConfig("tatyana");

// Load or extract (auto)
LpcCharacterConfig config = extractor.loadOrExtract("tatyana");
```

## Configuration

### application.properties

```properties
# LPC Generator settings
lpc.generator.prompts-dir=docs/prompts/characters
lpc.generator.assets-dir=assets/characters
lpc.generator.base-url=https://liberatedpixelcup.github.io/Universal-LPC-Spritesheet-Character-Generator/
```

## Roadmap

### Phase 1: URL Generation ✅
- [x] LpcCharacterConfig model
- [x] LpcUrlBuilder
- [x] LpcGeneratorService basic API

### Phase 2: Prompt Scanner ✅
- [x] PromptScanner service
- [x] AnimationPrompt model
- [x] Directory scanning logic
- [x] Statistics API

### Phase 3: Config Extractor ✅
- [x] ConfigExtractor service
- [x] Visual specs parsing
- [x] Color mapping (#8B1538 → burgundy)
- [x] config.json save/load

### Phase 4: Sprite Generator API 🚧
- [ ] Choose implementation (Selenium recommended)
- [ ] SeleniumSpriteGenerator
- [ ] ChromeDriver setup
- [ ] Canvas extraction
- [ ] Error handling

### Phase 5: Auto-Generation ✅
- [x] AutoSpriteGenerator service
- [x] Full workflow integration
- [x] Logging & progress tracking
- [x] Batch generation
- [x] Preview/dry-run mode

## Next Steps

### Phase 4 Implementation Options

**Option 1: Selenium WebDriver** (рекомендуемый)
- Запускает headless Chrome
- Открывает LPC Generator URL
- Ждет рендера
- Извлекает canvas как PNG

**Option 2: Server-Side Composer**
- Скачивает LPC asset слои
- Композиция через Java ImageIO
- Быстрее, но сложнее

**Option 3: External API**
- Создать Node.js микросервис
- Использовать Puppeteer
- Java вызывает REST API

## License

LPC sprites: **CC-BY-SA 3.0 / GPL 3.0**  
**Attribution required** — see [CREDITS.md](https://github.com/LiberatedPixelCup/Universal-LPC-Spritesheet-Character-Generator/blob/master/CREDITS.md)

## Links

- [LPC Generator](https://liberatedpixelcup.github.io/Universal-LPC-Spritesheet-Character-Generator/)
- [LPC GitHub](https://github.com/LiberatedPixelCup/Universal-LPC-Spritesheet-Character-Generator)
- [Integration Plan](INTEGRATION_PLAN.md)
