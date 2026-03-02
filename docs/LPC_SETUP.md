# LPC Sprite Generator - Setup Guide

Полное руководство по настройке системы генерации LPC спрайтов.

## Быстрый старт

### 1. Клонирование репозитория

```bash
# Клонируй с submodule
git clone --recurse-submodules https://github.com/Sagat9881/life_of_t.git

# Или если уже склонировал
git submodule update --init --recursive
```

### 2. Сборка проекта

```bash
# Полная сборка с генерацией спрайтов
mvn clean install

# Спрайты будут в: lpc-sprite-compositor/target/generated-sprites/
```

## Архитектура

```
life_of_t/
├── lpc-spritesheets/                  # Git submodule
│   └── spritesheets/                  # PNG файлы слоёв
│       ├── body/
│       ├── hair/
│       ├── clothes/
│       └── ...
│
├── lpc-sprite-compositor/         # Java композитор
│   ├── src/main/java/
│   │   └── LPCSpriteCompositor.java
│   ├── src/main/resources/
│   │   └── sprite-presets.json
│   └── target/generated-sprites/  # Генерируемые PNG
│
└── .github/workflows/
    └── generate-sprites.yml       # CI/CD автоматизация
```

## Способы генерации

### 1. Предгенерация при сборке

Автоматически запускается в фазе `generate-resources`:

```bash
mvn clean install
```

### 2. Ручная генерация

```bash
cd lpc-sprite-compositor
mvn exec:java \
  -Dexec.mainClass="ru.lifegame.lpc.compositor.PreGenerateSprites" \
  -Dexec.args="sprite-presets.json target/my-sprites"
```

### 3. Runtime генерация в Java

```java
import ru.lifegame.lpc.compositor.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Example {
    public static void main(String[] args) throws Exception {
        // Конфиг персонажа
        CharacterConfig tatyana = CharacterConfig.builder()
            .id("tatyana-office")
            .gender("female")
            .body("light")
            .hairStyle("ponytail")
            .hair("brown")
            .clothes(List.of("blouse-white", "skirt-pencil"))
            .accessories(List.of("glasses"))
            .build();
        
        // Композитор
        Path spritesheetsPath = Paths.get("lpc-spritesheets/spritesheets");
        LPCSpriteCompositor compositor = new LPCSpriteCompositor(spritesheetsPath);
        
        // Генерация
        BufferedImage sprite = compositor.generateSprite(tatyana);
        
        // Сохранение
        compositor.saveSprite(sprite, Paths.get("tatyana-office.png"));
    }
}
```

## Настройка пресетов

Редактируй `sprite-presets.json`:

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
    },
    {
      "id": "tatyana-work",
      "gender": "female",
      "body": "light",
      "hairStyle": "ponytail",
      "hair": "brown",
      "clothes": ["blouse-office", "skirt-black"],
      "accessories": ["glasses"]
    }
  ]
}
```

## Доступные слои

### Body
- `light`, `dark`, `tanned`, `darkelf`, `reddemon`

### Hair Styles
- `long`, `short`, `ponytail`, `messy`, `curly`

### Hair Colors
- `brown`, `blonde`, `black`, `red`, `white`

### Clothes
- `shirt-white`, `blouse-office`, `hoodie-grey`
- `pants-jeans`, `pants-joggers`, `skirt-black`
- `pajamas-blue`

### Accessories
- `glasses`, `hat`, `scarf`

## CI/CD Интеграция

GitHub Actions автоматически генерирует спрайты:

1. **При push** в `main`, `dev/*`, `feature/*`
2. **При PR** в `main`, `dev/*`
3. **Вручную** через GitHub UI

Артефакты доступны 30 дней после сборки.

## Трублшутинг

### Submodule не инициализирован

```bash
git submodule update --init --recursive
```

### Не найдены PNG файлы

Проверь, что `lpc-spritesheets/spritesheets/` существует:

```bash
ls -la lpc-spritesheets/spritesheets/
```

### Спрайты не генерируются

Проверь логи:

```bash
mvn clean install -X
```

## Лицензия

LPC Spritesheets: **CC-BY-SA 3.0 / GPL 3.0**

Источник: [Universal-LPC-Spritesheet-Character-Generator](https://github.com/sanderfrenken/Universal-LPC-Spritesheet-Character-Generator)