# Backend — Архитектура

## Принципы

Бэкенд «Life of T» строится на принципах **чистой архитектуры (Clean Architecture)**
Роберта Мартина и **порт-адаптерного паттерна (Hexagonal Architecture)**.

Главное правило: **зависимости направлены внутрь** — внешние слои зависят от внутренних,
но не наоборот. Доменный код ничего не знает о Spring, HTTP, базах данных или Telegram.

```
┌──────────────────────────────────────────────────────────┐
│  Infrastructure (Spring, HTTP, Persistence)              │
│  ┌────────────────────────────────────────────────────┐  │
│  │  Application (Use Cases, Commands, Queries, Views) │  │
│  │  ┌──────────────────────────────────────────────┐  │  │
│  │  │  Domain (Models, Actions, Rules, Balance)    │  │  │
│  │  └──────────────────────────────────────────────┘  │  │
│  └────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────┘
```

---

## Слой Domain

Содержит бизнес-объекты и правила игры. Не зависит ни от чего.

### Пакет `domain/action/`

**GameAction** — интерфейс любого действия персонажа:
```java
public interface GameAction {
    ActionType getType();
    String getDisplayName();
    String getDescription();
    int getEnergyCost();
    boolean isAvailable(GameSessionReadModel session);
    ActionResult execute(GameSession session);
}
```

**ActionResult** — результат выполнения действия:
```java
public record ActionResult(
    boolean success,
    String message,
    Map<String, Integer> statsDelta,  // изменения характеристик
    List<String> triggeredEvents,
    String nextLocation
) {}
```

**StandardActionType** — перечисление встроенных действий:
```
GO_TO_WORK        — работа (деньги +, стресс +, энергия −)
JOGGING           — пробежка (здоровье +, энергия −)
MAKE_COFFEE       — кофе (энергия +, стресс −)
CALL_HUSBAND      — звонок мужу (настроение +, отношения +)
DATE_WITH_HUSBAND — свидание (все отношения +)
FEED_DUCKS        — кормление уток (настроение +, стресс −)
WALK_DOG          — прогулка с собакой (здоровье +)
PLAY_WITH_CAT     — игра с котом (настроение +)
REST_AT_HOME      — отдых дома (энергия +, стресс −)
REST_ON_BENCH     — отдых на лавочке (стресс −)
HOUSEHOLD         — домашние дела (порядок +, стресс ±)
SELF_CARE         — уход за собой (самооценка +, стресс −)
TALK_TO_COLLEAGUE — разговор с коллегой (отношения +)
VISIT_FATHER      — визит к отцу (отношения +, настроение +)
```

**Actions** — утилитный класс, содержит фабричные методы для всех действий.

**ActionProvider** — Spring-бин, предоставляющий доступные действия для текущего состояния сессии.

### Пакет `domain/model/` (GameSession, Player и др.)

**GameSession** — основной агрегат:
```java
public class GameSession {
    private String id;
    private String telegramUserId;
    private Player player;
    private Map<String, Relationship> relationships;
    private List<Pet> pets;
    private GameTime time;
    private List<Quest> activeQuests;
    private List<String> completedQuestIds;
    private List<Conflict> activeConflicts;
    private GameEvent currentEvent;
    private Ending ending;
    // ... методы изменения состояния
}
```

**Player** — персонаж Татьяна:
```java
public class Player {
    private String id;
    private String name;
    private Stats stats;      // energy, health, stress, mood, money, selfEsteem
    private Job job;
    private String location;
    private Map<String, Boolean> tags;   // достижения/флаги
    private Map<String, Integer> skills; // навыки
    private List<String> inventory;
}
```

**Stats** — мутабельный объект характеристик:
```java
public class Stats {
    private int energy;     // 0–100, тратится на действия
    private int health;     // 0–100, влияет на работоспособность
    private int stress;     // 0–100, снижает эффективность
    private int mood;       // 0–100, влияет на результат действий
    private int money;      // 0+, рубли
    private int selfEsteem; // 0–100, влияет на конфликты
}
```

**Relationship** — отношения с NPC:
```java
public record Relationship(
    String id, String npcCode,
    int closeness,  // 0–100, близость
    int trust,      // 0–100, доверие
    int stability,  // 0–100, стабильность
    int romance     // 0–100, романтика
) {}
```

