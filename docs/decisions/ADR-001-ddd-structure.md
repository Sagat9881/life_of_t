# ADR-001 — DDD-структура проекта Life of T

## Мета

| Поле     | Значение                          |
|----------|-----------------------------------|
| ID       | ADR-001                           |
| Дата     | 2026-03-30                        |
| Статус   | Accepted                          |
| Автор    | Java Developer                    |
| Задачи   | TASK-BE-015, TASK-BE-016          |
| Ссылка на skill | `java-developer-skill.md` §7 (DDD + луковая архитектура) |

---

## Контекст

### Проблема

Игра «Life of T» — spec-driven lifesim: вся нарративная логика описана во внешних XML-спецификациях (квесты, NPC, конфликты, события мира). Бэкенд должен интерпретировать эти спецификации через единую систему абстракций, не зная имён конкретных персонажей или названий квестов (см. `java-developer-skill.md` §5.1 — независимость от нарративных данных).

Без явной DDD-структуры возникают следующие риски:
- Нарративная логика проникает в код через магические константы и `switch-case`-ветвления (антипаттерн §5.1).
- Размытые границы ответственности между слоями затрудняют тестирование domain-логики.
- Добавление нового NPC или квеста требует правок в коде, а не только в `narrative/`.

### Связь с narrativespecs

На момент аудита (2026-03-30) в репозитории присутствуют:
- **15 квестов** (`narrative/quests/*.xml`): aijan_integration, alexander_dinner, anxiety_attack, dacha_summer, duke_arrives, family_photo, feed_the_pride, find_persi, garfield_comfort, sam_trust, save_lada, separation, voland_visit, wedding, work_deadline.
- **11 NPC** (`narrative/npc-behavior/*.xml`): aijan, alexander, cirilla, duke, garfield, klop, lada, persi, sam, thelma, voland.
- **10 конфликтов** (`narrative/confilcts/*.xml`): burnout, father_concern, father_criticism, father_neglected, financial_disagreement, guilt, household_duties, identity_crisis, lack_of_attention, romantic_crisis.

Каждая из этих сущностей загружается через `SpecLoader` → соответствующий агрегат (`java-developer-skill.md` §3.1, поток загрузки QuestSpec).

---

## Решение

### Принятая архитектура: 4-слойный DDD (луковая модель)

```
Domain
  Application
    Infrastructure
      Presentation
```

Правило зависимостей (`java-developer-skill.md` §7):
- `domain` — без зависимостей наружу.
- `application` — зависит только от `domain`.
- `infrastructure` — реализует интерфейсы `domain`.
- `presentation` — зависит от `application`.

### Маппинг narrativespecs → DDD-агрегаты

| Spec              | Aggregate           | Repository              | DomainEvent           |
|-------------------|---------------------|-------------------------|-----------------------|
| `QuestSpec`       | `QuestAggregate`    | `QuestRepository`       | `QuestStarted`, `QuestCompleted` |
| `NpcSpec`         | `NpcAggregate`      | `NpcRepository`         | `NpcTriggered`, `NpcStateChanged` |
| `ConflictSpec`    | `ConflictAggregate` | `ConflictRepository`    | `ConflictStarted`, `ConflictResolved` |
| `WorldEventSpec`  | `WorldEventAggregate` | `WorldEventRepository` | `WorldEventFired`     |
| `AssetSpec` (XML) | `AssetAggregate`    | `AssetSpecRepository`   | `AssetGenerated`      |

Ожидаемые системные `enum`-типы (`java-developer-skill.md` §5.1, п.3):
- `SpecType` — тип спецификации (QUEST, NPC, CONFLICT, WORLD_EVENT, ASSET).
- `TriggerEventType` — тип триггера событий.
- `AssetId` (value object wrapper, не enum с конкретными ассетами) — §5.2.
- `AnimationId` (value object wrapper, не enum с конкретными ассетами) — §5.2.

---

## Аудит текущего состояния

### `ru.lifegame.backend`

> Источник: `backend/src/main/java/ru/lifegame/backend/` — аудит проведён 2026-03-30.
> Ссылка: `java-developer-skill.md` §6 (ограничения по директориям), §7 (структура пакетов).

#### Таблица аудита: DDD-слои

| DDD-слой         | Ожидается (по skill-файлу §7)                    | Факт (обнаруженные пакеты)                             | Статус |
|------------------|--------------------------------------------------|--------------------------------------------------------|--------|
| `domain/`        | model, event, repository, service, factory       | `action`, `balance`, `conflict`, `dto`, `ending`, `event`, `exception`, `model`, `narrative`, `npc`, `quest` | ⚠️ |
| `application/`   | usecase, port                                    | `command`, `controller`, `port`, `query`, `service`, `view` | ⚠️ |
| `infrastructure/`| spec, persistence, adapter                       | `concurrency`, `config`, `event`, `persistence`, `web` | ⚠️ |
| `presentation/`  | api                                              | **отсутствует** как отдельный пакет                    | ❌ |

