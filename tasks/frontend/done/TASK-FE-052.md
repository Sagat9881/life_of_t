# TASK-FE-052 — Реализация Assets Gallery HTML+CSS+JS

**Тип:** frontend  
**Приоритет:** HIGH  
**Статус:** TODO  
**Роль:** JavaScript Developer  
**Связанная спецификация:** `docs/specs/STATUS_PAGE_ASSETS_SPEC.md`  
**Зависимость:** TASK-BE-021

## Описание

Реализовать раздел Assets Gallery в рамках Project Status Dashboard согласно спецификации SA-012.

## Критерии приёмки

- [ ] Все 5 табов отрисовываются; счётчики корректны
- [ ] Переключение табов без перезагрузки страницы
- [ ] URL hash обновляется при смене таба
- [ ] Deep-link `#assets-{category}` активирует нужный таб
- [ ] Пустая категория показывает placeholder
- [ ] `loading="lazy"` на всех `<img>`
- [ ] ARIA-атрибуты: `role="tablist"`, `role="tab"`
