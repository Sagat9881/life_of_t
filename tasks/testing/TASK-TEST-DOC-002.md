# TASK-TEST-DOC-002: CI-шаг `validate-catalog` — динамическая проверка полноты каталога

## Метаданные

| Поле | Значение |
|---|---|
| **ID** | TASK-TEST-DOC-002 |
| **Тип** | testing |
| **Статус** | TODO |
| **Дата создания** | 2026-04-03 |
| **Роль-исполнитель** | Тестировщик-автоматизатор / DevOps |

---

## Контекст

В новом workflow `docs-site.yml` необходим шаг `validate-catalog`, гарантирующий, что сгенерированный `site/data/catalog.json` содержит записи для **всех** не-abstract сущностей из `specs-manifest.xml`. Без этого шага возможна ситуация «тихого выпадения» сущности из документационного сайта: сущность есть в манифесте, ассет сгенерирован, но в каталог она не попала.

Полное описание шага и его места в workflow — в `docs/specs/technical/visual-docs-ci-workflow.md`.

---

## Связанные спецификации

- [`docs/specs/technical/visual-docs-ci-workflow.md`](../../docs/specs/technical/visual-docs-ci-workflow.md) — описание шага `validate-catalog` и его места в пайплайне `docs-site.yml`
- [`docs/decisions/ADR-001-visual-docs-data-independence.md`](../../docs/decisions/ADR-001-visual-docs-data-independence.md) — принцип, которому должен следовать шаг
- [`docs/specs/technical/visual-docs-site-structure.md`](../../docs/specs/technical/visual-docs-site-structure.md) — формат `catalog.json` и поле `entityId`

---

## Что нужно сделать

1. **Реализовать скрипт (bash или python) или inline-блок в YAML** для шага `validate-catalog`:

   a. Прочитать `specs-manifest.xml` → извлечь список `entityId` всех сущностей, у которых `abstract != true`.

   b. Прочитать `site/data/catalog.json` → извлечь список `entityId` из всех записей каталога.

   c. Проверить, что список из пункта (a) является **подмножеством** списка из пункта (b).

   d. При несоответствии — **вывести список недостающих `entityId`** и завершиться с кодом выхода `1`.

2. **Шаг не должен содержать конкретных имён сущностей** — только динамическое чтение из манифеста и каталога.

3. **Встроить шаг в `docs-site.yml`** после шага `collect-manifest` (согласно `visual-docs-ci-workflow.md`).

---

## Критерии приёмки

- [ ] При наличии не-abstract сущности в `specs-manifest.xml` без записи в `catalog.json` — CI завершается с кодом 1 и выводит список недостающих `entityId`.
- [ ] При полном соответствии манифеста и каталога — CI проходит успешно (код 0).
- [ ] Добавление новой сущности в манифест + успешная генерация превью → шаг `validate-catalog` проходит автоматически, без изменения YAML.
- [ ] В коде шага отсутствует хардкод каких-либо `entityId`.

---

## Метрики

- Количество сущностей, «тихо» выпавших из документационного сайта: 0 (гарантируется шагом).
- Шаг выполняется за время, не превышающее допустимое для CI-стадии `validate` (см. `docs/metrics/`).

---

## Примечание системного аналитика

Задача решает единственную проблему: гарантию полноты `catalog.json` относительно манифеста. Исполнитель **не должен реализовывать генерацию каталога** — только шаг его проверки. Алгоритм встройки шага в `docs-site.yml` описан в `visual-docs-ci-workflow.md`; формат `catalog.json` — в `visual-docs-site-structure.md`.
