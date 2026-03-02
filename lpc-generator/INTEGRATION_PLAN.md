# LPC Generator Integration Plan

## 🎯 Цель

Создать систему, которая автоматически:
1. Сканирует промпты анимаций в `docs/prompts/characters/{characterId}/animations/`
2. Проверяет наличие соответствующих спрайтов в `assets/characters/{characterId}/animations/`
3. Генерирует недостающие спрайты через LPC Generator API
4. Сохраняет спрайты в соответствующие папки

---

## 📁 Структура проекта

### Промпты (источник правды)

```
docs/prompts/
└── characters/
    ├── tatyana/
    │   ├── base-sprite.txt                 # Базовый внешний вид
    │   ├── character-visual-specs.txt      # Детали внешности
    │   └── animations/
    │       ├── idle-neutral.txt            # 24 кадра, 6fps
    │       ├── idle-happy.txt
    │       ├── idle-tired.txt
    │       ├── walk-south.txt              # 25 кадров, 10fps
    │       ├── walk-north.txt
    │       ├── work-computer.txt
    │       ├── sleep.txt
    │       ├── emotion-joy.txt
    │       └── grooming-mirror.txt
    │
    ├── sam/
    │   └── animations/
    │       ├── idle-neutral.txt
    │       └── ...
    │
    └── garfield/
        └── animations/
            ├── idle-sitting.txt
            └── ...
```

### Ассеты (генерируемые спрайты)

```
assets/characters/
├── tatyana/
│   ├── config.json                      # LPC конфигурация
│   └── animations/
│       ├── idle-neutral.png             # Сгенерировано из idle-neutral.txt
│       ├── idle-happy.png
│       ├── idle-tired.png
│       ├── walk-south.png
│       └── ...
│
├── sam/
└── garfield/
```

### Правила именования

**Prompt**: `{animation-name}.txt` → **Sprite**: `{animation-name}.png`

Prимеры:
- `idle-neutral.txt` → `idle-neutral.png`
- `walk-south.txt` → `walk-south.png`
- `emotion-joy.txt` → `emotion-joy.png`

---

## 🔧 Phase 1: Создание модуля (✅ Готово)

### Что сделано

- [x] Создан Maven-модуль `lpc-generator`
- [x] `LpcGeneratorService` — главный API
- [x] `LpcUrlBuilder` — генерация URL с параметрами
- [x] `LpcCharacterConfig` — конфигурация персонажа
- [x] `LpcSpriteRequest` — запрос на генерацию
- [x] Добавлена зависимость в `application` и `demo` модули

---

## 🔧 Phase 2: Prompt Scanner (🚧 В разработке)

### Задачи

1. **Сканирование промптов**
   - Читать все `.txt` файлы из `docs/prompts/characters/*/animations/`
   - Извлекать имя персонажа и анимации
   - Создавать индекс: `Map<String, List<String>>` (characterId → animations)

