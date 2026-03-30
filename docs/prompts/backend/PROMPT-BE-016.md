# PROMPT-BE-016 — Реализация `ManifestScanner` (manifest-driven сканирование)

> **Роль:** Java Developer  
> **Skill-файл:** `java-developer-skill.md`  
> **Задача:** `tasks/backend/TASK-BE-016.md`

---

## Инструкция для Java Developer

Ты — **Java Developer** проекта «Life of T». Строго следуй `java-developer-skill.md`.

**Перед началом:**
1. Перечитай `java-developer-skill.md` §5.5 «Независимость от структуры директорий и манифесты блоков» — это **основной раздел** для этой задачи.
2. Убедись, что TASK-BE-015 выполнена (`SpecLoader<T>` существует).
3. Прочитай `game-content/.../narrative/asset_manifest.xml` для понимания существующего формата манифеста.

---

## Задача

Реализуй `ManifestScanner` — сканер манифестов нарративных блоков — в пакете `ru.lifegame.backend.infrastructure.spec`.

---

## Контекст

- Бэкенд сейчас угадывает структуру блоков по путям файловой системы → нарушение §5.5.
- Манифест блока (файл `manifest.*` в директории блока) описывает ВСЕ сущности блока и их пути.
- После этой задачи бэкенд сканирует только манифесты, получает список сущностей, загружает их через `SpecLoader<T>`.

---

## Что реализовать

### Доменная модель `ru.lifegame.backend.domain.spec`
```java
public record SpecEntry(String entityName, String specType, Path relativePath) {}

public record BlockManifest(String blockId, List<SpecEntry> entries) {}
```

### Инфраструктура `ru.lifegame.backend.infrastructure.spec`
```java
public class ManifestScanner {
    // Сканирует packageRoot на предмет manifest-файлов
    // Возвращает список BlockManifest (один на каждый найденный манифест)
    // НЕ угадывает структуру директорий — только читает манифест
    public List<BlockManifest> scanPackage(Path packageRoot) { ... }
}
```

### Манифесты блоков

Если манифестов нет — создать `manifest.json` (или `.xml`, в зависимости от существующего формата) для блоков:
- `narrative/specs/quest/manifest.*`
- `narrative/specs/confilcts/manifest.*`
- `narrative/specs/npc/manifest.*`
- `narrative/specs/world-events/manifest.*`

Каждый манифест содержит: `blockId`, список `entries` (entityName + specType + relativePath).

### Unit-тесты
- scanPackage на директории с 2 блоками → возвращает 2 BlockManifest
- Пустая директория → пустой список, нет исключений
- Манифест с 3 entries → BlockManifest.entries().size() == 3

---

## Ответ по SDD + DDD

- **SDD Specify** — BlockManifest, SpecEntry в domain-слое; ссылка на §5.5.
- **SDD Plan** — список классов по DDD-слоям.
- **SDD Task** — подтверждение TASK-BE-016.
- **SDD Implement** — полный код + манифест-файлы для 4 блоков + тесты.

---

## Ограничения (java-developer-skill.md §5.5)

- ManifestScanner НЕ сканирует поддиректории произвольно
- Добавление нового блока = только новый манифест-файл, без изменений в коде
- Каждый шаг обосновывай ссылкой на раздел `java-developer-skill.md`
