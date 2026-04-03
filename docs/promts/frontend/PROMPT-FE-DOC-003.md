# PROMPT-FE-DOC-003: Создать CI workflow `docs-site.yml`

> **Ветка:** `main` — все изменения только в `main`.
> **Движение задачи:**
> 1. Перед началом: переместить `tasks/frontend/todo/TASK-FE-DOC-003.md` → `tasks/frontend/in_progress/TASK-FE-DOC-003.md`.
> 2. После выполнения: переместить `tasks/frontend/in_progress/TASK-FE-DOC-003.md` → `tasks/frontend/done/TASK-FE-DOC-003.md`. Промт перенести в `docs/promts/frontend/done/PROMPT-FE-DOC-003.md`.

---

## Роль и skill-файл

Ты — **JavaScript Developer** проекта «Life of T». Строго следуй `javascript-developer-skill.md`.

Перед началом перечитай разделы 2, 3, 7, 8, 9 skill-файла. Резюмируй обязанности и ограничения.

---

## Задача

**ID:** TASK-FE-DOC-003  
**Компонент:** `.github/workflows/docs-site.yml` (новый файл)  
**Приоритет:** Высокий  
**Зависимости:** TASK-BE-DOC-001, TASK-BE-DOC-002, TASK-FE-DOC-001  
**Связанные спецификации:** `docs/specs/technical/visual-docs-ci-workflow.md`  
**ADR:** `docs/decisions/ADR-001-visual-docs-data-independence.md`

## Контекст

Создать `.github/workflows/docs-site.yml`: workflow генерирует `docs-preview.json`, скачивает PNG-спрайты, собирает `docs-site/` и публикует на GitHub Pages.

**Обязательно прочитай перед реализацией:**
- `docs/specs/technical/visual-docs-ci-workflow.md` — полная архитектура workflow
- `docs/decisions/ADR-001-visual-docs-data-independence.md`

## Цель

Рабочий CI/CD pipeline, полностью автоматизирующий сборку и публикацию docs-site. Шаг верификации хардкодов обязателен.

## Ответ по SDD

### 1. Архитектура workflow (из техспека)

Скопировать архитектурную схему из `docs/specs/technical/visual-docs-ci-workflow.md` в `.github/workflows/docs-site.yml`.

Обязательные шаги (строго по техспеку):
1. **Trigger**: `push` to `main`, `workflow_dispatch`
2. **Generate docs-preview.json** (запуск генератора с `--output-mode=docs-preview`) или переиспользование артефакта от `asset-generation.yml`
3. **Download sprite PNGs** (из CI-артефакта или GitHub Releases)
4. **Verify no hardcoded entity names in JS** — `grep -rn 'tanya\|sam\|aijan' docs-site/js/` должен вернуть 0
5. **Deploy to GitHub Pages**

### 2. Шаг верификации (обязателен)

```yaml
- name: Verify no hardcoded entity names in JS
  run: |
    RESULT=$(grep -rn 'tanya\|sam\|aijan\|alexander\|bed\|home_room' docs-site/js/ || true)
    if [ -n "$RESULT" ]; then
      echo "ERROR: Hardcoded entity names found in JS:"
      echo "$RESULT"
      exit 1
    fi
    echo "OK: No hardcoded entity names found"
```

### 3. GitHub Pages

- Настроить GitHub Pages для репозитория (branch `gh-pages` или `docs-site/` folder).
- Протестировать через `workflow_dispatch` перед финальным PR.

### 4. Implement

**Критерии готовности (DoD):**
- [ ] Workflow завершается успешно через `workflow_dispatch`.
- [ ] Сайт доступен по URL GitHub Pages.
- [ ] Шаг `Verify no hardcoded entity names` проходит.
- [ ] Добавление новой `<entity>` в манифест + push → сайт обновляется автоматически.

---

## Жёсткие ограничения

- ❌ Никаких хардкодов имён сущностей в YAML-шагах.
- ❌ Не трогать `narrative/`, `tasks/`, `src/`.
- ✅ Шаг верификации хардкодов обязателен.
- ✅ Работа строго в ветке `main`.