2. **Парсинг промптов**
   - Извлечь из XML параметры персонажа:
     - Hair color (#8B1538 → "burgundy")
     - Clothing (beige sweater, gray-blue jeans)
     - Accessories (gold heart necklace)
   - Конвертировать в `LpcCharacterConfig`

3. **Проверка существования спрайтов**
   - Для каждого промпта проверить: `assets/characters/{characterId}/animations/{animation}.png`
   - Создать список недостающих спрайтов

### Код

```java
@Service
public class PromptScanner {

    private static final String PROMPTS_DIR = "docs/prompts/characters";
    private static final String ASSETS_DIR = "assets/characters";

    public Map<String, List<AnimationPrompt>> scanPrompts() {
        // 1. Scan docs/prompts/characters/*/animations/*.txt
        // 2. Parse each prompt file
        // 3. Create AnimationPrompt objects
        // 4. Group by characterId
        return promptsByCharacter;
    }

    public List<AnimationPrompt> findMissingSprites() {
        Map<String, List<AnimationPrompt>> prompts = scanPrompts();
        List<AnimationPrompt> missing = new ArrayList<>();

        for (var entry : prompts.entrySet()) {
            String characterId = entry.getKey();
            for (AnimationPrompt prompt : entry.getValue()) {
                Path spritePath = buildSpritePath(characterId, prompt.getName());
                if (!Files.exists(spritePath)) {
                    missing.add(prompt);
                }
            }
        }

        return missing;
    }

    private Path buildSpritePath(String characterId, String animationName) {
        return Paths.get(ASSETS_DIR, characterId, "animations", animationName + ".png");
    }
}
```

---

## 🔧 Phase 3: Config Extractor (🚧 В разработке)

### Задачи

1. **Парсинг visual specs**
   - Читать `docs/prompts/characters/{characterId}/character-visual-specs.txt`
   - Извлекать цвета, одежду, аксессуары
   - Маппинг на LPC слои

2. **Color Mapping**
   ```
   #8B1538 (burgundy) → LPC hair color: "burgundy" or closest
   #F5E6D3 (beige)    → LPC clothes: "Longsleeve_beige"
   #6B7280 (gray-blue)→ LPC pants: "Pants_gray_blue"
   ```

3. **Сохранение config.json**
   - Создавать `assets/characters/{characterId}/config.json`
   - Сохранить `LpcCharacterConfig` в JSON

### Код

```java
@Service
public class ConfigExtractor {

    public LpcCharacterConfig extractFromPrompt(Path visualSpecsPath) {
        // 1. Parse XML or text file
        // 2. Extract appearance data
        // 3. Map to LPC layers
        // 4. Return LpcCharacterConfig
    }

    public void saveConfig(String characterId, LpcCharacterConfig config) {
        Path configPath = Paths.get("assets/characters", characterId, "config.json");
        // Serialize to JSON and save
    }

    public LpcCharacterConfig loadConfig(String characterId) {
        Path configPath = Paths.get("assets/characters", characterId, "config.json");
        // Deserialize from JSON
    }
}
```

---

## 🔧 Phase 4: Sprite Generator API (📋 Планируется)

### Варианты реализации

#### Option 1: Selenium WebDriver (рекомендуемый)

**Pros**:
- ✅ Работает с любыми JS-генераторами
- ✅ Полное управление браузером
- ✅ Вызержанная технология

**Cons**:
- ⚠️ Требует ChromeDriver
- ⚠️ Медленно (нужен рендер страницы)

```java
@Service
public class SeleniumSpriteGenerator {

    private WebDriver driver;

    public byte[] generateSprite(String lpcUrl) {
        driver.get(lpcUrl);
        
        // Wait for LPC Generator to render
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(d -> ((JavascriptExecutor) d)
            .executeScript("return document.querySelector('canvas') != null"));

        // Extract canvas as PNG
        String script = """
            var canvas = document.querySelector('canvas');
            return canvas.toDataURL('image/png');
        """;
        String base64Image = (String) ((JavascriptExecutor) driver).executeScript(script);
        
        // Decode base64 to bytes
        return Base64.getDecoder().decode(base64Image.split(",")[1]);
    }
}
```

#### Option 2: Server-Side Composer

**Pros**:
- ✅ Быстро (нет браузера)
- ✅ Полный контроль

**Cons**:
- ⚠️ Нужно скачать все LPC слои
- ⚠️ Сложная логика композиции

#### Option 3: External API Service

**Pros**:
- ✅ Простота интеграции

**Cons**:
- ⚠️ Зависимость от стороннего сервиса
- ⚠️ Такого API не существует (нужно создавать самому)

---

## 🔧 Phase 5: Auto-Generation Workflow (📋 Планируется)

### Алгоритм

```java
@Service
public class AutoSpriteGenerator {

    @Autowired
    private PromptScanner promptScanner;

    @Autowired
    private ConfigExtractor configExtractor;

    @Autowired
    private LpcGeneratorService lpcService;

    /**
     * Main workflow: scan prompts, generate missing sprites
     */
    public void generateMissingSprites() {
        log.info("Starting auto-generation workflow...");

        // 1. Scan all prompts
        Map<String, List<AnimationPrompt>> promptsByCharacter = promptScanner.scanPrompts();
        log.info("Found {} characters with prompts", promptsByCharacter.size());

        // 2. For each character
        for (var entry : promptsByCharacter.entrySet()) {
            String characterId = entry.getKey();
            List<AnimationPrompt> prompts = entry.getValue();

            log.info("Processing character: {}", characterId);

            // 3. Load or extract character config
            LpcCharacterConfig config = loadOrExtractConfig(characterId);

            // 4. For each animation prompt
            for (AnimationPrompt prompt : prompts) {
                String animationName = prompt.getName();
                Path spritePath = buildSpritePath(characterId, animationName);

                // 5. Check if sprite exists
                if (Files.exists(spritePath)) {
                    log.debug("Sprite already exists: {}", spritePath);
                    continue;
                }

                log.info("Generating sprite: {}/{}", characterId, animationName);

                // 6. Generate sprite
                try {
                    LpcSpriteRequest request = LpcSpriteRequest.builder()
                        .characterId(characterId)
                        .animationName(animationName)
                        .config(config)
                        .build();

                    Path generated = lpcService.generateSprite(request);
                    log.info("✅ Generated: {}", generated);

                } catch (Exception e) {
                    log.error("❌ Failed to generate sprite: {}/{}", characterId, animationName, e);
                }
            }
        }

        log.info("✅ Auto-generation complete!");
    }

    private LpcCharacterConfig loadOrExtractConfig(String characterId) {
        // Try to load existing config.json
        try {
            return configExtractor.loadConfig(characterId);
        } catch (Exception e) {
            // Extract from visual specs prompt
            Path visualSpecsPath = Paths.get("docs/prompts/characters", characterId, "character-visual-specs.txt");
            LpcCharacterConfig config = configExtractor.extractFromPrompt(visualSpecsPath);
            configExtractor.saveConfig(characterId, config);
            return config;
        }
    }
}
```

### Использование

```java
// В Application.java или DemoApplication.java

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);

        // Запустить авто-генерацию при старте
        AutoSpriteGenerator generator = context.getBean(AutoSpriteGenerator.class);
        generator.generateMissingSprites();
    }
}
```

---

## 🚦 Roadmap

### Phase 1: Модуль ✅
- [x] Maven module structure
- [x] LpcGeneratorService
- [x] LpcUrlBuilder
- [x] Model classes
- [x] Integration with application/demo

### Phase 2: Prompt Scanner 🚧
- [ ] PromptScanner service
- [ ] AnimationPrompt model
- [ ] Directory scanning logic
- [ ] Unit tests

### Phase 3: Config Extractor 🚧
- [ ] ConfigExtractor service
- [ ] XML/text parsing
- [ ] Color mapping
- [ ] config.json save/load

### Phase 4: Sprite Generator 📋
- [ ] Choose implementation (Selenium recommended)
- [ ] SeleniumSpriteGenerator
- [ ] ChromeDriver setup
- [ ] Canvas extraction
- [ ] Error handling

### Phase 5: Auto-Generation 📋
- [ ] AutoSpriteGenerator service
- [ ] Full workflow integration
- [ ] Logging & progress tracking
- [ ] Batch generation
- [ ] Startup hook

---

## 👥 Некст степс

### Ты (разработчик)
1. Review этот plan
2. Подтвердить approach (или предложить изменения)
3. Сказать: "Погнали, Phase 2!" 🚀

### Я (ассистент)
1. Создать PromptScanner
2. Реализовать ConfigExtractor
3. Интегрировать Selenium
4. Связать все вместе в AutoSpriteGenerator

---

**Статус**: 🟡 Phase 1 Complete, Ready for Phase 2
