# TASK-BE-016 — Реализация manifest-driven сканирования нарративных блоков

## Метаданные

| Поле | Значение |
|---|---|
| **ID** | TASK-BE-016 |
| **Тип** | backend |
| **Роль** | Java Developer |
| **Приоритет** | HIGH |
| **Дата создания** | 30.03.2026 |
| **Статус** | TODO |
| **Промт** | `docs/prompts/backend/PROMPT-BE-016.md` |

---

## Описание

Текущий бэкенд сканирует директории `narrative/` напрямую, угадывая структуру по путям файловой системы. Необходимо реализовать систему, где бэкенд сканирует только **манифесты** (`manifest.*`) и получает из них весь список сущностей блока и их пути.

**Промт для выполнения:** `docs/prompts/backend/PROMPT-BE-016.md`

---

## Входные артефакты

- `java-developer-skill.md` — §5.5
- `docs/CONTENT_API.md`
- `docs/MIGRATION.md`
- `game-content/life-of-t/src/main/resources/narrative/asset_manifest.xml`
- TASK-BE-015 — зависимость от `SpecLoader<T>`

---

## Выходные артефакты

- `ru.lifegame.backend.infrastructure.spec.ManifestScanner`
- `ru.lifegame.backend.domain.spec.BlockManifest`
- Обновлённый `SpecLoader<T>` (интеграция с ManifestScanner)
- Манифест-файлы для блоков `quest`, `conflicts`, `npc`, `world-events`
- Unit-тесты

---

## Критерии готовности (DoD)

- [ ] `ManifestScanner` реализован, не содержит хардкоженных путей блоков
- [ ] `BlockManifest` описывает все сущности блока через SpecEntry
- [ ] Добавление нового блока не требует изменений в коде (только новый манифест)
- [ ] Все 4 блока имеют манифесты
- [ ] Unit-тесты пройдены
- [ ] Код прошёл ревью по чеклисту `java-developer-skill.md` §5

---

## Связи

- **Блокирует:** TASK-BE-017
- **Зависит от:** TASK-BE-015
