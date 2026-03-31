# TASK-SA-011 — Спецификация HTML Overview-страницы

| Поле | Значение |
|------|----------|
| **ID** | TASK-SA-011 |
| **Тип** | analytics |
| **Статус** | DONE |
| **Приоритет** | HIGH |
| **Роль** | System Analyst |
| **Эпик** | EPIC-DEVOPS-001 |
| **Связанная спека** | `docs/specs/STATUS_PAGE_OVERVIEW_SPEC.md` |
| **Зависит от** | TASK-SA-010 |

## Описание

Спроектировать техническую спецификацию HTML Overview-страницы для раздела метрик на Project Status Dashboard. Специфицировать HTML-структуру, компоненты карточек и drilldown-панелей, JavaScript API и CSS pixel-art токены.

## Критерии приёмки

- [x] `docs/specs/STATUS_PAGE_OVERVIEW_SPEC.md` создан
- [x] Wireframe описывает все 5 карточек и drilldown-панель (Раздел 1)
- [x] HTML-структура `.metric-card` и `.drilldown-panel` задокументированы (Разделы 2, 3)
- [x] Маппинг `metrics.json` → UI задокументирован для каждого поля (Раздел 4)
- [x] JavaScript API задокументирован с сигнатурами (Раздел 5)
- [x] CSS-переменные и pixel-art токены определены (Раздел 6)
- [x] Нет ссылок на фреймворки
- [x] Созданы задачи TASK-FE-050 и TASK-FE-051
