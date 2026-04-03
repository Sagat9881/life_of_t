# TASK-FE-DOC-001: Статический сайт — каркас и data-driven рендер карточек

**Тип:** frontend  
**Статус:** todo  
**Роль:** JavaScript Developer  
**Skill-файл:** `javascript-developer-skill.md`

---

## Контекст

Документационный сайт строится как pure-static приложение. Все данные поступают из `site/data/catalog.json` и `site/data/docs.json`, собранных в CI bash-скриптами. Структура данных описана в `docs/specs/technical/visual-docs-site-structure.md`.

---

## Связанные спецификации

- `docs/specs/technical/visual-docs-site-structure.md` — формат `catalog.json`, `docs.json`, компоненты (`EntityCard`, `FilterBar`).
- `docs/decisions/ADR-001-visual-docs-data-independence.md` — запрет хардкода `entityType`/`entityId`.
- `javascript-developer-skill.md` — ограничения роли JS Developer.

---

## Что нужно сделать

1. Создать директорию `site/` с подструктурой:
   ```
   site/
   ├── src/          ← JS-модули
   ├── data/         ← CI кладёт сюда catalog.json и docs.json
   ├── public/       ← статические ресурсы (CSS, иконки)
   └── dist/         ← выходной каталог сборки
   ```
2. Реализовать загрузку `catalog.json` при инициализации (`fetch` или `import`).
3. Итерационно рендерить компонент **EntityCard** для каждой записи каталога. Перебор строго по данным — без `if (type === '...')` или аналогов.
4. Реализовать компонент **FilterBar**: список фильтров строится из уникальных значений поля `entityType` каталога — динамически, без хардкода типов.
5. Фильтрация — клиентская, по полю `entityType` из данных.
6. Точка входа сборки: `npm run build` → результат в `site/dist/`.

---

## Ограничения (из `javascript-developer-skill.md` и ADR-001)

- Запрещено: `if (type === 'characters')` или любое ветвление по строковым именам типов/id сущностей.
- Стек: **vanilla JS + vanilla CSS**. Фреймворки (React/Vue/Angular) не допускаются.
- Принцип «данные управляют рендером» — обязателен во всех компонентах.
- Не описывать Canvas/DOM для игровых анимаций — это зона игрового клиента, не docs-site.

---

## Критерии приёмки

- [ ] При добавлении новой сущности в `catalog.json` карточка появляется автоматически без изменений JS-кода.
- [ ] `FilterBar` корректно отражает все `entityType` из каталога, включая новые.
- [ ] Нет хардкода `entityType` или `entityId` в коде (CI-grep-проверка пройдена).
- [ ] Сайт собирается командой `npm run build` в `site/`.
- [ ] Структура `site/src/`, `site/data/`, `site/public/`, `site/dist/` создана и задокументирована в `README.md` внутри `site/`.
