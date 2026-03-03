# Генерация ассетов — Руководство

## Обзор системы

Asset Generator — модуль процедурной генерации графических ресурсов игры «Лиф оф T».
Все ассеты создаются программно на Java с использованием `java.awt` и `javax.imageio`
без внешних графических зависимостей.

Генерация запускается автоматически при старте приложения с флагом `--generate-assets`
или при первом запуске, если директория `generated-assets/` пустая.

---

## Архитектура модуля

```
asset-generator/src/main/java/ru/lifegame/assets/
├── AssetGenerator.java            — основной интерфейс генератора
├── AssetGeneratorApplication.java — Spring Boot точка входа (standalone)
├── AssetRequest.java              — запрос на генерацию конкретного ассета
├── AssetType.java                 — перечисление типов ассетов
├── GeneratorRegistry.java         — реестр доступных генераторов
├── config/
│   ├── AssetGenerationRunner.java — ApplicationRunner: запускает всё при старте
│   └── AssetGeneratorConfig.java  — Spring-конфигурация модуля
├── sprite/
│   ├── LpcSpriteCompositor.java   — компоновщик слоёв LPC-спрайтов
│   ├── CharacterDefinition.java   — описание персонажа (набор слоёв)
│   ├── SpriteLayer.java           — один слой (изображение + категория)
│   ├── SpriteLayerCategory.java   — категория слоя (BODY, CLOTHES, HAIR…)
│   ├── SpriteAction.java          — тип анимации (WALK, IDLE, RUN…)
│   ├── Direction.java             — направление (DOWN, UP, LEFT, RIGHT)
│   └── AnimationFrame.java        — один кадр анимации
└── texture/
    ├── ProceduralTextureGenerator.java — генератор тайловых текстур
    ├── CellGrid.java                   — сетка клеток для тайла
    ├── SymmetryMode.java               — режим симметрии
    └── TextureColorPalette.java        — палитра цветов по контексту
```

---

## Генерация текстур

### Принципы

Каждая текстура строится как тайл **N×N пикселей**, разбитый на **2×2 логические клетки**.
Генератор заполняет клетки случайными паттернами (noise, градиент, клетка) и применяет симметрию
для получения тайловой развёртки без видимых швов.

### CellGrid

```java
// Создать сетку 2x2 клетки с размером ячейки 32 пикселя
CellGrid grid = new CellGrid(2, 2, 32);
grid.fill(pattern, symmetryMode);
```

**Параметры:**
- `cols`, `rows` — количество клеток по горизонтали и вертикали
- `cellSize` — размер одной клетки в пикселях
- `pattern` — шаблон заполнения (FLAT, NOISE, CHECKER, GRADIENT)

### SymmetryMode

| Константа      | Описание                                             |
|----------------|------------------------------------------------------|
| `NONE`         | Без симметрии, случайное заполнение                  |
| `HORIZONTAL`   | Зеркало по горизонтали (левая = отражению правой)    |
| `VERTICAL`     | Зеркало по вертикали                                 |
| `QUAD`         | Четырёхкратная симметрия (все 4 угла идентичны)      |

Режим `QUAD` даёт наилучший результат для тайлов пола и стен.

### TextureColorPalette

Палитры сгруппированы по игровым локациям:

| Константа      | Цвета                        | Применение                 |
|----------------|------------------------------|----------------------------|
| `COZY_HOME`    | Бежевые, тёплые              | Пол и стены квартиры       |
| `PARK`         | Зелёные, коричневые          | Трава, земля               |
| `OFFICE`       | Серые, синеватые             | Офисный интерьер           |
| `CAFE`         | Кремовые, терракотовые       | Кафе, кухня                |
| `STREET`       | Серые, асфальтовые           | Улица, тротуар            |

### Добавление новой текстуры

1. Добавить константу в `TextureColorPalette` с нужными цветами:
   ```java
   HOSPITAL(new Color(240, 245, 248), new Color(200, 215, 220), new Color(180, 200, 210));
   ```

2. Создать `AssetRequest` с нужными параметрами:
   ```java
   AssetRequest request = AssetRequest.texture("hospital_floor", 64,
       TextureColorPalette.HOSPITAL, SymmetryMode.QUAD);
   ```

3. Зарегистрировать в `GeneratorRegistry`:
   ```java
   registry.register(AssetType.TEXTURE, "hospital_floor",
       () -> textureGenerator.generate(request));
   ```

4. Добавить вызов в `AssetGenerationRunner.run()`.

---

## Генерация спрайтов персонажей

### LPC-стандарт

