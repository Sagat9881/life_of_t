# TASK-BE-015 — Реализация обобщённого `SpecLoader<T>` для нарративных спецификаций

## Метаданные

| Поле | Значение |
|---|---|
| **ID** | TASK-BE-015 |
| **Тип** | backend |
| **Роль** | Java Developer |
| **Приоритет** | HIGH |
| **Дата создания** | 30.03.2026 |
| **Статус** | TODO |
| **Промт** | `docs/prompts/backend/PROMPT-BE-015.md` |

---

## Описание

Текущая загрузка нарративных спецификаций содержит хардкоженные пути и switch-case по типам сущностей. Необходимо реализовать обобщённый `SpecLoader<T extends NarrativeSpec>`, который загружает любой тип спецификации через единый интерфейс без привязки к конкретным именам квестов, NPC или конфликтов.

**Промт для выполнения:** `docs/prompts/backend/PROMPT-BE-015.md`

---

## Входные артефакты

- `java-developer-skill.md` — §3.1 Нарративные спецификации, §5.1 Независимость от нарративных данных
- `docs/CONTENT_API.md` — контракт Content API
- `game-content/life-of-t/src/main/resources/narrative/` — структура существующих спецификаций
- Технические спецификации в `docs/` (CONTENT_API, EVENT_SYSTEM)

---

## Выходные артефакты

- `ru.lifegame.backend.infrastructure.spec.SpecLoader<T>` — обобщённый загрузчик
- `ru.lifegame.backend.domain.spec.NarrativeSpec` — базовый интерфейс/абстрактный класс
- `ru.lifegame.backend.domain.spec.SpecPath` — value-object для пути к спецификации
- Unit-тесты в `src/test/` покрывающие: загрузку QuestSpec, NpcSpec, обработку ошибок

---

## Технические требования

```java
// Целевой интерфейс:
public class SpecLoader<T extends NarrativeSpec> {
    public T load(SpecPath path, Class<T> type) { /* ... */ }
    public List<T> loadAll(SpecPath basePath, Class<T> type) { /* ... */ }
}
```

**Ограничения (из `java-developer-skill.md` §5.1):**
- Никаких `if/switch` по именам квестов, NPC, событий
- Никаких хардкоженных идентификаторов нарративных сущностей
- `SpecPath` — value object, не строковая константа
- Десериализация через конфигурируемый маппер (не хардкоженный)

---

## Критерии готовности (DoD)

- [ ] `SpecLoader<T>` реализован в пакете `infrastructure.spec`
- [ ] Работает для QuestSpec, NpcSpec, ConflictSpec без изменения кода загрузчика
- [ ] Нет хардкоженных путей и имён сущностей в коде загрузчика
- [ ] Unit-тесты пройдены (покрытие ≥ 80% для `SpecLoader`)
- [ ] Код прошёл ревью по чеклисту `java-developer-skill.md` §5

---

## Связи

- **Блокирует:** TASK-BE-016 (manifest-driven scan использует SpecLoader)
- **Зависит от:** ничего (базовая инфраструктура)
- **Связано с:** `docs/CONTENT_API.md`, `docs/EVENT_SYSTEM.md`

---

## Примечание аналитика

> Ключевая инфраструктурная задача. Без неё TASK-BE-016 и TASK-BE-017 не могут быть реализованы корректно.  
> Ссылка: `java-developer-skill.md` §3.1, §5.1, §5.5.
