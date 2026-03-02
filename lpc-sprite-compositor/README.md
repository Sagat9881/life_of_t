# LPC Sprite Compositor

Локальный композитор спрайтов LPC (Liberated Pixel Cup) для Java.

## Особенности

- ✅ **Локальная композиция PNG** — никаких сетевых запросов
- ✅ **Предгенерация в CI/CD** — спрайты генерируются при сборке
- ✅ **Поддержка слоёв** — тело, волосы, одежда, аксессуары
- ✅ **Git Submodule** — исходники из Universal-LPC-Generator

## Архитектура

```
lpc-sprite-compositor/
├── src/main/java/
│   └── ru/lifegame/lpc/compositor/
│       ├── LPCSpriteCompositor.java      # Основной композитор
│       ├── CharacterConfig.java          # Конфиг персонажа
│       ├── LayerConfigLoader.java        # Загрузчик метаданных
│       └── PreGenerateSprites.java       # CLI для предгенерации
│
├── src/main/resources/
│   └── sprite-presets.json           # Пресеты для генерации
│
└── pom.xml                           # Maven конфигурация
```

## Использование

### 1. Инициализация submodule

```bash
# Клонируй LPC Generator
git submodule update --init --recursive
```

### 2. Предгенерация при сборке

```bash
# Сборка автоматически генерирует спрайты
mvn clean install

# Результат: target/generated-sprites/*.png
```

### 3. Runtime генерация

```java
import ru.lifegame.lpc.compositor.*;

// Создай конфиг персонажа
CharacterConfig config = CharacterConfig.builder()
    .id("tatyana-custom")
    .gender("female")
    .body("light")
    .hairStyle("long")
    .hair("brown")
    .clothes(List.of("shirt-white", "pants-jeans"))
    .build();

// Инициализируй композитор
Path spritesheets = Paths.get("lpc-spritesheets/spritesheets");
LPCSpriteCompositor compositor = new LPCSpriteCompositor(spritesheets);

// Генерируй спрайт
BufferedImage sprite = compositor.generateSprite(config);

// Сохрани
compositor.saveSprite(sprite, Paths.get("output/tatyana.png"));
```

## Формат sprite-presets.json

```json
{
  "characters": [
    {
      "id": "tatyana-default",
      "gender": "female",
      "body": "light",
      "hairStyle": "long",
      "hair": "brown",
      "clothes": ["shirt-white", "pants-jeans"],
      "accessories": []
    }
  ]
}
```

## CI/CD Интеграция

Спрайты генерируются автоматически в фазе `generate-resources` Maven:

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>generate-sprites</id>
            <phase>generate-resources</phase>
            <goals><goal>java</goal></goals>
            <configuration>
                <mainClass>ru.lifegame.lpc.compositor.PreGenerateSprites</mainClass>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Справка по LPC Spritesheets

Спрайты из [Universal-LPC-Spritesheet-Character-Generator](https://github.com/sanderfrenken/Universal-LPC-Spritesheet-Character-Generator):

- **Размер**: 832x1344 (13 фреймов × 21 анимация)
- **Слои**: Body, Hair, Clothes, Weapons, Accessories
- **Лицензия**: CC-BY-SA 3.0 / GPL 3.0

## Логирование

Использует SLF4J. Пример вывода:

```
INFO  [LPCSpriteCompositor] Generating sprite for config: tatyana-default
INFO  [LPCSpriteCompositor] Sprite saved to: target/generated-sprites/tatyana-default.png
```

## TODO

- [ ] Поддержка дополнительных слоёв (weapons, shields)
- [ ] Кеширование загруженных PNG
- [ ] Валидация конфигов
- [ ] Unit-тесты для композиции