# TASK-AS-DOC-001: Создать docs/visual-specs/ и документационные спеки для нарратора

| Поле | Значение |
|------|----------|
| **ID** | TASK-AS-DOC-001 |
| **Тип** | assets / narrative |
| **Компонент** | `docs/visual-specs/` (новая директория) |
| **Исполнитель** | Нарратор |
| **Приоритет** | Средний |
| **Зависимости** | — |
| **Связанные спецификации** | [`docs/specs/technical/visual-docs-preview-mode.md`](../../../docs/specs/technical/visual-docs-preview-mode.md) |
| **ADR** | [ADR-001](../../../docs/decisions/ADR-001-visual-docs-data-independence.md) |

---

## Описание

Директория `docs/visual-specs/` — отдельный namespace для документационных описаний сущностей.
Эти файлы **не смешиваются** с `game-content/.../asset-specs/`.

Нарратор создаёт краткие описания каждой сущности для отображения на документационном сайте:
`description`, `role`, `tags`.

## Задачи реализации

1. Создать `docs/visual-specs/README.md` с описанием namespace и правилами.
2. Для каждой из 12 `abstract=false` сущностей из категории `characters`:
   создать `docs/visual-specs/characters/{id}.md` с полями:
   - `displayName`
   - `description` (1-2 предложения для карточки сайта)
   - `role` (например: главный персонаж, NPC, питомец)
   - `tags` (массив: например ["romantic", "main"])
3. Аналогично для `locations` (12 шт.) — `role`, `mood`, `description`.

## Критерии приёмки

- [ ] Все 12 персонажей имеют документационный md-файл.
- [ ] Все 12 локаций имеют документационный md-файл.
- [ ] Нет упоминаний технических деталей (Java, JSON, PNG) в этих файлах.
- [ ] Описания соответствуют тону игры (светлый ромком).
