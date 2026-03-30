# TASK-BE-016 — Реализация NarrativeLoader: загрузка квестов из resources/narrative/

## Метаданные

| Поле | Значение |
|---|---|
| **ID** | TASK-BE-016 |
| **Тип** | [backend] |
| **Роль** | Java Developer |
| **Приоритет** | HIGH |
| **GAP-метрика** | TR-01 |
| **Дата создания** | 30.03.2026 |
| **Статус** | TODO |

---

## Описание

Имеющийся нарративный контент (15 квестов, 11 NPC, 10 конфликтов, 10 концовок) размещён в `game-content/life-of-t/src/main/resources/narrative/`, однако подтверждения наличия сервиса загрузки (`NarrativeLoader` или аналога) в DDD-структуре `ru.lifegame.backend` не получено.

Необходимо реализовать или верифицировать сервис загрузки нарративных спецификаций в Application-слое, обеспечив доступ к данным квестов, NPC, конфликтов и концовок через доменные агрегаты.

---

## Входные артефакты

- `java-developer-skill.md` — DDD-слои: Domain / Application / Infrastructure
- `game-content/life-of-t/src/main/resources/narrative/` — спецификации (YAML/JSON)
- `narrativeplans/content-plan.md` (TASK-NA-011) — порядок и приоритет загрузки
- `docs/specs/technical/backend-ddd-structure.md` (TASK-BE-015) — карта пакетов

---

## Выходные артефакты

- Класс `NarrativeLoader` (или аналог) в Application-слое `ru.lifegame.backend`
- Unit-тесты для `NarrativeLoader` (покрытие ≥ 80%)
- Обновлённый `docs/specs/technical/narrative-loader-spec.md`

### Минимальный контракт NarrativeLoader:
```
List<QuestSpec> loadQuests()
List<NpcSpec> loadNpcs()
List<ConflictSpec> loadConflicts()
List<EndingSpec> loadEndings()
```

---

## Критерии готовности (DoD)

- [ ] Класс `NarrativeLoader` существует в Application-слое
- [ ] Загружает все 4 типа контента (квесты, NPC, конфликты, концовки)
- [ ] Unit-тесты пройдены, покрытие ≥ 80%
- [ ] Файл спецификации `docs/specs/technical/narrative-loader-spec.md` создан
- [ ] TASK-BE-015 выполнена (DDD-структура подтверждена) — предусловие

---

## Зависимости

- **Зависит от**: TASK-BE-015 (DDD-карта), TASK-NA-011 (content-plan)
- **Блокирует**: API-эндпоинты выдачи квестов на фронт (tasks/frontend)

---

## Примечание аналитика

> Без подтверждённого NarrativeLoader невозможно строить API-контракты между бэком и фронтом. Задача является критическим path для интеграции нарративного контента.
> Ссылка на skill-файл: `system-analyst-skill.md` §6 Технические спецификации (контракты API), §9 Взаимодействие с Java Developer.
