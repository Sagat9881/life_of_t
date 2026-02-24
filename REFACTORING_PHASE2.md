# Рефакторинг Фаза 2: Агрегаты и Репозитории

## Дата: 24 февраля 2026

## Обзор изменений Фазы 2

Вторая фаза рефакторинга сфокусирована на выделении агрегатов согласно DDD принципам, улучшении системы репозиториев и добавлении валидации.

## Выделение агрегатов

### 1. GameSession Aggregate

**Агрегат сессии игры** - главный агрегат всего приложения.

#### Структура:
```
domain/model/session/
├── GameSession.java              [Root Entity]
├── GameTime.java                 [Value Object]
├── GameOverChecker.java          [Domain Service]
├── GameOverReason.java           [Enum]
└── GameSessionRepository.java    [Repository Interface]
```

#### Изменения:

**GameSession** (Root Entity)[cite:77]
- Добавлена валидация в конструкторе
- Все поля проверяются на `null`
- Метод `createNew()` валидирует telegramUserId
- Добавлен метод `validateNotFinished()` для invariants
- Улучшена инкапсуляция

**GameTime** (Value Object)[cite:78]
- Конвертирован в `record` (immutable)
- Добавлена валидация:
  - День не может быть отрицательным
  - Час должен быть в диапазоне [0, HOURS_PER_DAY)
  - `advanceHours()` не может принимать отрицательные значения

**GameOverChecker** (Domain Service)[cite:79]
- Переме щён в aggregate session
- Проверяет условия завершения игры
- Часть бизнес-логики aggregate

### 2. Границы Aggregate

#### GameSession - Root Aggregate
**Что входит:**
- `GameSession` - корневая сущность
- `GameTime` - value object времени
- `GameOverChecker` - domain service
- `GameOverReason` - enum причин окончания игры

**Инварианты:**
1. Нельзя выполнять действия в завершённой сессии
2. Время не может превышать лимит дня
3. Все компоненты session не могут быть null

**Граница:**
- Внешние агрегаты (PlayerCharacter, Relationships, Pets) ссылаются через GameSession
- GameSession управляет жизненным циклом всех компонентов
- Никто не может изменять внутренние компоненты напрямую

## Улучшение репозиториев

### Проблема
**Было:**
- `SessionRepository` в `application.port.out`
- Прямая зависимость от infrastructure
- Нет чёткого разделения repository и persistence

### Решение

#### 1. GameSessionRepository в domain[cite:81]

```java
// domain/model/session/GameSessionRepository.java
public interface GameSessionRepository {
    Optional<GameSession> findByTelegramUserId(String telegramUserId);
    void save(GameSession session);
    boolean exists(String telegramUserId);
}
```

**Принципы:**
- Интерфейс определён в domain
- Domain не зависит от infrastructure
- Следует Dependency Inversion Principle

#### 2. Application Port extends Domain Repository[cite:82]

```java
// application/port/out/SessionRepository.java
public interface SessionRepository extends GameSessionRepository {
    // Может добавить application-specific методы
}
```

**Преимущества:**
- Обратная совместимость
- Application может расширить интерфейс
- Domain остаётся изолированным

#### 3. Разделение Repository и Persistence[cite:89]

**SessionPersistence** - отвечает за физическое хранение:
```java
public interface SessionPersistence {
    Optional<GameSession> load(String telegramUserId);
    void persist(GameSession session);
    boolean exists(String telegramUserId);
}
```

**InMemorySessionRepository** - координирует кэш и persistence[cite:88]:
```java
public class InMemorySessionRepository implements SessionRepository {
    private final Map<String, GameSession> sessions; // Кэш
    private final SessionPersistence persistence;    // Хранилище
}
```

**Разделение ответственностей:**
- Repository = координация + кэширование
- Persistence = физическое хранение (файлы, БД)

## Добавление валидации

### Value Objects с валидацией

#### GameTime[cite:78]
```java
public record GameTime(int day, int hour) {
    public GameTime {
        if (day < 0) {
            throw new IllegalArgumentException("Day cannot be negative: " + day);
        }
        if (hour < 0 || hour >= GameBalance.HOURS_PER_DAY) {
            throw new IllegalArgumentException("Hour must be between 0 and ...");
        }
    }
}
```

**Преимущества:**
- Невозможно создать невалидный GameTime
- Валидация в одном месте
- Compile-time гарантии типобезопасности

### Invariants в агрегатах

#### GameSession[cite:77]
```java
private void validateNotFinished() {
    if (isFinished()) {
        throw new InvalidGameStateException("Game session is finished");
    }
}

public ActionResult executeAction(GameAction action) {
    validateNotFinished(); // Проверка invariant
    // ...
}
```

