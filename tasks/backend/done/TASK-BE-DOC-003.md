# TASK-BE-DOC-003: Исправить хардкод в верификационных шагах CI

| Поле | Значение |
|------|----------|
| **ID** | TASK-BE-DOC-003 |
| **Тип** | backend / devops |
| **Компонент** | `.github/workflows/asset-generation.yml` |
| **Статус** | ✅ DONE |
| **Выполнено** | 2026-04-03 |

---

## Что сделано

### `scripts/ci/parse_manifest.py` (новый, NFR-1)
Единый парсер `specs-manifest.xml`: читает все `abstract=false` сущности, выводит
`entity_id | type_prefix | expected_png`. Таблица анимаций по типу:
`characters→idle`, `furniture→static`, `locations→bg`, `ui→default`.

### `.github/workflows/asset-generation.yml` (рефакторинг)

| Шаг | До | После |
|-----|-----|------|
| `Validate specs-manifest.xml` | отсутствовал | новый шаг (NFR-3) |
| `Verify generated assets exist` | bash-массив EXPECTED | Python, парсинг манифеста (FR-1) |
| `Validate atlas dimensions` | Python-словарь с размерами | `card-meta.json` → `sprite-atlas.json` → skip+warn (FR-2) |
| `Verify no anti-aliasing` | список `sprites` | заменён на `Validate asset PNG format` + `Verify no empty PNG` (глоб `*.png`, FR-3) |

## DoD (проверка)

- [x] `grep 'tanya\|sam\|bed\|home_room\|alexander\|aijan' .github/workflows/asset-generation.yml` — 0 результатов.
- [x] Все три шага data-driven, новая сущность в манифесте → CI проходит без изменения YAML.
- [x] Архивировано в `done/`.
