# TASK-FE-DOC-002: Реализовать панель деталей и анимацию спрайтов

| Поле | Значение |
|------|----------|
| **ID** | TASK-FE-DOC-002 |
| **Тип** | frontend |
| **Компонент** | `docs-site/js/detail.js` |
| **Исполнитель** | JavaScript Developer |
| **Приоритет** | Средний |
| **Зависимости** | TASK-FE-DOC-001 |
| **Связанные спецификации** | [`docs/specs/technical/visual-docs-site-structure.md`](../../../docs/specs/technical/visual-docs-site-structure.md) |
| **ADR** | [ADR-001](../../../docs/decisions/ADR-001-visual-docs-data-independence.md) |

---

## Описание

Реализовать панель деталей, открывающуюся по клику на карточку:
- Анимация спрайта через перебор кадров `sprite-atlas.png`.
- Раскрытие JSON-спецификации сущности (pretty-print).

## Задачи реализации

1. Создать `docs-site/js/detail.js`: `openDetail(entity)` — отображает панель.
2. Анимация: использовать `requestAnimationFrame` + `<canvas>` или `background-position`
   для перебора кадров из `sprite-atlas.png`. Количество кадров и размер — из `animations` поля.
3. Отображение JSON-спецификации через `<pre>JSON.stringify(entity, null, 2)</pre>`.
4. Кнопка закрытия панели.
5. Поддержка `spriteAtlasFile === null`: скрыть canvas, показать placeholder.

## Критерии приёмки

- [ ] Клик на карточку открывает панель без перезагрузки страницы.
- [ ] JSON-спецификация отображается корректно для всех типов сущностей.
- [ ] Анимация спрайта запускается если `spriteAtlasFile !== null`.
- [ ] При `spriteAtlasFile === null` отображается placeholder без ошибок в console.
- [ ] Нет имён сущностей в `detail.js`.