**GameTime** — игровое время:
```java
public class GameTime {
    private int day;   // номер дня от 1
    private int hour;  // 6–22 (активные часы)
}
```

### Баланс

Каждое действие меняет несколько характеристик согласно балансовой таблице.
Действия не должны менять характеристики напрямую через `Player.getStats().setEnergy(...)` —
вместо этого `ActionResult` возвращает `statsDelta`, который применяет `GameSession`.

Примеры дельт:
```
GO_TO_WORK:     money+150, stress+20, energy-30, mood-10
JOGGING:        health+15, energy-25, stress-10, selfEsteem+5
MAKE_COFFEE:    energy+20, stress-5
CALL_HUSBAND:   mood+15, stress-10, relationships[husband].closeness+5
REST_AT_HOME:   energy+40, stress-20
```

---

## Слой Application

Оркестрирует доменные объекты, реализует use cases.

### Входящие порты (Port In — Use Cases)

Интерфейсы в `application/port/in/`:

```java
public interface StartOrLoadSessionUseCase {
    GameStateView execute(StartSessionCommand command);
}

public interface GetGameStateUseCase {
    GameStateView execute(GetStateQuery query);
}

public interface ExecutePlayerActionUseCase {
    ActionResultView execute(ExecuteActionCommand command);
}

public interface EndDayUseCase {
    GameStateView execute(EndDayCommand command);
}

public interface ChooseConflictTacticUseCase {
    GameStateView execute(ChooseConflictTacticCommand command);
}

public interface ChooseEventOptionUseCase {
    GameStateView execute(ChooseEventOptionCommand command);
}
```

### Команды и запросы (CQRS-lite)

**Команды** — изменяют состояние:
```java
record StartSessionCommand(String telegramUserId) {}
record ExecuteActionCommand(String telegramUserId, String actionId) {}
record EndDayCommand(String telegramUserId) {}
record ChooseConflictTacticCommand(String telegramUserId, String conflictId, String tacticId) {}
record ChooseEventOptionCommand(String telegramUserId, String eventId, String optionId) {}
```

**Запросы** — только чтение:
```java
record GetStateQuery(String telegramUserId) {}
```

### Исходящие порты (Port Out)

```java
// Хранение сессий
public interface SessionRepository {
    Optional<GameSession> findByTelegramUserId(String telegramUserId);
    GameSession save(GameSession session);
    void deleteByTelegramUserId(String telegramUserId);
}

// Публикация событий
public interface EventPublisher {
    void publish(String eventType, Object payload);
}
```

### Реализации Use Cases (Services)

Пример `StartOrLoadSessionService`:
```java
@Service
public class StartOrLoadSessionService implements StartOrLoadSessionUseCase {

    private final SessionRepository sessionRepository;
    private final ActionProvider actionProvider;

    @Override
    public GameStateView execute(StartSessionCommand command) {
        GameSession session = sessionRepository
            .findByTelegramUserId(command.telegramUserId())
            .orElseGet(() -> GameSession.createNew(command.telegramUserId()));

        GameSession saved = sessionRepository.save(session);
        return GameStateMapper.toView(saved, actionProvider.getAvailable(saved));
    }
}
```

### View Objects (DTO)

View объекты — иммутабельные records, используемые только для отдачи данных клиенту:

| View            | Содержимое                                        |
|-----------------|---------------------------------------------------|
| `GameStateView` | Полное состояние: player, relations, time, events |
| `PlayerView`    | Имя, статы, работа, локация, инвентарь            |
| `StatsView`     | energy, health, stress, mood, money, selfEsteem   |
| `ActionOptionView` | id, название, описание, цена энергии, доступность |
| `ConflictView`  | id, описание, тактики                             |
| `EventView`     | id, текст, варианты ответа                        |
| `EndingView`    | тип концовки, текст, условие победы/поражения     |
| `RelationshipView` | id, npcCode, closeness, trust, stability, romance |

---

## Слой Infrastructure

Адаптеры, реализующие исходящие порты.

### Persistence

```java
// InMemorySessionRepository — для разработки и тестов
@Repository
public class InMemorySessionRepository implements SessionRepository {
    private final Map<String, GameSession> store = new ConcurrentHashMap<>();
    // ...
}
```

В продакшене заменяется JPA-репозиторием без изменения кода use cases.

