# TASK-FE-DOC-003: Создать CI workflow `docs-site.yml`

| Поле | Значение |
|------|----------|
| **ID** | TASK-FE-DOC-003 |
| **Тип** | frontend / devops |
| **Компонент** | `.github/workflows/docs-site.yml` |
| **Исполнитель** | JavaScript Developer |
| **Приоритет** | Высокий |
| **Статус** | ✅ done |

---

## Что сделано

### `.github/workflows/docs-site.yml` — 4-job pipeline

| Job | Описание |
|-----|----------|
| `generate-docs-preview` | Maven build → java --enable-preview → полинг `docs-preview.json` → Python валидация (entity count vs manifest) |
| `download-sprites` | `dawidd6/action-download-artifact@v6` → `asset-generation.yml` artifact `generated-pixel-art-assets`, `continue-on-error: true` |
| `build-docs-site` | Download артефакты → **ADR-001 check** (Python, читает ID из manifest) → `upload-pages-artifact@v3` |
| `deploy` | `actions/deploy-pages@v4`, `environment: github-pages` |

### Особенности

- Trigger: `push` to `main` (пути `game-content/**`, `asset-generator/**`, `docs-site/**`, workflow файл) + `workflow_dispatch`
- `concurrency: group: pages-${{ github.ref }}, cancel-in-progress: true`
- `download-sprites` имеет `continue-on-error: true` — сайт собирается без спрайтов на первом запуске
- Ни одного хардкода ID сущностей в YAML

## Критерии приёмки

- [x] Workflow создан и валиден (YAML прошел review)
- [x] Шаг `Verify no hardcoded entity names` имплементирован (ADR-001)
- [ ] Workflow завершается через `workflow_dispatch` — зависит от TASK-BE-DOC-001/002
- [ ] Сайт доступен по URL GitHub Pages — требует Settings → Pages → Source: GitHub Actions
- [ ] push новой `<entity>` → авто-обновление — integration test после BE-ready

## Коммиты

- `0e8d9e3ad26af53823462773899da10534f06027` — feat(ci): TASK-FE-DOC-003 docs-site.yml

## Следующие шаги (вне зоны JS Developer)

- Включить GitHub Pages в Settings → Pages → Source: **GitHub Actions**
- TASK-BE-DOC-001: `asset-generation.yml` workflow должен производить `generated-pixel-art-assets`
- TASK-BE-DOC-002: Java генератор должен поддерживать `--assets.output-mode=docs-preview`