Спрайты персонажей следуют стандарту **Liberated Pixel Cup (LPC)**:
- Лист спрайтов: 832×1344 пикселей
- 4 направления: DOWN (вниз), UP (вверх), LEFT (влево), RIGHT (вправо)
- 9 анимаций: IDLE, WALK, RUN, ATTACK, HURT, DIE, CAST_UP, CAST_SIDE, CAST_SELF
- Каждая анимация: 6–8 кадров по 64×64 пикселя

### Слоистая система

`LpcSpriteCompositor` накладывает слои изображений в фиксированном порядке снизу вверх:

```
1. SHADOW       — тень под персонажем
2. BODY         — тело (обязательный слой)
3. UNDERWEAR    — нижнее бельё / базовая одежда
4. LEGS         — штаны, юбка
5. TORSO        — рубашка, куртка
6. OUTERWEAR    — верхняя одежда
7. HAIR_BACK    — задняя часть волос (за персонажем)
8. HAIR_FRONT   — передняя часть волос
9. FACE         — выражение лица, эмоции
10. ACCESSORIES — шляпа, очки, украшения
```

### CharacterDefinition

Описывает набор слоёв для конкретного персонажа:

```java
CharacterDefinition tatyana = CharacterDefinition.builder()
    .name("tatyana")
    .layer(SpriteLayerCategory.BODY,       "body_female_light")
    .layer(SpriteLayerCategory.HAIR_BACK,  "hair_long_brown_back")
    .layer(SpriteLayerCategory.TORSO,      "shirt_casual_white")
    .layer(SpriteLayerCategory.LEGS,       "jeans_blue")
    .layer(SpriteLayerCategory.HAIR_FRONT, "hair_long_brown_front")
    .build();
```

### Добавление нового персонажа

1. Создать `CharacterDefinition` с набором слоёв.
2. Разместить PNG-файлы слоёв в `src/main/resources/sprites/layers/<категория>/`.
3. Зарегистрировать в `GeneratorRegistry`:
   ```java
   registry.register(AssetType.SPRITE, "tatyana",
       () -> compositor.compose(tatyanaDef, outputDir));
   ```
4. Добавить вызов в `AssetGenerationRunner`.

### Добавление новых анимаций

1. Добавить значение в `SpriteAction`:
   ```java
   SWIM(/* row offset */)
   ```
2. Добавить кадры анимации в файлы слоёв (строго соблюдать позиции LPC-формата).
3. Обновить `AnimationFrame` если требуются новые параметры кадра.

---

## Конфигурация

```yaml
# application.yml (модуль application или asset-generator standalone)
lifegame:
  assets:
    output-dir: ./generated-assets   # куда сохранять PNG
    textures:
      tile-size: 64                   # размер тайла в пикселях
      symmetry: QUAD                  # режим симметрии по умолчанию
    sprites:
      scale: 2                        # масштаб при сохранении (1 = 64px, 2 = 128px)
      generate-atlas: true            # создавать ли атлас всех спрайтов
```

---

## Запуск генерации

### Из приложения (рекомендуется)

```bash
java -jar life-of-t-application-0.1.0-SNAPSHOT.jar \
     --generate-assets \
     --spring.main.web-application-type=none
```

### Standalone (только asset-generator)

```bash
cd asset-generator
mvn spring-boot:run -Dspring-boot.run.profiles=generate-assets
```

### Из тестов

```java
@SpringBootTest(classes = AssetGeneratorApplication.class)
@ActiveProfiles("generate-assets")
class AssetGenerationIntegrationTest {
    @Autowired
    private GeneratorRegistry registry;

    @Test
    void allAssetsGenerate() {
        assertThatCode(() -> registry.generateAll()).doesNotThrowAnyException();
    }
}
```

---

## Выходные файлы

После генерации в `generated-assets/` появятся:

```
generated-assets/
├── textures/
│   ├── cozy_home_floor.png
│   ├── park_grass.png
│   ├── office_floor.png
│   └── ...
└── sprites/
    ├── tatyana/
    │   ├── tatyana_sheet.png    — полный лист спрайтов
    │   └── tatyana_idle_down_0.png, ...  — отдельные кадры (опционально)
    └── ...
```

Фронтенд загружает ассеты по предсказуемым именам через `AssetService`:
```typescript
const textureUrl = `/assets/textures/cozy_home_floor.png`;
const spriteUrl  = `/assets/sprites/tatyana/tatyana_sheet.png`;
```

---

## Тестирование

Тесты покрывают:
- `CellGridTest` — правильность создания и заполнения сетки
- `ProceduralTextureGeneratorTest` — генерация возвращает непустые PNG-данные
- `LpcSpriteCompositorTest` — корректная компоновка слоёв
- `CharacterDefinitionTest` — валидация обязательных слоёв

```bash
mvn test -pl asset-generator
```