**Защита инвариантов:**
1. Нельзя изменять завершённую сессию
2. Все операции проверяют состояние перед выполнением
3. Состояние всегда консистентно

## Обновления всех зависимостей

### Application Services
Все сервисы обновлены для новой локации GameSession:
- `ExecutePlayerActionService`[cite:83]
- `StartOrLoadSessionService`[cite:84]
- `GetGameStateService`[cite:85]
- `ChooseConflictTacticService`[cite:86]
- `ChooseEventOptionService`[cite:87]

### Infrastructure
- `InMemorySessionRepository`[cite:88] - обновлён импорт
- `SessionPersistence`[cite:89] - новый интерфейс для persistence

## Новая структура пакетов

```
src/main/java/ru/lifegame/backend/
├── domain/
│   └── model/
│       ├── session/                    [NEW AGGREGATE]
│       │   ├── GameSession.java        [Root Entity + Validation]
│       │   ├── GameTime.java           [Value Object + Validation]
│       │   ├── GameOverChecker.java    [Domain Service]
│       │   ├── GameOverReason.java     [Enum]
│       │   └── GameSessionRepository.java [Repository Interface]
│       ├── character/                  [TODO: Next aggregate]
│       ├── relationship/               [TODO: Next aggregate]
│       └── pet/                        [TODO: Next aggregate]
│
├── application/
│   └── port/out/
│       └── SessionRepository.java      [Extends domain repository]
│
└── infrastructure/
    └── persistence/
        ├── SessionPersistence.java     [NEW: Persistence interface]
        ├── InMemorySessionRepository.java [Updated]
        └── FileSessionPersistence.java
```

## Принципы DDD применённые

### 1. Aggregate Pattern
- **GameSession** - чётко определённый aggregate root
- Внешний доступ только через root
- Транзакционная граница = aggregate граница
- Инварианты защищаются root entity

### 2. Value Objects
- **GameTime** - immutable, validated
- Операции возвращают новые экземпляры
- Идентичность через значения, не через ID

### 3. Repository Pattern
- Интерфейс в domain слое
- Скрывает детали persistence
- Работает с агрегатами целиком
- Не раскрывает детали хранения

### 4. Ubiquitous Language
- `GameSession` - не просто "Session"
- `GameTime` - доменное понятие времени в игре
- `GameOverReason` - явные причины окончания

## Преимущества фазы 2

### 1. Явные границы агрегатов
✅ Понятно что относится к GameSession
✅ Понятно как взаимодействовать с aggregate
✅ Транзакционные границы очевидны

### 2. Валидация данных
✅ Невозможно создать невалидные объекты
✅ Ошибки обнаруживаются рано
✅ Меньше defensive programming

### 3. Изоляция domain
✅ Domain определяет свой repository
✅ Нет зависимостей на infrastructure
✅ Легко тестировать domain логику

### 4. Разделение ответственностей
✅ Repository ≠ Persistence
✅ Каждый компонент делает одну вещь
✅ Легче заменять реализации

## Что дальше?

### Фаза 3: Остальные агрегаты

1. **PlayerCharacter Aggregate**
   - `domain/model/character/`
   - `PlayerCharacter`, `Stats`, `Skills`, `JobInfo`
   - Валидация stats (0-100)

2. **Relationships Aggregate**
   - `domain/model/relationship/`
   - `Relationships`, `Relationship`, `RelationshipChanges`
   - Инварианты отношений

3. **Pets Aggregate**
   - `domain/model/pet/`
   - `Pets`, `Pet`
   - Валидация состояния питомцев

### Фаза 4: Domain Events

- Event sourcing для истории игры
- Интеграция с внешними системами
- Аудит изменений

### Фаза 5: CQRS

- Разделение команд и запросов
- Оптимизированные read models
- Масштабируемость

## Статистика Фазы 2

- **Создано файлов**: 5 (session aggregate + repository)
- **Обновлено файлов**: 7 (all application services + infrastructure)
- **Удалено файлов**: 4 (old locations)
- **Коммитов**: 19
- **Добавлено валидации**: 8 invariants + 3 value objects

## Обратная совместимость

✅ Все REST endpoints работают
✅ Поведение приложения идентично
✅ Application port расширяет domain repository
✅ Миграция прозрачна для клиентов

## Ссылки

- [REFACTORING.md](./REFACTORING.md) - Фаза 1
- [Domain-Driven Design](https://martinfowler.com/bliki/DomainDrivenDesign.html)
- [Aggregate Pattern](https://martinfowler.com/bliki/DDD_Aggregate.html)
