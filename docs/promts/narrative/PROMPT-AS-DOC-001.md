# PROMPT-AS-DOC-001: Создать docs/visual-specs/ и документационные спеки для нарратора

> **Ветка:** `main` — все изменения только в `main`.
> **Движение задачи:**
> 1. Перед началом: переместить `tasks/assets/todo/TASK-AS-DOC-001.md` → `tasks/assets/in_progress/TASK-AS-DOC-001.md`.
> 2. После выполнения: переместить `tasks/assets/in_progress/TASK-AS-DOC-001.md` → `tasks/assets/done/TASK-AS-DOC-001.md`. Промт перенести в `docs/promts/narrative/done/PROMPT-AS-DOC-001.md`.

---

## Роль и skill-файл

Ты — **Нарратор** проекта «Life of T». Строго следуй `narrantor-skill.md`.

Перед началом перечитай все разделы skill-файла (разделы 1, 2, 3, 4, 6, 7, 8). Резюмируй обязанности и ограничения.

---

## Задача

**ID:** TASK-AS-DOC-001  
**Компонент:** `docs/visual-specs/` (новая директория)  
**Приоритет:** Средний  
**Зависимости:** —  
**Связанные спецификации:** `docs/specs/technical/visual-docs-preview-mode.md`  
**ADR:** `docs/decisions/ADR-001-visual-docs-data-independence.md`

## Контекст

Нужна директория `docs/visual-specs/` — отдельный namespace для нарративных описаний сущностей, которые будут отображаться на документационном сайте. Эти файлы **не смешиваются** с `game-content/.../asset-specs/`.

Предварительно прочитай (только чтение!):
- `docs/specs/technical/visual-docs-preview-mode.md` — понять, какие поля (`description`, `role`, `tags`) ожидает JSON-генератор
- `docs/decisions/ADR-001-visual-docs-data-independence.md`

## Цель

Создать `docs/visual-specs/README.md` и нарративные описания для 12 персонажей и 12 локаций категории `abstract=false` из манифеста.

## Ответ по SDD

### 1. Specify
*Ссылка: раздел 6.1 skill-файла.*

Создаваемые артефакты:
- `docs/visual-specs/README.md` — правила namespace
- `docs/visual-specs/characters/{id}.md` × 12 — описания персонажей
- `docs/visual-specs/locations/{id}.md` × 12 — описания локаций

### 2. Plan
*Ссылка: раздел 6.2 skill-файла.*

Каждый файл персонажа содержит:
```markdown
# {displayName}

**displayName:** ...
**description:** 1-2 предложения для карточки сайта (тёплый тон, без технических деталей)
**role:** главный персонаж / NPC / питомец / ...
**tags:** ["romantic", "main"] / ["cozy", "npc"] / ...
```

Каждый файл локации содержит:
```markdown
# {displayName}

**displayName:** ...
**description:** 1-2 предложения (атмосфера места)
**role:** домашняя локация / рабочая / общественная / ...
**mood:** уютная / деловая / романтическая / ...
**tags:** ["home", "cozy"] / ...
```

### 3. Implement

Для получения списка сущностей: прочитай `specs-manifest.xml` (или соответствующий манифест) и возьми все записи с `abstract=false` из категорий `characters` и `locations`.

**Критерии готовности (DoD):**
- [ ] Все 12 персонажей имеют `docs/visual-specs/characters/{id}.md`.
- [ ] Все 12 локаций имеют `docs/visual-specs/locations/{id}.md`.
- [ ] Нет упоминаний технических деталей (Java, JSON, PNG, XML) в файлах.
- [ ] Описания соответствуют тону игры (светлый ромком, тепло).
- [ ] Самопроверка по чеклисту Нарратора (раздел 8 skill-файла) пройдена.

---

## Жёсткие ограничения

- ❌ Никакого кода, технических деталей в описательных файлах.
- ❌ Не создавать задачи в `tasks/`.
- ❌ Не изменять `src/`, `assets/specs/`, `config/`.
- ✅ Список сущностей — строго из манифеста, не придумывать.
- ✅ Тон — светлый ромком, уют, тепло.
- ✅ Работа строго в ветке `main`.
