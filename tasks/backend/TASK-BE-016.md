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

Текущий бэкенд сканирует директории `narrative/` напрямую, угадывая структуру по путям файловой системы. Это нарушает принцип независимости от структуры директорий (`java-developer-skill.md` §5.5).

Необходимо реализовать систему, где бэкенд сканирует только **манифесты** (`manifest.*`) и получает из них весь список сущностей блока и их пути.

**Промт для выполнения:** `docs/prompts/backend/PROMPT-BE-016.md`

---

## Входные артефакты

- `java-developer-skill.md` — §5.5 Независимость от структуры директорий, манифесты блоков
- `docs/CONTENT_API.md` — контракт загрузки контента
- `docs/MIGRATION.md` — план миграции
- `game-content/life-of-t/src/main/resources/narrative/asset_manifest.xml` — пример существующего манифеста
- TASK-BE-015 — **должна быть выполнена** (зависимость от `SpecLoader<T>`)

---

## Выходные артефакты

- `ru.lifegame.backend.infrastructure.spec.ManifestScanner` — сканер манифестов
- `ru.lifegame.backend.domain.spec.BlockManifest` — value-object манифеста блока
- Обновлённый `SpecLoader<T>` (интеграция с ManifestScanner)
- Манифест-файлы для блоков `quest`, `confilcts`, `npc`, `world-events` (если отсутствуют)
- Unit-тесты: сканирование, парсинг манифеста, получение списка сущностей

---

## Технические требования

```java
// Целевой интерфейс:
public class ManifestScanner {
    // Сканирует пакет на предмет манифестов, возвращает список BlockManifest
    public List<BlockManifest> scanPackage(Path packageRoot) { /* ... */ }
}

public record BlockManifest(
    String blockId,
    List<SpecEntry> entries  // каждый entry: тип + относительный путь к spec
) {}
```

**Ограничения (из §5.5):**
- Бэкенд не сканирует произвольно поддиректории
- Вся структура блока берётся только из манифеста
- Добавление нового блока не требует изменений в коде сканера

---

## Критерии готовности (DoD)

- [ ] `ManifestScanner` реализован, не содержит хардкоженных путей блоков
- [ ] `BlockManifest` описывает все сущности блока через SpecEntry
- [ ] Добавление нового блока в `narrative/` не требует изменений в коде (только новый манифест)
- [ ] Все 4 блока (`quest`, `confilcts`, `npc`, `world-events`) имеют манифесты
- [ ] Unit-тесты пройдены
- [ ] Код прошёл ревью по чеклисту `java-developer-skill.md` §5

---

## Связи

- **Блокирует:** TASK-BE-017 (AssetSpec scanner использует тот же принцип)
- **Зависит от:** TASK-BE-015
- **Связано с:** `docs/MIGRATION.md`, `docs/REFACTORING.md`

---

## Примечание аналитика

> Манифест-driven подход — ключевое архитектурное решение для масштабируемости нарратива.  
> Ссылка: `java-developer-skill.md` §5.5, §3.1.
