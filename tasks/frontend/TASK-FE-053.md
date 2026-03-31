# TASK-FE-053 — Компонент `.asset-card` с hover-preview анимации

**Тип:** frontend  
**Приоритет:** MEDIUM  
**Статус:** TODO  
**Роль:** JavaScript Developer  
**Связанная спецификация:** `docs/specs/STATUS_PAGE_ASSETS_SPEC.md` (раздел 4, 7.2, 7.3)  
**Зависимость:** TASK-FE-052 (базовая галерея должна быть реализована)

## Описание

Реализовать компонент `.asset-card` и поведение при hover согласно спецификации SA-012, раздел 4.

## Что реализовать

1. **JS-функция `buildAssetCard(entry, category)`** — возвращает DOM-элемент `div.asset-card` по спецификации (раздел 4.1).
2. **CSS hover-поведение:** при наведении на `.asset-card` — масштабировать `.asset-preview` для показа полной сетки кадров.
3. **Отсутствие meta (`metaPath === null`):** класс `.no-meta`, без `.animation-list`, пометка `⚠️ Метаданные отсутствуют` (раздел 7.2).
4. **Битый PNG:** `onerror` заменяет `src` на `assets/ui/placeholder.png`, добавляет класс `.broken` (раздел 7.3).
5. **Форматирование:** `fileSizeBytes → KB`, `frameWidth × frameHeight px`, счётчик frames.

## Критерии приёмки

- [ ] HTML-структура карточки соответствует разделу 4.1 спецификации
- [ ] CSS-классы соответствуют таблице из раздела 4.2
- [ ] Hover показывает увеличенное изображение атласа
- [ ] `.no-meta` карточка рендерится без `.animation-list` и с предупреждением
- [ ] Битый PNG заменяется `placeholder.png`; добавляется класс `.broken`
- [ ] `fileSizeBytes: null` отображается как `«—»`
- [ ] Функция `buildAssetCard` принимает `AssetEntry` и возвращает `HTMLElement`
