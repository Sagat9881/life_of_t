# PROMPT-BE-017 — Реализация `AssetSpecParser` (manifest-driven XML)

> **Роль:** Java Developer  
> **Skill-файл:** `java-developer-skill.md`  
> **Задача:** `tasks/assets/TASK-BE-017.md`

---

## Инструкция для Java Developer

Ты — **Java Developer** проекта «Life of T». Строго следуй `java-developer-skill.md`.

**Перед началом:**
1. Перечитай `java-developer-skill.md` §3.2 «Спецификации ассетов», §5.2 «Независимость от конкретных ассетов», §5.5.
2. Убедись, что TASK-BE-016 выполнена (паттерн манифестов понят и реализован).
3. Прочитай `docs/ASSETS.md` и `game-content/.../narrative/asset_manifest.xml`.

---

## Задача

Реализуй `AssetSpecParser` — manifest-driven XML-парсер ассет-спецификаций — в пакете `ru.lifegame.assets.infrastructure`.

---

## Контекст

- Генератор ассетов работает с XML-спецификациями из `assets/specs/`.
- XML описывает: слои (layers), анимации, конфигурации атласов, наследование, привязку поведения слоёв.
- Парсер должен работать с **любым** ассетом из манифеста без изменений в коде.

---

## Что реализовать

### Доменная модель `ru.lifegame.assets.domain`
```java
public record AssetId(String value) {}
public record AnimationId(String value) {}
public record LayerId(String value) {}

public record Layer(LayerId id, String type, Map<String, String> params) {}
public record Animation(AnimationId id, LayerId targetLayer, List<String> frames,
                        boolean looping, float speed) {}
public record AtlasConfig(String atlasPath, int tileWidth, int tileHeight) {}
public record LayerBinding(LayerId source, LayerId target, String bindingType) {}

public record AssetSpec(
    AssetId id,
    List<Layer> layers,
    List<Animation> animations,
    AtlasConfig atlasConfig,
    Optional<AssetId> inheritsFrom,
    List<LayerBinding> bindings
) {}
```

### Инфраструктура `ru.lifegame.assets.infrastructure`
```java
public class AssetSpecParser {
    // Парсит XML по пути из манифеста ассетов
    // НЕ знает конкретных имён ассетов — только структуру XML
    public AssetSpec parse(Path xmlPath) { ... }
}
```

### Unit-тесты
- Парсинг XML с 2 слоями → `AssetSpec.layers().size() == 2`
- Парсинг XML с наследованием → `AssetSpec.inheritsFrom().isPresent() == true`
- Парсинг XML с привязкой слоёв → `AssetSpec.bindings()` не пустой
- Невалидный XML → типизированное исключение

---

## Ответ по SDD + DDD

- **SDD Specify** — доменные value objects (AssetId, AnimationId, LayerId, AssetSpec); ссылка на §3.2.
- **SDD Plan** — список классов по DDD-слоям: domain / infrastructure.
- **SDD Task** — подтверждение TASK-BE-017.
- **SDD Implement** — полный код AssetSpecParser + доменная модель + тесты + пример XML-спеки.

---

## Ограничения (java-developer-skill.md §5.2)

- Запрещены: `switch-case` по именам анимаций, `enum` с конкретными ассетами
- Парсер работает только с абстрактными ID (AssetId, AnimationId, LayerId)
- Добавление нового ассета = только новый XML-файл, без изменений в коде парсера
- Каждый шаг обосновывай ссылкой на раздел `java-developer-skill.md`
