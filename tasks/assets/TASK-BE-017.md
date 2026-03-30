# TASK-BE-017 — Реализация manifest-driven XML-парсера ассет-спецификаций (`AssetSpecParser`)

## Метаданные

| Поле | Значение |
|---|---|
| **ID** | TASK-BE-017 |
| **Тип** | assets |
| **Роль** | Java Developer |
| **Приоритет** | MEDIUM |
| **Дата создания** | 30.03.2026 |
| **Статус** | TODO |
| **Промт** | `docs/prompts/assets/PROMPT-BE-017.md` |

---

## Описание

Генератор ассетов (`ru.lifegame.assets`) должен читать XML-спецификации ассетов (`assets/specs/`) через манифесты блоков — аналогично тому, как TASK-BE-016 решает эту задачу для нарративных спецификаций.

Требуется реализовать `AssetSpecParser`, который парсит XML-описания ассетов (слои, анимации, атласы, наследование, привязка поведения) без привязки к конкретным именам файлов или ассетов.

**Промт для выполнения:** `docs/prompts/assets/PROMPT-BE-017.md`

---

## Входные артефакты

- `java-developer-skill.md` — §3.2 Спецификации ассетов, §5.2 Независимость от конкретных ассетов, §5.5
- `docs/ASSETS.md` — описание структуры ассетов
- `game-content/life-of-t/src/main/resources/narrative/asset_manifest.xml` — пример существующего манифеста ассетов
- TASK-BE-016 — архитектурный паттерн манифестов (для переиспользования)

---

## Выходные артефакты

- `ru.lifegame.assets.infrastructure.AssetSpecParser` — XML-парсер ассет-спецификаций
- `ru.lifegame.assets.domain.AssetSpec` — доменная модель ассет-спецификации
- `ru.lifegame.assets.domain.AssetSpec.Layer` — слой ассета
- `ru.lifegame.assets.domain.AssetSpec.Animation` — анимация
- `ru.lifegame.assets.domain.AssetSpec.AtlasConfig` — конфигурация атласа
- Unit-тесты: парсинг слоёв, анимаций, наследования, привязки поведения

---

## Технические требования

```java
// Целевой интерфейс:
public class AssetSpecParser {
    // Парсит XML по пути из манифеста, возвращает доменную модель
    public AssetSpec parse(Path xmlPath) { /* ... */ }
}

public record AssetSpec(
    AssetId id,
    List<Layer> layers,
    List<Animation> animations,
    AtlasConfig atlasConfig,
    Optional<AssetId> inheritsFrom,  // наследование
    List<LayerBinding> bindings       // привязка поведения слоёв
) {}
```

**Ограничения (из `java-developer-skill.md` §5.2):**
- Никаких `switch-case` по именам анимаций, спрайтов, слоёв
- Никаких `enum` с конкретными ассетами
- Парсер работает с абстрактными `AssetId`, `AnimationId`, `LayerId`
- Добавление нового ассет-блока в `assets/specs/` не требует изменений в коде парсера

---

## Критерии готовности (DoD)

- [ ] `AssetSpecParser` парсит XML по структуре из манифеста
- [ ] `AssetSpec` содержит: слои, анимации, атлас-конфиг, наследование, привязку поведения
- [ ] Никаких хардкоженных имён ассетов, слоёв, анимаций в коде
- [ ] Добавление нового ассета в `assets/specs/` не требует изменений парсера
- [ ] Unit-тесты пройдены (парсинг, наследование, привязки)
- [ ] Код прошёл ревью по чеклисту `java-developer-skill.md` §5

---

## Связи

- **Зависит от:** TASK-BE-016 (паттерн манифестов)
- **Связано с:** `docs/ASSETS.md`, `docs/REFACTORING_PHASE2.md`

---

## Примечание аналитика

> AssetSpecParser — зеркало ManifestScanner для домена ассетов. Архитектурно они идентичны.  
> Ссылка: `java-developer-skill.md` §3.2, §5.2, §5.5.
