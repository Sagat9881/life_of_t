# PROMPT-BE-DOC-002: Добавить sentinel-сигнал завершения генерации

> **Ветка:** `main` — все изменения только в `main`.
> **Движение задачи:**
> 1. Перед началом: переместить `tasks/backend/todo/TASK-BE-DOC-002.md` → `tasks/backend/in_progress/TASK-BE-DOC-002.md`.
> 2. После выполнения: переместить `tasks/backend/in_progress/TASK-BE-DOC-002.md` → `tasks/backend/done/TASK-BE-DOC-002.md`. Промт перенести в `docs/promts/backend/done/PROMPT-BE-DOC-002.md`.

---

## Роль и skill-файл

Ты — **Java Developer** проекта «Life of T». Строго следуй `java-developer-skill.md`.

Перед началом перечитай все разделы skill-файла (разделы 2, 5, 6, 7, 9, 10) и резюмируй обязанности и ограничения.

---

## Задача

**ID:** TASK-BE-DOC-002  
**Компонент:** `asset-generator/` + `.github/workflows/asset-generation.yml`  
**Приоритет:** Высокий  
**Зависимости:** TASK-BE-DOC-001 (частично)  
**Связанные спецификации:** `docs/specs/technical/ci-fix-asset-hardcode.md`  
**ADR:** `docs/decisions/ADR-001-visual-docs-data-independence.md`

## Контекст

CI использует `tanya_idle.png` как сигнал завершения генерации — это хардкод, нарушающий ADR-001. Требуется определить и реализовать data-driven сигнал завершения.

**Обязательно прочитай перед реализацией:**
- `docs/specs/technical/ci-fix-asset-hardcode.md` — техспек, раздел про sentinel
- `docs/decisions/ADR-001-visual-docs-data-independence.md`

## Цель

Заменить хардкод `tanya_idle.png` в CI-ожидании на data-driven sentinel-сигнал. Сигнал должен работать независимо от состава сущностей.

## Ответ по SDD

### 1. Specify

Определи: какой файл гарантированно создаётся **последним** при завершении генерации.

Кандидаты (согласно техспеку):
- `sprite-atlas.json` — если уже создаётся последним, задача в документировании контракта.
- `generation-complete.sentinel` — если нужен явный sentinel.

Порядок анализа:
1. Изучи текущий код генератора — в каком порядке создаются файлы.
2. Зафикси решение: либо документируй существующий контракт в `docs/decisions/`, либо добавь sentinel.

### 2. Plan

Если `sprite-atlas.json` — последний:
- Добавить комментарий/документ в `docs/decisions/` фиксирующий контракт.
- Обновить `asset-generation.yml`: заменить проверку `tanya_idle.png` на `sprite-atlas.json`.

Если нужен sentinel:
- Добавить в Infrastructure слой генератора (`ru.lifegame.assets.infrastructure.generator`) запись `generation-complete.sentinel` как последнего шага.
- Обновить `asset-generation.yml`.

### 3. Implement

**Критерии готовности (DoD):**
- [ ] В `asset-generation.yml` нет имени `tanya_idle.png` в шаге ожидания.
- [ ] Используемый сигнал задокументирован в комментарии или ADR.
- [ ] CI проходит при наличии нового персонажа без `tanya_idle.png`.

---

## Жёсткие ограничения

- ❌ Не хардкодить имена конкретных файлов ассетов в Java-коде.
- ❌ Не трогать `narrative/`, `tasks/`.
- ✅ Решение задокументировано — другие участники знают, что является сигналом завершения.
- ✅ Работа строго в ветке `main`.
