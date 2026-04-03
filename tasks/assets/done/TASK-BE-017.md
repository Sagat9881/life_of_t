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

Генератор ассетов (`ru.lifegame.assets`) должен читать XML-спецификации ассетов через манифесты блоков — аналогично TASK-BE-016 для нарративных спецификаций. Реализовать `AssetSpecParser` без привязки к конкретным именам файлов или ассетов.

**Промт для выполнения:** `docs/prompts/assets/PROMPT-BE-017.md`

---

## Входные артефакты

- `java-developer-skill.md` — §3.2, §5.2, §5.5
- `docs/ASSETS.md`
- `game-content/life-of-t/src/main/resources/narrative/asset_manifest.xml`
- TASK-BE-016 — архитектурный паттерн манифестов

---

## Выходные артефакты

- `ru.lifegame.assets.infrastructure.AssetSpecParser`
- `ru.lifegame.assets.domain.AssetSpec`
- `ru.lifegame.assets.domain.AssetSpec.Layer`
- `ru.lifegame.assets.domain.AssetSpec.Animation`
- `ru.lifegame.assets.domain.AssetSpec.AtlasConfig`
- Unit-тесты

---

## Критерии готовности (DoD)

- [ ] `AssetSpecParser` парсит XML по структуре из манифеста
- [ ] `AssetSpec` содержит: слои, анимации, атлас-конфиг, наследование, привязку поведения
- [ ] Никаких хардкоженных имён ассетов, слоёв, анимаций
- [ ] Добавление нового ассета не требует изменений парсера
- [ ] Unit-тесты пройдены

---

## Связи

- **Зависит от:** TASK-BE-016
- **Связано с:** `docs/ASSETS.md`
