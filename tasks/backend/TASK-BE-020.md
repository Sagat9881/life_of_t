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

Написать скрипт `scripts/metrics/collect_assets.py`, который обходит директорию сгенерированных ассетов и директорию нарративного контента, собирая метрики **Assets Coverage (AC)** и **Narrative Coverage (NC)**.

## Требования к скрипту

### Assets Coverage (AC):
1. AC-01: `COUNT(*.png)` рекурсивно в `asset-generator/target/generated-assets/`
2. AC-02: `COUNT(sprite-atlas.json)` рекурсивно
3. AC-03: `COUNT(*.png)` по поддиректориям `npc/`, `locations/`, `furniture/`, `pets/`, `ui/`
4. AC-04: `COUNT(dirs в locations/ где есть idle.png И хотя бы один variant_*.png)`
5. AC-05: `COUNT(dirs в npc/ где *.png файлы совпадают с ключами в sprite-atlas.json)`

### Narrative Coverage (NC):
1. NC-01..NC-04: `COUNT(*.xml)` в `game-content/quest/`, `conflicts/`, `npc/`, `world-events/`
2. NC-05: `COUNT(manifest.xml)` рекурсивно в `game-content/`

3. Записывать результат в `docs/status/assets_metrics.json` и `docs/status/narrative_metrics.json`

## Формат выходного JSON (assets)

```json
{
  "totalAtlases": 0,
  "atlasesWithMetadata": 0,
  "metadataCoveragePct": 0.0,
  "byCategory": { "npc": 0, "locations": 0, "furniture": 0, "pets": 0, "ui": 0 },
  "locationsComplete": 0,
  "npcComplete": 0
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
- [ ] При отсутствии директории `generated-assets/` — возвращает нули без ошибки
- [ ] При отсутствии директории `game-content/` — возвращает нули без ошибки
- [ ] Оба JSON-файла соответствуют схеме из `PROJECT_METRICS_SPEC.md §4`

## Метрики

- AC-01, AC-02, AC-03, AC-04, AC-05
- NC-01, NC-02, NC-03, NC-04, NC-05
