# Swagger/OpenAPI документация

## Обзор

Проект Life of T использует Swagger/OpenAPI 3.0 для документирования REST API и генерации клиентского кода.

## Доступ к Swagger UI

После запуска приложения, Swagger UI доступен по адресу:

```
http://localhost:8080/swagger-ui/index.html
```

### OpenAPI JSON спецификация

Для генерации клиентов используйте OpenAPI спецификацию:

```
http://localhost:8080/v3/api-docs
```

### YAML формат

```
http://localhost:8080/v3/api-docs.yaml
```

## API Endpoints

### 1. Начать/Загрузить сессию

**POST** `/api/v1/game/session/start`

Создаёт новую игровую сессию или загружает существующую.

**Request Body:**
```json
{
  "telegramUserId": 123456789
}
```

**Response:** `GameStateView`

---

### 2. Получить состояние игры

**GET** `/api/v1/game/state?telegramUserId={id}`

Возвращает полное состояние игры.

**Parameters:**
- `telegramUserId` (query, required) - ID пользователя

**Response:** `GameStateView`

---

### 3. Выполнить действие

**POST** `/api/v1/game/action`

Выполняет игровое действие.

**Request Body:**
```json
{
  "telegramUserId": 123456789,
  "actionCode": "WORK"
}
```

**Доступные actionCode:**
- `WORK` - Работа
- `DATE_WITH_HUSBAND` - Свидание с мужем
- `FEED_PETS` - Покормить питомцев
- `PLAY_WITH_PETS` - Поиграть с питомцами
- `WALK_PETS` - Выгулять питомцев
- `VISIT_FATHER` - Навестить отца
- `REST` - Отдохнуть
- `SOCIAL_MEDIA` - Соцсети

**Response:** `GameStateView`

---

### 4. Выбрать тактику конфликта

**POST** `/api/v1/game/conflict/tactic`

Применяет тактику разрешения конфликта.

**Request Body:**
```json
{
  "telegramUserId": 123456789,
  "conflictId": "conflict-uuid-123",
  "tacticCode": "SURRENDER"
}
```

**Базовые тактики:**
- `SURRENDER` - Уступить
- `ASSERT` - Настоять на своём
- `COMPROMISE` - Компромисс
- `AVOID` - Избежать

**Навыковые тактики:**
- `LISTEN_AND_UNDERSTAND` - Выслушать и понять (эмпатия 30+)
- `USE_HUMOR` - Пошутить (юмор 20+)
- `LOGICAL_ARGUMENT` - Логический аргумент (риторика 40+)
- `EMOTIONAL_APPEAL` - Эмоциональный призыв (харизма 30+)
- `SET_BOUNDARIES` - Установить границы (ассертивность 50+)

**Response:** `GameStateView`

---

### 5. Выбрать вариант события

**POST** `/api/v1/game/event-choice`

Выбирает вариант ответа на событие.

**Request Body:**
```json
{
  "telegramUserId": 123456789,
  "eventId": "event-uuid-456",
  "optionCode": "option-1"
}
```

**Response:** `GameStateView`

---

## Генерация клиентского кода

### Использование OpenAPI Generator

#### 1. Установка OpenAPI Generator

```bash
npm install @openapitools/openapi-generator-cli -g
```

Или через Docker:

```bash
docker pull openapitools/openapi-generator-cli
```

#### 2. Генерация TypeScript клиента

```bash
openapi-generator-cli generate \
  -i http://localhost:8080/v3/api-docs \
  -g typescript-fetch \
  -o ./generated-client/typescript
```

#### 3. Генерация Java клиента

```bash
openapi-generator-cli generate \
  -i http://localhost:8080/v3/api-docs \
  -g java \
  -o ./generated-client/java \
  --additional-properties=library=resttemplate
```

#### 4. Генерация Python клиента

```bash
openapi-generator-cli generate \
  -i http://localhost:8080/v3/api-docs \
  -g python \
  -o ./generated-client/python
```

### Поддерживаемые генераторы

- `typescript-fetch` - TypeScript with Fetch API
- `typescript-axios` - TypeScript with Axios
- `javascript` - JavaScript
- `java` - Java (RestTemplate, WebClient, etc.)
- `kotlin` - Kotlin
- `python` - Python
- `go` - Go
- `csharp` - C#
- `php` - PHP
- `ruby` - Ruby
- `swift5` - Swift 5

Полный список: https://openapi-generator.tech/docs/generators

---

## Тестирование через Swagger UI

1. Откройте http://localhost:8080/swagger-ui/index.html
2. Выберите endpoint
3. Нажмите "Try it out"
4. Заполните параметры
5. Нажмите "Execute"

### Пример последовательности тестов:

1. **Начать сессию:** POST `/session/start`
2. **Проверить состояние:** GET `/state`
3. **Выполнить действие:** POST `/action` с `actionCode=WORK`
4. **Проверить изменения:** GET `/state`

---

## Структура GameStateView

```typescript
interface GameStateView {
  sessionId: string;
  telegramUserId: string;
  player: PlayerView;
  relationships: RelationshipView[];
  pets: PetView[];
  time: TimeView;
  availableActions: ActionOptionView[];
  activeQuests: QuestView[];
  completedQuestIds: string[];
  activeConflicts: ConflictView[];
  currentEvent: EventView | null;
  ending: EndingView | null;
  lastActionResult: ActionResultView | null;
}
```

### PlayerView

```typescript
interface PlayerView {
  id: string;
  name: string;
  stats: StatsView;
  job: JobView;
  location: string;
  tags: string[];
  skills: Record<string, number>;
  inventory: string[];
}
```

### StatsView

```typescript
interface StatsView {
  energy: number;      // 0-100
  health: number;      // 0-100
  stress: number;      // 0-100
  mood: number;        // 0-100
  money: number;       // рубли
  selfEsteem: number;  // 0-100
}
```

---

## Настройка в application.properties

```properties
# Swagger UI path
springdoc.swagger-ui.path=/swagger-ui.html

# API docs path
springdoc.api-docs.path=/v3/api-docs

# Enable Swagger UI
springdoc.swagger-ui.enabled=true

# Display request duration
springdoc.swagger-ui.displayRequestDuration=true

# Sort operations by method
springdoc.swagger-ui.operationsSorter=method

# Sort tags alphabetically
springdoc.swagger-ui.tagsSorter=alpha
```

---

## Полезные ссылки

- [Swagger UI Documentation](https://swagger.io/docs/open-source-tools/swagger-ui/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [OpenAPI Generator](https://openapi-generator.tech/)
- [SpringDoc Documentation](https://springdoc.org/)
