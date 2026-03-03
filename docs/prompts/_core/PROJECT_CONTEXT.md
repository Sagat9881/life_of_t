# Life of T — Контекст проекта

## Обзор

**Life of T** («Жизнь Татьяны») — уютный life-sim симулятор в ромкор-сеттинге,
вдохновлённый механиками Punch Club. Игрок управляет ежедневными делами молодой
женщины Татьяны и пытается найти баланс между работой, семьёй, здоровьем и личным временем.

## Технологический стек

| Слой            | Технология                              |
|-----------------|-----------------------------------------|
| Backend         | Java 21, Spring Boot 3.2, Clean Architecture |
| Frontend        | TypeScript, React 18, Zustand, Vite     |
| Telegram Bot    | telegrambots 6.9, Spring Boot           |
| Asset Generator | Java 2D API, PNG-генерация              |
| CI/CD           | GitHub Actions                          |
| Сборка          | Maven (многомодульный проект)           |

## Структура модулей

```
life-of-t-mvp/
├── pom.xml                  # Родительский POM
├── backend/                 # Бизнес-логика (Clean Architecture)
├── frontend/                # React UI (Vite + TypeScript)
├── asset-generator/         # Генератор спрайтов и текстур
├── application/             # Fat JAR — объединяет все модули
├── telegram-bot/            # Telegram Mini App + бот
├── .github/workflows/       # CI/CD
└── docs/                    # Документация и промпты
```

## Модуль backend

Реализует игровую логику по принципам чистой архитектуры.

### Слои

```
domain/          — доменные модели, бизнес-правила, value objects
application/
  command/       — входящие команды (StartSessionCommand, ExecuteActionCommand…)
  query/         — запросы (GetStateQuery)
  port/
    in/          — входящие порты (use cases)
    out/         — исходящие порты (репозитории, публикаторы событий)
  service/       — реализации use cases
  view/          — DTO для отдачи данных клиенту (GameStateView, StatsView…)
infrastructure/
  persistence/   — in-memory или JPA-репозиторий
  web/           — REST-контроллеры
  config/        — Spring-конфигурация
```

### Ключевые Use Cases

| Use Case                   | Описание                                 |
|----------------------------|------------------------------------------|
| `StartOrLoadSessionUseCase`| Создаёт или загружает игровую сессию     |
| `GetGameStateUseCase`      | Возвращает текущее состояние игры        |
| `ExecutePlayerActionUseCase`| Выполняет выбранное игроком действие     |
| `EndDayUseCase`            | Завершает игровой день                   |
| `ChooseConflictTacticUseCase`| Выбирает тактику в конфликтной ситуации |
| `ChooseEventOptionUseCase` | Выбирает вариант ответа на событие       |

### Доменные модели

- **GameSession** — агрегат игровой сессии, содержит Player, RelationshipMap, время
- **Player** — персонаж: статистики (energy, health, stress, mood, money, selfEsteem), инвентарь, скиллы
- **GameAction** — интерфейс действия с методом `execute(session)`
- **StandardActionType** — перечисление всех базовых действий (GO_TO_WORK, JOGGING, FEED_DUCKS…)

### REST API

```
POST   /api/game/start           — начать/загрузить сессию
GET    /api/game/state           — текущее состояние
POST   /api/game/action          — выполнить действие
POST   /api/game/end-day         — закончить день
POST   /api/game/conflict/tactic — выбрать тактику в конфликте
POST   /api/game/event/choose    — ответить на событие
```

## Модуль frontend

React-приложение, использующее Zustand для управления состоянием.

### Ключевые компоненты

- **App.tsx** — корневой компонент, роутинг
- **GameScreen** — основной экран игры
- **StatsPanel** — панель характеристик персонажа
- **ActionList** — список доступных действий
- **EventModal** — модальное окно случайного события
- **ConflictModal** — модальное окно конфликта

### Zustand Store

```typescript
interface GameStore {
  state: GameStateView | null;
  isLoading: boolean;
  error: string | null;

  startSession: (telegramUserId: string) => Promise<void>;
  loadState: () => Promise<void>;
  executeAction: (actionId: string) => Promise<void>;
  endDay: () => Promise<void>;
}
```

## Модуль asset-generator

Генерирует PNG-ассеты процедурно при первом запуске или по флагу `--generate-assets`.

### Компоненты

- **ProceduralTextureGenerator** — генерирует тайловые текстуры (2×2 клетки, симметрия)
- **LpcSpriteCompositor** — компонует LPC-спрайты из слоёв (body, clothes, hair, accessories)
- **TextureColorPalette** — палитра цветов для текстур (COZY_HOME, PARK, OFFICE…)
- **SymmetryMode** — режимы симметрии (NONE, HORIZONTAL, VERTICAL, QUAD)

## Модуль telegram-bot

Spring Boot приложение с TelegramLongPollingBot.

### Компоненты

- **LifeOfTBot** — основной бот, обрабатывает /start, /help, /stats, /newgame
- **WebAppDataHandler** — обрабатывает данные от Telegram Mini App
- **TelegramGameService** — обёртка над backend use cases для Telegram-контекста
- **BotProperties** — конфигурационный record (token, username, webappUrl)

### Переменные окружения

```
BOT_TOKEN    — токен бота от BotFather (обязательно в продакшене)
BOT_USERNAME — @username бота без символа @
WEBAPP_URL   — публичный URL веб-приложения для Mini App
```

## Модуль application

Fat JAR, объединяющий backend + asset-generator. Фронтенд встраивается как статические ресурсы.

### Запуск

```bash
# Стандартный запуск
java -jar life-of-t-application-0.1.0-SNAPSHOT.jar

# С генерацией ассетов
java -jar life-of-t-application-0.1.0-SNAPSHOT.jar --generate-assets

# Скрипты
./start.sh      # Linux/macOS
start.bat       # Windows
```

## CI/CD

GitHub Actions — три джоба:

1. **build** — компиляция, тесты, сборка JAR
2. **generate-assets** — генерация ассетов из JAR (depends on build)
3. **analyze** — статический анализ и покрытие кода

### Ветки

- `main` — стабильная ветка, полный pipeline
- `dev/**` — ветки разработки, только build

## Конвенции кода

### Java

- Java 21, **без preview-фич**, **без Lombok**
- Записи (records) для неизменяемых DTO
- Тесты: JUnit 5 + AssertJ + Mockito
- Пакет: `ru.lifegame.<модуль>`

### TypeScript/React

- Functional components, React hooks
- CSS Modules для стилей
- Zustand для глобального состояния
- Vite как сборщик

## Глоссарий

| Термин              | Значение                                              |
|---------------------|-------------------------------------------------------|
| Session             | Игровая сессия привязана к telegramUserId             |
| GameStateView       | DTO: полное состояние игры для отдачи клиенту         |
| Action              | Действие персонажа (занимает энергию, меняет статсы)  |
| Conflict            | Сложная ситуация с выбором тактики (work, family...)  |
| Event               | Случайное событие с вариантами ответа                 |
| LPC                 | Liberated Pixel Cup — стандарт пиксельных спрайтов    |
| Ending              | Концовка: наступает при достижении условия победы/пор.|
