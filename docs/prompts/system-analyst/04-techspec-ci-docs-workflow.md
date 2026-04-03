# Промт 04 — Техспека: CI-workflow для docs-site

## Роль и skill-файл

Роль: **System Analyst**  
Skill-файл: `system-analyst-skill.md`

---

## Инструкция перед началом

Перечитай `system-analyst-skill.md` (разделы 1, 2, 3, 5, 6). Прочитай:
- `.github/workflows/asset-generation.yml` — текущий пайплайн генерации;
- `.github/workflows/asset-generator-ci.yml` — CI с валидацией спек;
- `docs/decisions/ADR-001-visual-docs-data-independence.md` — принцип независимости;
- `docs/specs/technical/visual-docs-preview-mode.md` — что генерирует docs-preview mode;
- `docs/specs/technical/visual-docs-site-structure.md` — структура сайта и форматы данных.

Резюмируй обязанности и ограничения, затем выполняй.

---

## Задача

Создай техническую спецификацию в `docs/specs/technical/visual-docs-ci-workflow.md`.

### Контекст

Необходим новый GitHub Actions workflow `.github/workflows/docs-site.yml`, который:
1. Запускается при изменении XML-спек, документации или `scripts/`.
2. Запускает генератор в режиме `docs-preview`.
3. Запускает bash-скрипты для сборки `catalog.json` и `docs.json`.
4. Собирает статический сайт.
5. Деплоит на GitHub Pages.

Все шаги должны быть **data-driven**: никакого хардкода имён ассетов или сущностей в YAML.

### Что должно быть в техспеке

1. **Триггеры workflow** (пути, при изменении которых запускается):
   - `game-content/**/asset-specs/**` — изменение XML-спек;
   - `docs/visual-specs/**` — изменение документационных спек;
   - `scripts/**` — изменение bash-скриптов;
   - `site/src/**` — изменение исходников сайта;
   - Ручной запуск (`workflow_dispatch`).

2. **Шаги workflow** (описать каждый шаг: название, команда/действие, входы, выходы, условие провала):

   | Шаг | Описание | Вход | Выход |
   |-----|----------|------|-------|
   | checkout | `actions/checkout@v4` | — | код репо |
   | setup-java | Java 21 Temurin | — | JDK |
   | build-generator | `mvn -B install -DskipTests -f asset-generator/pom.xml` | pom.xml | generator JAR |
   | generate-docs-preview | `mvn ... -Doutput.mode=docs-preview` | specs-manifest.xml + visual-specs.xml | `target/docs-preview/` |
   | collect-manifest | `bash scripts/collect-manifest.sh` | specs-manifest.xml + card-meta.json | `site/data/catalog.json` |
   | collect-docs | `bash scripts/collect-docs.sh` | `docs/*.md` | `site/data/docs.json` |
   | validate-catalog | проверить, что catalog.json содержит записи для всех не-abstract сущностей манифеста | catalog.json + specs-manifest.xml | pass/fail |
   | build-site | `npm run build` в `site/` | src/ + data/ | `site/dist/` |
   | deploy-pages | `actions/deploy-pages` | `site/dist/` | GitHub Pages URL |

3. **Спецификация `scripts/collect-manifest.sh`**:
   - Входы: путь к `specs-manifest.xml`, путь к директории `docs-preview/`.
   - Выход: `site/data/catalog.json` формата, описанного в `visual-docs-site-structure.md`.
   - Алгоритм (словами): распарсить XML → для каждой не-abstract сущности → прочитать её `card-meta.json` → собрать в массив → записать JSON.
   - Запрещено: хардкод имён сущностей; использование путей кроме тех, что переданы параметрами.

4. **Спецификация `scripts/collect-docs.sh`**:
   - Входы: путь к `docs/`.
   - Выход: `site/data/docs.json` — список разделов документации (title из первого H1 файла, slug из имени файла, contentPath).
   - Алгоритм: `find docs/ -name '*.md'` → для каждого файла извлечь первый заголовок → собрать JSON-массив.

5. **Проверка корректности (validate-catalog)**:
   - Скрипт динамически читает `specs-manifest.xml`, получает список не-abstract сущностей, проверяет, что для каждой есть запись в `catalog.json`.
   - Никакого хардкода ожидаемых имён файлов (см. ADR-001).

6. **Связанные артефакты**: ADR-001, `visual-docs-preview-mode.md`, `visual-docs-site-structure.md`, оба существующих workflow.
7. **Метрики**: workflow завершается менее чем за 10 минут; валидация проходит при добавлении новой сущности без изменения YAML.
8. **Задачи**: TASK-AS-DOC-001 (bash-скрипты), TASK-TEST-DOC-001 (исправление хардкода в CI), TASK-TEST-DOC-002 (новый validate-catalog шаг).

### Критерии готовности

- [ ] Спека создана в `docs/specs/technical/visual-docs-ci-workflow.md`.
- [ ] Каждый шаг workflow описан (название, команда, входы, выходы).
- [ ] Обе спецификации bash-скриптов описаны алгоритмически.
- [ ] Явно запрещён хардкод сущностей в YAML согласно ADR-001.
- [ ] Описан шаг динамической валидации каталога.

> **Ограничение:** не пиши bash-код или YAML напрямую. Только спецификации алгоритмов и контракты.
