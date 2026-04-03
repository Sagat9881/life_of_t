# TASK-FE-DOC-002: Реализовать панель деталей и анимацию спрайтов

| Поле | Значение |
|------|----------|
| **ID** | TASK-FE-DOC-002 |
| **Тип** | frontend |
| **Компонент** | `docs-site/js/detail.js` |
| **Исполнитель** | JavaScript Developer |
| **Приоритет** | Средний |
| **Статус** | ✅ done |

---

## Что сделано

### `docs-site/js/detail.js` — полная реализация

| Метод | Слой | Описание |
|--------|------|----------|
| `initDetailPanel()` | init | подписка на close/backdrop/Escape |
| `openDetail(entity)` | state + render | открыть панель, заполнить данными из entity |
| `closeDetail()` | state | закрыть, отменить rAF, сбросить состояние |
| `buildSpriteSection(entity)` | render | `<canvas>` + `requestAnimationFrame` или placeholder |
| `buildAnimControls(canvas)` | render | play/pause + prev/next frame |
| `buildAnimList(animations)` | render | чипсы анимаций из массива |
| `buildPaletteSection(colorPalette)` | render | сетка свачей |
| `buildJsonSection(entity)` | render | `<details><pre>JSON</pre></details>` |
| `drawFrame(canvas)` | render | canvas + `imageSmoothingEnabled=false` |
| `animLoop(canvas, controls)` | render | rAF цикл с `fps` лимитером |

### `docs-site/css/style.css` — обновлён

- Добавлены стили: `.detail-panel`, `.detail-sprite-area`, `.sprite-canvas`, `.anim-controls`, `.anim-btn`, `.detail-palette-grid`, `.detail-spec`, `.detail-json`
- Тёмная тема полностью поддерживается
- Респонсивность 375px+

## Критерии приёмки

- [x] Клик на карточку открывает панель без перезагрузки.
- [x] JSON-спецификация отображается для всех типов.
- [x] Анимация запускается если `spriteAtlasFile !== null`.
- [x] `spriteAtlasFile === null` — placeholder без ошибок в console.
- [x] `grep -rn 'tanya\|sam\|aijan' docs-site/js/detail.js` → 0 результатов.

## Коммиты

- `cf960709ea6bb419d8380168c44c38513bdd14c1` — feat(docs-site): TASK-FE-DOC-002