#### Детализация: `domain/`

| Пакет               | Статус  | Комментарий |
|---------------------|---------|-------------|
| `domain/model/`     | ✅      | Присутствует; содержит `session/` (GameSession aggregate — Phase 2) |
| `domain/event/`     | ✅      | Присутствует |
| `domain/quest/`     | ✅      | Quest.java, QuestFactory.java, QuestLog.java, QuestProgressUpdater.java, QuestStepState.java, QuestObjective.java |
| `domain/npc/`       | ✅      | NpcSpecLoader.java, NpcUtilityBrain.java, runtime/, spec/ |
| `domain/conflict/`  | ✅      | Присутствует |
| `domain/narrative/` | ⚠️      | Нарративный подпакет в domain — не предусмотрен skill §7; SpecLoader должен быть в `infrastructure/spec/` |
| `domain/dto/`       | ⚠️      | DTO в domain — нарушение DDD: DTO принадлежат `application/` или `presentation/` |
| `domain/action/`    | ⚠️      | Требует проверки: если это domain entity/VO — OK; если application command — неверное размещение |
| `domain/balance/`   | ⚠️      | Баланс-константы как отдельный пакет в domain — допустимо только как ValueObject, не как конфиги |
| `domain/ending/`    | ⚠️      | Может быть частью `model/` (GameOverReason зафиксирован в REFACTORING_PHASE2.md) |
| `domain/exception/` | ⚠️      | Доменные исключения — допустимо в domain, но рекомендуется переместить в `domain/model/` |
| Repository-интерфейсы в domain | ✅ | GameSessionRepository.java зафиксирован в REFACTORING_PHASE2.md |
| Factory в domain    | ✅      | QuestFactory.java присутствует |

#### Детализация: `application/`

| Пакет                    | Статус | Комментарий |
|--------------------------|--------|-------------|
| `application/port/`      | ✅     | SessionRepository extends GameSessionRepository |
| `application/command/`   | ✅     | CQRS-команды — соответствует `usecase` |
| `application/query/`     | ✅     | CQRS-запросы — соответствует `usecase` |
| `application/service/`   | ✅     | Application services (ExecutePlayerActionService и др.) |
| `application/controller/`| ⚠️    | **Нарушение слоёв**: controller принадлежит `presentation/api/`, не `application/` |
| `application/view/`      | ⚠️    | **Нарушение слоёв**: view принадлежит `presentation/`, не `application/` |

#### Детализация: `infrastructure/`

| Пакет                        | Статус | Комментарий |
|------------------------------|--------|-------------|
| `infrastructure/persistence/`| ✅     | InMemorySessionRepository, SessionPersistence, FileSessionPersistence |
| `infrastructure/config/`     | ✅     | Конфигурация Spring — допустимо |
| `infrastructure/event/`      | ✅     | Реализация event-publishing |
| `infrastructure/concurrency/`| ⚠️    | Утилиты конкурентности — уточнить: если это adapter — переименовать |
| `infrastructure/web/`        | ⚠️    | Web-адаптеры: допустимо как `infrastructure/adapter/web/`, но лучше вынести в `presentation/` |
| `infrastructure/spec/`       | ❌     | **Отсутствует**: SpecLoader/NpcSpecLoader находится в `domain/npc/` — нарушение; spec-загрузчики должны быть в `infrastructure/spec/` |
| `infrastructure/adapter/`    | ❌     | Отсутствует явно — требуется для внешних адаптеров |

#### Детализация: `presentation/`

| Пакет              | Статус | Комментарий |
|--------------------|--------|-------------|
| `presentation/api/`| ❌     | **Отсутствует** как пакет; REST-контроллеры находятся в `application/controller/` — критическое нарушение DDD |

### `ru.lifegame.assets`

> Модуль `ru.lifegame.assets` в `backend/src/main/java/` не обнаружен как отдельный пакет.
> Ожидается по `java-developer-skill.md` §7: `domain/`, `application/`, `infrastructure/`, `presentation/cli`.

| DDD-слой              | Ожидается          | Факт       | Статус |
|-----------------------|--------------------|------------|--------|
| `assets/domain/`      | model, service     | ❌ нет     | ❌     |
| `assets/application/` | usecase            | ❌ нет     | ❌     |
| `assets/infrastructure/` | spec, generator | ❌ нет  | ❌     |
| `assets/presentation/`| cli                | ❌ нет     | ❌     |

> ⚠️ **Критическое отклонение**: модуль `ru.lifegame.assets` не выделен. Логика генерации ассетов, по всей видимости, встроена в `ru.lifegame.backend`. Требуется выделение в отдельный модуль.

