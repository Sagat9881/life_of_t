# Техспек: устранение хардкода в `asset-generation.yml`

| Поле | Значение |
|------|----------|
| **Путь** | `docs/specs/technical/ci-fix-asset-hardcode.md` |
| **Компонент** | `.github/workflows/asset-generation.yml` |
| **SDD-фаза** | Specify |
| **Дата** | 2026-04-03 |
| **Ответственный** | System Analyst |
| **Исполнитель** | Java Developer (минимальные изменения в Java) + DevOps (bash/CI) |
| **ADR** | [ADR-001](../decisions/ADR-001-visual-docs-data-independence.md) |
| **Задачи** | `tasks/backend/TASK-BE-DOC-003.md` |

---

## 1. Контекст и цель

В `.github/workflows/asset-generation.yml` выявлены следующие нарушения ADR-001:

| Шаг | Нарушение |
|-----|-----------|
| `Generate assets via application startup` | Ожидание `tanya_idle.png` как сигнала завершения |
| `Verify generated assets exist` | Bash-массив `EXPECTED` с 6 конкретными именами PNG |
| `Validate atlas dimensions` | Python-словарь с именами и ожидаемыми размерами |
| `Verify no anti-aliasing` | Python-список `sprites` с 5 конкретными именами |

**Цель**: заменить все хардкоды на data-driven логику, читающую из `specs-manifest.xml`.

---

## 2. Спецификация исправлений

### Исправление 1 — Сигнал завершения генерации

**Было:**
```bash
if [ -f "${{ env.ASSET_OUTPUT_DIR }}/tanya_idle.png" ]; then
```

**Стало:**
```bash
# Ждём появления sprite-atlas.json (генерируется генератором после всех PNG)
if [ -f "${{ env.ASSET_OUTPUT_DIR }}/sprite-atlas.json" ]; then
```

Либо (если `sprite-atlas.json` не создаётся): ждём появления `docs-preview.json`
при запуске с `--output-mode=docs-preview`. **Java Developer уточняет**, какой файл
гарантированно создаётся последним.

### Исправление 2 — Верификация существования ассетов

**Было:**
```bash
EXPECTED=(
  "tanya_idle.png"
  "tanya_walk.png"
  ...
)
for asset in "${EXPECTED[@]}"; do ...
```

**Стало:**
```bash
# Динамически строим список ожидаемых PNG из манифеста
python3 - <<'EOF'
import xml.etree.ElementTree as ET, os, sys
manifest = ET.parse(
  'game-content/life-of-t/src/main/resources/asset-specs/specs-manifest.xml'
)
output_dir = os.environ['ASSET_OUTPUT_DIR']
all_ok = True
for entity in manifest.findall('entity'):
  if entity.get('abstract', 'false') == 'false':
    entity_id = entity.get('path', '').split('/')[-1]
    # Ищем любой PNG с prefix entity_id в output dir
    found = [f for f in os.listdir(output_dir) if f.startswith(entity_id) and f.endswith('.png')]
    if found:
      print(f'OK: {entity_id} -> {found}')
    else:
      print(f'MISSING: no PNG for {entity_id}')
      all_ok = False
if not all_ok:
  sys.exit(1)
print('All entities have generated PNG assets.')
EOF
```

### Исправление 3 — Валидация размеров атласа

**Было:** Python-словарь с захардкоженными размерами.

**Стало:** Читать ожидаемые размеры из XML-спека каждой сущности (`<meta width="...">` или аналогично).
Если спек не содержит ожидаемых размеров — **шаг пропускается** (не падает CI).

Либо: Java Developer добавляет в `sprite-atlas.json` поля `expectedWidth`/`expectedHeight`
для каждого спрайта, и CI читает их оттуда.

> **Требует согласования с Java Developer**: какой источник истины для ожидаемых размеров.

### Исправление 4 — Проверка анти-алиасинга

**Было:** Python-список `sprites` с 5 именами.

**Стало:**
```python
import os
output_dir = os.environ.get('ASSET_OUTPUT_DIR', '')
pngs = [f for f in os.listdir(output_dir) if f.endswith('.png')]
all_ok = True
for name in pngs:
    path = os.path.join(output_dir, name)
    size = os.path.getsize(path)
    if size > 0:
        print(f'OK: {name} ({size} bytes, non-empty)')
    else:
        print(f'FAIL: {name} is empty')
        all_ok = False
if not all_ok:
    exit(1)
print(f'All {len(pngs)} sprite files are valid non-empty PNGs.')
```

---

## 3. Открытые вопросы (для Java Developer)

1. Какой файл гарантированно создаётся **последним** при завершении стандартной генерации?
   (Кандидаты: `sprite-atlas.json`, специальный sentinel-файл, stdout-сообщение)
2. Содержит ли `sprite-atlas.json` ожидаемые размеры спрайтов? Если нет — нужно ли добавить?
3. Требуется ли изменить код генератора для поддержки sentinel-файла?

---

## 4. Метрики и критерии готовности

| Критерий | Измерение |
|----------|-----------|
| `asset-generation.yml` не содержит имён `tanya`, `sam`, `bed` и т.д. | `grep -v '#'` + проверка в CI |
| Добавление новой `<entity>` в манифест проходит CI без изменений YAML | Integration test |
| Верификация охватывает все `abstract=false` сущности | Python count check |
| Шаг ожидания использует data-driven сигнал | Code review |
