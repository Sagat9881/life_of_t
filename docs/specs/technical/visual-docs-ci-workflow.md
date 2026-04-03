# Техспек: CI workflow для сборки документационного сайта

| Поле | Значение |
|------|----------|
| **Путь** | `docs/specs/technical/visual-docs-ci-workflow.md` |
| **Компонент** | `.github/workflows/docs-site.yml` (новый файл) |
| **SDD-фаза** | Specify |
| **Дата** | 2026-04-03 |
| **Ответственный** | System Analyst |
| **Исполнители** | Java Developer (шаг генерации), JavaScript Developer (шаг сборки сайта) |
| **ADR** | [ADR-001](../decisions/ADR-001-visual-docs-data-independence.md) |
| **Задачи** | `tasks/backend/TASK-BE-DOC-002.md`, `tasks/frontend/TASK-FE-DOC-003.md` |

---

## 1. Контекст и цель

Документационный сайт должен собираться и публиковаться **автоматически** при каждом push в `master`,
без участия человека, строго соблюдая принцип data independence (ADR-001).

---

## 2. Структура workflow `docs-site.yml`

```yaml
name: Build & Deploy Documentation Site

on:
  push:
    branches: [master]
    paths:
      - 'game-content/**'
      - 'asset-generator/**'
      - 'docs-site/**'
      - '.github/workflows/docs-site.yml'
  workflow_dispatch:

jobs:
  generate-docs-preview:
    name: Generate docs-preview.json
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '21', distribution: temurin, cache: maven }
      - name: Build
        run: |
          mvn -B -N install --no-transfer-progress
          mvn -B install -DskipTests -f backend/pom.xml --no-transfer-progress
          mvn -B package -f application/pom.xml -DskipTests --no-transfer-progress
      - name: Generate docs-preview.json
        run: |
          java --enable-preview -jar application/target/life-of-t.jar \
            --assets.output-mode=docs-preview \
            --assets.output-dir=/tmp/docs-preview-output \
            --server.port=8099 &
          APP_PID=$!
          # Ожидание: появление docs-preview.json (data-driven, без хардкода имён)
          for i in $(seq 1 30); do
            if [ -f "/tmp/docs-preview-output/docs-preview.json" ]; then
              echo "docs-preview.json ready after ${i}s"; break
            fi
            sleep 1
          done
          kill $APP_PID 2>/dev/null || true
      - name: Validate docs-preview.json (data-driven)
        run: |
          python3 - <<'EOF'
          import json, xml.etree.ElementTree as ET, sys

          manifest = ET.parse(
            'game-content/life-of-t/src/main/resources/asset-specs/specs-manifest.xml'
          )
          expected_count = len([
            e for e in manifest.findall('entity')
            if e.get('abstract', 'false') == 'false'
          ])
          with open('/tmp/docs-preview-output/docs-preview.json') as f:
            data = json.load(f)
          actual_count = len(data)
          if actual_count != expected_count:
            print(f'FAIL: expected {expected_count} entities, got {actual_count}')
            sys.exit(1)
          print(f'OK: {actual_count} entities in docs-preview.json')
          EOF
      - uses: actions/upload-artifact@v4
        with:
          name: docs-preview-json
          path: /tmp/docs-preview-output/docs-preview.json
          retention-days: 7

  download-sprites:
    name: Download generated sprites
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      # Скачивает последний артефакт generated-pixel-art-assets из asset-generation workflow
      - name: Download sprites artifact
        uses: dawidd6/action-download-artifact@v6
        with:
          workflow: asset-generation.yml
          name: generated-pixel-art-assets
          path: /tmp/sprites
      - uses: actions/upload-artifact@v4
        with:
          name: sprites
          path: /tmp/sprites/
          retention-days: 7

  build-docs-site:
    name: Build static docs site
    runs-on: ubuntu-latest
    needs: [generate-docs-preview, download-sprites]
    steps:
      - uses: actions/checkout@v4
      - uses: actions/download-artifact@v4
        with: { name: docs-preview-json, path: docs-site/data }
      - uses: actions/download-artifact@v4
        with: { name: sprites, path: docs-site/assets }
      - name: Verify no hardcoded entity names in JS (data independence check)
        run: |
          # Читает имена сущностей из манифеста и проверяет их отсутствие в JS-коде
          python3 - <<'EOF'
          import xml.etree.ElementTree as ET, subprocess, sys
          manifest = ET.parse(
            'game-content/life-of-t/src/main/resources/asset-specs/specs-manifest.xml'
          )
          ids = [
            e.get('path', '').split('/')[-1]
            for e in manifest.findall('entity')
            if e.get('abstract', 'false') == 'false'
          ]
          violations = []
          for entity_id in ids:
            result = subprocess.run(
              ['grep', '-rn', entity_id, 'docs-site/js/'],
              capture_output=True, text=True
            )
            if result.stdout.strip():
              violations.append(f'{entity_id}: {result.stdout.strip()}')
          if violations:
            print('FAIL: hardcoded entity names in JS:')
            for v in violations: print(v)
            sys.exit(1)
          print(f'OK: no hardcoded entity names in JS ({len(ids)} checked)')
          EOF
      - uses: actions/upload-pages-artifact@v3
        with: { path: docs-site/ }

  deploy:
    name: Deploy to GitHub Pages
    runs-on: ubuntu-latest
    needs: build-docs-site
    permissions:
      pages: write
      id-token: write
    environment:
      name: github-pages
      url: ${{ steps.deployment.outputs.page_url }}
    steps:
      - id: deployment
        uses: actions/deploy-pages@v4
```

---

## 3. Принципы data independence в workflow

1. **Ожидание завершения генератора** — по наличию `docs-preview.json`, а не по имени конкретного PNG.
2. **Валидация** — считает сущности из `specs-manifest.xml` и сравнивает с JSON.
3. **Проверка JS на хардкод** — читает имена из манифеста и ищет их в `docs-site/js/`.
4. **Никаких статических списков** — все проверки генерируются из данных.

---

## 4. Метрики и критерии готовности

| Критерий | Измерение |
|----------|-----------|
| Workflow завершается успешно | GitHub Actions |
| `docs-preview.json` содержит верное кол-во записей | Python validation step |
| JS не содержит имён сущностей | grep step |
| Сайт опубликован на GitHub Pages | `deploy` job |
| При добавлении `<entity>` в манифест — сайт обновляется без изменений кода | Integration test |
