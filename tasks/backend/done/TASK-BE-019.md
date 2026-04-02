# TASK-BE-019 — Скрипт сбора метрик backend-кода (счётчики классов)

| Поле | Значение |
|------|----------|
| **ID** | TASK-BE-019 |
| **Тип** | backend |
| **Статус** | TODO |
| **Приоритет** | MEDIUM |
| **Роль** | Java Developer |
| **Эпик** | EPIC-DEVOPS-001 |
| **Связанная спека** | `docs/specs/PROJECT_METRICS_SPEC.md` §2.2 |
| **Зависит от** | TASK-SA-010 |

## Описание

Написать скрипт `scripts/metrics/collect_backend.py`, который анализирует Java-код бэкенда и собирает метрики группы **Backend Completeness (BC)**.

## Требования к скрипту

1. Считать файлы `.java` в директориях:
   - BC-01: `backend/src/main/java/ru/lifegame/backend/domain/model/`
   - BC-02: `backend/src/main/java/ru/lifegame/backend/application/service/`
   - BC-03: `backend/src/main/java/ru/lifegame/backend/infrastructure/web/controller/`
2. BC-04: `COUNT(*.java в test/) / COUNT(*.java в main/) * 100`
3. BC-05: Java-файлы без хардкодированных строк нарратива
4. Записывать результат в `docs/status/backend_metrics.json`

## Формат выходного JSON

```json
{
  "aggregates": 0,
  "services": 0,
  "controllers": 0,
  "testCoveragePct": 0.0,
  "narrativeSpecsWithoutHardcode": 0
}
```

## Критерии приёмки

- [ ] Скрипт создан в `scripts/metrics/collect_backend.py`
- [ ] Все метрики BC-01..BC-05 вычисляются корректно
- [ ] Скрипт не падает при отсутствии `backend/src/`