### Web (REST Controllers)

```java
@RestController
@RequestMapping("/api/game")
public class GameController {

    private final StartOrLoadSessionUseCase startOrLoadSessionUseCase;
    private final GetGameStateUseCase getGameStateUseCase;
    private final ExecutePlayerActionUseCase executePlayerActionUseCase;
    // ...

    @PostMapping("/start")
    public GameStateView start(@RequestBody StartRequest req) {
        return startOrLoadSessionUseCase.execute(
            new StartSessionCommand(req.telegramUserId()));
    }

    @PostMapping("/action")
    public ActionResultView executeAction(@RequestBody ActionRequest req) {
        return executePlayerActionUseCase.execute(
            new ExecuteActionCommand(req.telegramUserId(), req.actionId()));
    }
}
```

---

## Как добавить новое действие

1. **Создать класс** в `domain/action/impl/`:
   ```java
   public class YogaAction implements GameAction {
       @Override
       public ActionType getType() { return StandardActionType.YOGA; }

       @Override
       public int getEnergyCost() { return 20; }

       @Override
       public boolean isAvailable(GameSessionReadModel session) {
           return session.getPlayer().getStats().getEnergy() >= 20;
       }

       @Override
       public ActionResult execute(GameSession session) {
           return ActionResult.success("Татьяна позанималась йогой. Стресс ушёл.",
               Map.of("stress", -25, "health", +10, "energy", -20));
       }
   }
   ```

2. **Добавить константу** в `StandardActionType`.

3. **Зарегистрировать** в `Actions` (фабрика) или `ActionProvider` (Spring-бин).

4. **Написать тест:**
   ```java
   @Test
   void yoga_reducesStress() {
       GameSession session = TestGameSession.withEnergy(50);
       ActionResult result = new YogaAction().execute(session);
       assertThat(result.statsDelta()).containsEntry("stress", -25);
   }
   ```

---

## Как добавить нового NPC

1. Создать `NpcCode` константу (enum или sealed interface).
2. При создании новой сессии в `GameSession.createNew()` добавить новый `Relationship`:
   ```java
   relationships.put("sister", new Relationship(UUID.randomUUID().toString(),
       "sister", 50, 50, 60, 0));
   ```
3. Создать действие взаимодействия (см. выше): `VisitSisterAction`.
4. Добавить `SisterNpcCode` в конфиги событий/конфликтов при необходимости.

---

## Как добавить новый конфликт

1. Создать `ConflictDefinition` — описание с тактиками и последствиями.
2. Зарегистрировать в `ConflictRegistry`.
3. Добавить триггер: условие, при котором конфликт появляется (в `EndDayService`
   или `ExecutePlayerActionService` — при определённых значениях статов или тегах).
4. Покрыть тестами ветки тактик.

---

## Тестирование

### Стратегия

| Уровень       | Что тестируем                            | Инструменты         |
|---------------|------------------------------------------|---------------------|
| Unit          | Действия, сервисы, доменные правила      | JUnit 5, Mockito    |
| Integration   | REST API end-to-end                      | MockMvc, @SpringBootTest |
| Acceptance    | Полный игровой сценарий                  | @SpringBootTest     |

### Пример unit-теста сервиса

```java
@ExtendWith(MockitoExtension.class)
class ExecutePlayerActionServiceTest {

    @Mock SessionRepository sessionRepository;
    @Mock ActionProvider actionProvider;

    ExecutePlayerActionService service;

    @BeforeEach
    void setUp() {
        service = new ExecutePlayerActionService(sessionRepository, actionProvider);
    }

    @Test
    void executeAction_reducesEnergyByActionCost() {
        GameSession session = TestGameSession.withEnergy(80);
        when(sessionRepository.findByTelegramUserId("u1"))
            .thenReturn(Optional.of(session));
        when(actionProvider.findById("jogging")).thenReturn(new JoggingAction());

        service.execute(new ExecuteActionCommand("u1", "jogging"));

        assertThat(session.getPlayer().getStats().getEnergy()).isLessThan(80);
    }
}
```

### Запуск тестов

```bash
# Все тесты
mvn test -pl backend

# Только unit-тесты (без интеграционных)
mvn test -pl backend -Dgroups=unit

# С покрытием
mvn verify -pl backend -P ci
```
