# TASK-BE-DOC-002: Добавить sentinel-сигнал завершения генерации

| Поле | Значение |
|------|----------|
| **ID** | TASK-BE-DOC-002 |
| **Тип** | backend / assets |
| **Компонент** | `asset-generator/` + `.github/workflows/asset-generation.yml` |
| **Исполнитель** | Java Developer |
| **Приоритет** | Высокий |
| **Зависимости** | TASK-BE-DOC-001 (частично) |
| **Связанные спецификации** | [`docs/specs/technical/ci-fix-asset-hardcode.md`](../../../docs/specs/technical/ci-fix-asset-hardcode.md) |
| **ADR** | [ADR-001](../../../docs/decisions/ADR-001-visual-docs-data-independence.md) |

---

## Описание

CI использует `tanya_idle.png` как сигнал завершения генерации — это хардкод, нарушающий ADR-001.
Требуется определить и реализовать data-driven сигнал завершения.

## Задачи реализации

1. Определить: какой файл гарантированно создаётся **последним** при завершении генерации
   (кандидаты: `sprite-atlas.json`, `generation-complete.sentinel`).
2. Если `sprite-atlas.json` уже создаётся последним — задача только в документировании и фиксации
   этого контракта в `docs/decisions/` или комментарии в коде.
3. Если нет — добавить создание sentinel-файла в Infrastructure слой генератора.
4. Обновить `.github/workflows/asset-generation.yml`: заменить проверку `tanya_idle.png`
   на проверку согласованного sentinel-файла.

## Критерии приёмки

- [ ] В `asset-generation.yml` нет имени `tanya_idle.png` в шаге ожидания.
- [ ] Используемый сигнал задокументирован в комментарии или ADR.
- [ ] CI проходит при наличии нового персонажа без `tanya_idle.png`.
