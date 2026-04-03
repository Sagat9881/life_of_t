# Промт 02 — Техспека: docs-preview output mode генератора ассетов

## Роль и skill-файл

Роль: **System Analyst**  
Skill-файл: `system-analyst-skill.md`

---

## Инструкция перед началом

Перечитай `system-analyst-skill.md` (разделы 1, 2, 3, 5, 6, 9). Прочитай:
- `java-developer-skill.md` (разделы 2, 3.2, 5, 6, 7) — чтобы понять ограничения Java Developer;
- `docs/prompts/_core/unified-asset-schema.xml` — текущий формат XML-спек;
- `asset-generator/pom.xml` — параметры `specs.dir` и `output.dir`, как передаются в `AssetGeneratorRunner`.

Резюмируй обязанности и ограничения, затем выполняй.

---

## Задача

Создай техническую спецификацию в `docs/specs/technical/visual-docs-preview-mode.md`.

### Контекст

Сейчас `AssetGeneratorRunner` запускается с параметрами `specs.dir` и `output.dir` и генерирует PNG-спрайт-стрипы + `sprite-atlas.json` для игрового рендера (размеры определяются спекой: `frame-width`, `frame-height`, анимации до 50 кадров).

Для документационного сайта нужен **новый output-mode** — `docs-preview` — который генерирует для каждой сущности из манифеста:
- Один статичный PNG-превью фиксированного размера (128×128 px, первый кадр idle-анимации или первый слой).
- JSON-метаданные карточки: `entityId`, `entityType`, `caption`, `previewPath`, `animationCount`, `layerCount`, `specPath`.

Этот режим не заменяет основной режим генерации — он запускается **дополнительно** через отдельный параметр CLI.

### Что должно быть в техспеке

Структура по разделу 6 `system-analyst-skill.md`:

1. **Контекст и цель**: зачем нужен docs-preview mode, чем отличается от основного.
2. **Функциональные требования**:
   - Генератор принимает новый параметр `--output-mode=docs-preview` (или системное свойство `output.mode=docs-preview`).
   - В docs-preview mode: для каждой не-abstract сущности из манифеста генерируется `{entityType}/{entityId}/preview.png` (128×128 RGBA 32-bit) и `{entityType}/{entityId}/card-meta.json`.
   - Список сущностей берётся **только из `specs-manifest.xml`** — никакого хардкода.
   - Если у сущности нет idle-анимации — берётся первый кадр первого слоя.
   - Если сущность `abstract="true"` — пропускается.
3. **Нефункциональные требования**: PNG 128×128, RGBA 32-bit (TYPE_INT_ARGB), без антиалиасинга; JSON UTF-8; время генерации всего каталога — не более 60 секунд на ubuntu-latest.
4. **Формат `card-meta.json`**: опиши все поля (entityId, entityType, caption, previewPath, animationCount, layerCount, specVersion, specPath). Все значения — из XML-спеки, никакого хардкода.
5. **Контракт CLI**: как именно передаётся параметр в `AssetGeneratorRunner` (системное свойство или аргумент командной строки — выбери и зафиксируй один способ, согласованный с текущим `pom.xml`).
6. **Ограничения для Java Developer**: код нового режима не должен содержать имён персонажей/локаций; добавление новой сущности в манифест не требует изменений в коде генератора.
7. **Связанные артефакты**: `specs-manifest.xml`, `unified-asset-schema.xml`, `asset-generator/pom.xml`, `ADR-001`.
8. **Метрики готовности**: каждая не-abstract сущность из манифеста имеет `preview.png` и `card-meta.json`; CI-шаг проверяет это динамически (не по хардкоду).
9. **Задачи для Java Developer**: укажи идентификаторы задач, которые будут созданы по этой спеке (TASK-BE-DOC-001, TASK-BE-DOC-002 — детали в промте 06).

### Критерии готовности

- [ ] Спека создана в `docs/specs/technical/visual-docs-preview-mode.md`.
- [ ] Формат `card-meta.json` описан полностью (все поля, типы, примеры).
- [ ] Чётко указано, как передаётся `output-mode` — без двусмысленности.
- [ ] Явно запрещён хардкод сущностей согласно ADR-001.
- [ ] Указаны ссылки на связанные артефакты.

> **Ограничение:** не пиши Java-код. Только контракты, форматы и требования.