### Статус REFACTORING_PHASE2.md

**Статус: Завершён (Phase 2 закрыта).**

Дата документа: 24 февраля 2026. Phase 2 содержит:
- ✅ GameSession Aggregate выделен в `domain/model/session/`
- ✅ GameSessionRepository перемещён в domain (Dependency Inversion)
- ✅ SessionPersistence выделен отдельно от Repository
- ✅ GameTime конвертирован в immutable record с валидацией
- ✅ Все application services обновлены
- ✅ 8 инвариантов + 3 value objects с валидацией
- ✅ Обратная совместимость сохранена

**Незавершённые пункты Phase 2 (статус TODO в документе):**
- ⬜ PlayerCharacter Aggregate (`domain/model/character/`)
- ⬜ Relationships Aggregate (`domain/model/relationship/`)
- ⬜ Pets Aggregate (`domain/model/pet/`)

Фазы 3 (остальные агрегаты), 4 (Domain Events) и 5 (CQRS) запланированы, но не начаты.

### Критические отклонения

1. **❌ `presentation/` отсутствует** — контроллеры размещены в `application/controller/`. Нарушение луковой архитектуры (`java-developer-skill.md` §7).
2. **❌ `infrastructure/spec/` отсутствует** — `NpcSpecLoader` находится в `domain/npc/`, что нарушает принцип: domain не должен содержать инфраструктурные загрузчики.
3. **❌ `ru.lifegame.assets` отсутствует** — генератор ассетов не выделен в отдельный DDD-модуль.
4. **⚠️ `domain/dto/`** — DTO в domain нарушают DDD; принадлежат `application/` или `presentation/`.
5. **⚠️ `application/view/`** — view принадлежит `presentation/`, не `application/`.
6. **⚠️ NpcSpecLoader в domain** — SpecLoader — инфраструктурный компонент; потенциальное нарушение §5.1 если содержит зависимость от конкретного формата файла.

> Проверка на антипаттерны §5 (switch-case / if-else по типам NPC или квестов) требует отдельного code review задачей TASK-BE-017.

---

## Последствия

### Что упрощается при соблюдении DDD

- Добавление нового NPC, квеста или конфликта требует только размещения XML-файла в `narrative/` — код не меняется.
- Domain-логика тестируется без Spring-контекста (чистые unit-тесты агрегатов).
- Слои можно заменять независимо (например, `FileSessionPersistence` → DB-реализация без изменения domain).
- Чёткие границы агрегатов делают транзакционный контроль предсказуемым.

### Риски при отклонении от структуры

- Размытие слоёв приводит к цикличным зависимостям и невозможности тестировать domain без инфраструктуры.
- SpecLoader в domain создаёт зависимость domain от формата файлов — нарушение изоляции.
- Отсутствие `presentation/` как слоя размывает ответственность за HTTP-адаптеры.

### Связанные решения

- [REFACTORING.md](../REFACTORING.md) — Phase 1: базовая структура пакетов.
- [REFACTORING_PHASE2.md](../REFACTORING_PHASE2.md) — Phase 2: GameSession Aggregate, Repository Pattern, Value Objects.
- [MIGRATION.md](../MIGRATION.md) — миграции БД.

---

## Следующие шаги

Задачи для доведения до целевой DDD-структуры:

| ID           | Описание                                                                                     | Приоритет |
|--------------|----------------------------------------------------------------------------------------------|-----------|
| TASK-BE-017  | Переместить REST-контроллеры из `application/controller/` в `presentation/api/`              | Высокий   |
| TASK-BE-018  | Переместить `NpcSpecLoader` из `domain/npc/` в `infrastructure/spec/`                       | Высокий   |
| TASK-BE-019  | Вынести `domain/dto/` в `application/` или `presentation/`                                  | Средний   |
| TASK-BE-020  | Переместить `application/view/` в `presentation/`                                           | Средний   |
| TASK-BE-021  | Выделить `ru.lifegame.assets` как отдельный DDD-модуль (domain/app/infra/cli)               | Высокий   |
| TASK-BE-022  | Создать `infrastructure/adapter/` для внешних адаптеров (web, concurrency)                  | Средний   |
| TASK-BE-023  | Реализовать PlayerCharacter, Relationships, Pets агрегаты (Phase 3 REFACTORING)             | Средний   |
| TASK-BE-024  | Code review на наличие switch-case/if-else по типам NPC/Quest (антипаттерн §5.1)            | Высокий   |
| TASK-BE-025  | Ввести `SpecType`, `TriggerEventType` enum; обернуть AssetId/AnimationId в Value Object     | Средний   |
| TASK-BE-026  | Проверить `domain/narrative/` — переместить SpecLoader-логику в `infrastructure/spec/`      | Средний   |
