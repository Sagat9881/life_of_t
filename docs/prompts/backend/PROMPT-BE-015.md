# PROMPT-BE-015 — Реализация `SpecLoader<T extends NarrativeSpec>`

> **Роль:** Java Developer  
> **Skill-файл:** `java-developer-skill.md`  
> **Задача:** `tasks/backend/TASK-BE-015.md`

---

## Инструкция для Java Developer

Ты — **Java Developer** проекта «Life of T». Строго следуй `java-developer-skill.md`.

**Перед началом:**
1. Перечитай `java-developer-skill.md` полностью (§2 Зона ответственности, §3.1, §5.1, §6 Ограничения директорий).
2. Резюмируй обязанности Java Developer в 3–5 предложениях.
3. Подтверди: ты не описываешь Canvas/DOM, не правишь нарратив, не создаёшь задачи.

---

## Задача

Реализуй обобщённый `SpecLoader<T extends NarrativeSpec>` в пакете `ru.lifegame.backend.infrastructure.spec`.

---

## Контекст

- Текущая загрузка нарративных спецификаций содержит хардкоженные пути и switch-case по типам.
- Источники спецификаций: `game-content/.../narrative/` (quests, confilcts, npc, world-events).
- Техдокументация: `docs/CONTENT_API.md`, `docs/EVENT_SYSTEM.md`.

---

## Что реализовать

### Пакет `ru.lifegame.backend.domain.spec`
```java
// Базовый интерфейс всех нарративных спецификаций
public interface NarrativeSpec {
    String getId();
    String getBlockId();
}

// Value object для пути
public record SpecPath(String blockId, String entityName) {
    public static SpecPath of(String blockId, String entityName) { ... }
}
```

### Пакет `ru.lifegame.backend.infrastructure.spec`
```java
// Обобщённый загрузчик — НИКАКИХ if/switch по именам сущностей
public class SpecLoader<T extends NarrativeSpec> {
    public T load(SpecPath path, Class<T> type) { /* десериализация из файла */ }
    public List<T> loadAll(SpecPath basePath, Class<T> type) { /* загрузка всех в директории */ }
}
```

### Unit-тесты
- Загрузка `QuestSpec` по `SpecPath.of("quest", "morning_cat")` → результат не `null`, поля заполнены
- Загрузка `NpcSpec` → аналогично
- Несуществующий путь → выбрасывает типизированное исключение (не `NullPointerException`)

---

## Ответ по SDD + DDD (java-developer-skill.md §2, §3.1)

- **SDD Specify** — изменения в domain-слое (NarrativeSpec, SpecPath), ссылка на §3.1.
- **SDD Plan** — список классов по DDD-слоям: domain / infrastructure.
- **SDD Task** — подтверждение TASK-BE-015.
- **SDD Implement** — полный код классов + тесты.

---

## Ограничения (java-developer-skill.md §5.1)

- Запрещены: `if (questName.equals(...))`, `switch(entityName)`, хардкоженные пути
- `SpecPath` — value object, не `String` константа
- Десериализатор — конфигурируемый (через конструктор / DI)
- Каждый шаг обосновывай ссылкой на раздел `java-developer-skill.md`
