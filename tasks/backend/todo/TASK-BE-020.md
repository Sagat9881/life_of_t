# TASK-BE-020 — Скрипт сбора метрик Assets (счётчики PNG/JSON)

| Поле | Значение |
|------|----------|
| **ID** | TASK-BE-020 |
| **Тип** | backend |
| **Статус** | TODO |
| **Приоритет** | MEDIUM |
| **Роль** | Java Developer |
| **Эпик** | EPIC-DEVOPS-001 |
| **Связанная спека** | `docs/specs/PROJECT_METRICS_SPEC.md` §2.3, §2.4 |
| **Зависит от** | TASK-SA-010 |

## Описание

Написать скрипт `scripts/metrics/collect_assets.py` для метрик **Assets Coverage (AC)** и **Narrative Coverage (NC)**.

## Формат выходного JSON (assets)

```json
{
  "totalAtlases": 0, "atlasesWithMetadata": 0, "metadataCoveragePct": 0.0,
  "byCategory": { "npc": 0, "locations": 0, "furniture": 0, "pets": 0, "ui": 0 },
  "locationsComplete": 0, "npcComplete": 0
}
```

## Формат выходного JSON (narrative)

```json
{
  "quests": 0, "conflicts": 0, "npc": 0, "worldEvents": 0,
  "total": 0, "withManifest": 0, "manifestCoveragePct": 0.0
}
```

## Критерии приёмки

- [ ] Скрипт создан в `scripts/metrics/collect_assets.py`
- [ ] Все метрики AC-01..AC-05 и NC-01..NC-05 вычисляются корректно
- [ ] При отсутствии `generated-assets/` — возвращает нули
- [ ] При отсутствии `game-content/` — возвращает нули
