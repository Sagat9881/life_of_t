# TASK-BE-017 — Спецификация ассет-пайплайна: AssetSpec для NPC и квестов

## Метаданные

| Поле | Значение |
|---|---|
| **ID** | TASK-BE-017 |
| **Тип** | [assets] |
| **Роль** | Java Developer |
| **Приоритет** | MEDIUM |
| **GAP-метрика** | TR-01 / AC-04 |
| **Дата создания** | 30.03.2026 |
| **Статус** | TODO |

---

## Описание

Для 11 NPC и 15 квестов, описанных в нарративных спецификациях, отсутствует формализованная ассет-спецификация (`AssetSpec`). Без неё ассет-пайплайн не знает, какие графические и аудио-ресурсы необходимы для каждого персонажа и сцены, что блокирует генерацию и валидацию ассетов.

Необходимо создать `AssetSpec` для NPC и спецификацию ассет-дескрипторов для ключевых квестовых сцен.

---

## Входные артефакты

- `java-developer-skill.md` — структура `AssetSpec` и ассет-пайплайн
- `game-content/life-of-t/src/main/resources/narrative/` — NPC и квестовые спецификации
- `narrativeworld/tatyana-facts.md` (TASK-NA-010) — канонические детали внешности
- `docs/specs/technical/backend-ddd-structure.md` (TASK-BE-015)

---

## Выходные артефакты

- `assets/specs/npc-asset-spec.md` — спецификация ассетов для 11 NPC
- `assets/specs/quest-scene-asset-spec.md` — ассеты для ключевых квестовых сцен
- `docs/specs/technical/asset-pipeline-spec.md` — технический контракт пайплайна

### Обязательные поля AssetSpec для NPC:
```yaml
npc_id: string          # идентификатор NPC
sprite_variants: []     # список вариантов спрайтов (настроения/ракурсы)
audio_ids: []           # звуковые реплики
scene_backgrounds: []   # фоны сцен с этим NPC
resolution: string      # требуемое разрешение
format: string          # PNG/WebP и т.п.
```

---

## Критерии готовности (DoD)

- [ ] Файл `assets/specs/npc-asset-spec.md` создан и содержит записи для всех 11 NPC
- [ ] Файл `assets/specs/quest-scene-asset-spec.md` создан
- [ ] Файл `docs/specs/technical/asset-pipeline-spec.md` описывает контракт пайплайна
- [ ] AssetSpec согласован с `narrativeworld/tatyana-facts.md` (TASK-NA-010)
- [ ] System Analyst согласовал спецификацию перед передачей в пайплайн

---

## Зависимости

- **Зависит от**: TASK-BE-015 (DDD-карта), TASK-NA-010 (tatyana-facts)
- **Блокирует**: генерацию ассетов и задачи tasks/assets/ следующего цикла

---

## Примечание аналитика

> Формализация AssetSpec на этом этапе предотвращает накопление «безымянных» ассетов без привязки к нарративным спецификациям. Без этого документа ассет-пайплайн будет работать вслепую.
> Ссылка на skill-файл: `system-analyst-skill.md` §3 Ограничения (п.3 синхронизация ассетов), §2 п.2 Технические спецификации.
