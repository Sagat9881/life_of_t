# Рефакторинг архитектуры проекта

## Дата: 24 февраля 2026

## Обзор изменений

Проект был реорганизован для соответствия принципам чистой архитектуры (Clean Architecture) и Domain-Driven Design (DDD).

## Основные изменения

### 1. Перемещение компонентов презентационного слоя

**Изменение:** `GameStateViewMapper` перемещён из `application.service` в `infrastructure.web.mapper`

**Причина:** Маппинг в веб-представление - это инфраструктурная ответственность, не бизнес-логика

```
Было:  application/service/GameStateViewMapper.java
Стало: infrastructure/web/mapper/GameStateViewMapper.java
```

### 2. Введение принципа инверсии зависимостей для действий

**Изменение:** Создан интерфейс `ActionProvider` в domain слое

**Было:**
- Domain зависел от конкретного класса `GameEngine` в `domain.service`
- Нарушение принципа инверсии зависимостей

**Стало:**
- Domain определяет интерфейс `ActionProvider`
- Infrastructure предоставляет реализацию `GameEngineAdapter`
- Domain не зависит от infrastructure

```
Новый файл: domain/action/ActionProvider.java (интерфейс)
Было:       domain/service/GameEngine.java
Стало:      infrastructure/game/GameEngineAdapter.java
```

### 3. Реорганизация domain services

#### GameEngine → GameEngineAdapter
- **Откуда:** `domain.service.GameEngine`
- **Куда:** `infrastructure.game.GameEngineAdapter`
- **Причина:** Управление реестром действий - инфраструктурная задача

#### ConflictTriggers
- **Откуда:** `domain.service.ConflictTriggers`
- **Куда:** `domain.conflict.ConflictTriggers`
- **Причина:** Лучшая cohesion - находится рядом с другими классами конфликтов

#### GameOverChecker
- **Откуда:** `domain.service.GameOverChecker`
- **Куда:** `domain.model.GameOverChecker`
- **Причина:** Тесно связан с жизненным циклом GameSession

#### EndingEvaluator
- **Осталось:** `domain.ending.EndingEvaluator`
- **Причина:** Уже находился в правильном пакете

### 4. Добавлены недостающие сервисы приложения

**Новые классы:**
- `application/service/ChooseConflictTacticService.java`
- `application/service/ChooseEventOptionService.java`

**Причина:** Реализация use cases, используемых контроллером

### 5. Улучшен EventPublisher

**Изменение:** Извлечён в отдельный класс

```
Было:  Лямбда в InfrastructureConfig
Стало: infrastructure/event/LoggingEventPublisher.java
```

**Преимущества:**
- Легко заменить на реальную реализацию (Kafka, RabbitMQ)
- Лучшая тестируемость
- Явная документация поведения

## Новая структура пакетов

```
src/main/java/ru/lifegame/backend/
├── domain/
│   ├── action/
│   │   ├── ActionProvider.java          [NEW] - интерфейс
│   │   ├── GameAction.java
│   │   └── impl/
│   ├── conflict/
│   │   ├── Conflict.java
│   │   ├── ConflictTriggers.java        [MOVED] <- domain.service
│   │   └── ...
│   ├── model/
│   │   ├── GameSession.java
│   │   ├── GameOverChecker.java         [MOVED] <- domain.service
│   │   └── ...
│   ├── ending/
│   │   ├── EndingEvaluator.java         [UNCHANGED]
│   │   └── ...
│   └── service/                          [УДАЛЁН]
├── application/
│   ├── service/
│   │   ├── ExecutePlayerActionService.java
│   │   ├── ChooseConflictTacticService.java  [NEW]
│   │   ├── ChooseEventOptionService.java     [NEW]
│   │   └── ...
│   └── port/
├── infrastructure/
│   ├── game/
│   │   └── GameEngineAdapter.java       [NEW] <- domain.service.GameEngine
│   ├── event/
│   │   └── LoggingEventPublisher.java   [NEW]
│   ├── web/
│   │   ├── mapper/
│   │   │   └── GameStateViewMapper.java [MOVED] <- application.service
│   │   └── controller/
│   └── config/
```

## Принципы, которым следует код

### 1. Dependency Inversion Principle (DIP)
- Domain определяет интерфейсы (порты)
- Infrastructure предоставляет реализации
- Domain НЕ зависит от infrastructure

### 2. Single Responsibility Principle (SRP)
- Каждый класс имеет одну причину для изменения
- Маппинг отделён от бизнес-логики
- Публикация событий изолирована

### 3. Interface Segregation Principle (ISP)
- `ActionProvider` - минимальный интерфейс
- Клиенты зависят только от используемых методов

### 4. Domain-Driven Design
- Domain services сгруппированы по субдоменам
- Лучшая cohesion внутри пакетов
- Явные границы агрегатов

## Преимущества новой структуры

1. **Тестируемость**
   - Можно мокировать `ActionProvider`
   - Domain тестируется без infrastructure

2. **Гибкость**
   - Легко заменить `GameEngineAdapter` другой реализацией
   - Можно добавить кеширование, валидацию и т.д.

3. **Понятность**
   - Явная структура зависимостей
   - Каждый пакет имеет чёткую ответственность

4. **Maintainability**
   - Изменения в infrastructure не затрагивают domain
   - Легко найти, где находится нужная логика

## Что дальше?

### Следующие этапы рефакторинга:

1. **Выделение агрегатов**
   - Создать подпакеты для агрегатов в domain.model
   - Явно определить границы агрегатов

2. **Улучшение репозиториев**
   - Разделить `SessionRepository` и `SessionPersistence`
   - Переместить интерфейс репозитория в domain

3. **Добавление валидации**
   - Value Objects с валидацией в конструкторах
   - Invariants в агрегатах

4. **Документация**
   - package-info.java для каждого пакета
   - Javadoc для публичных API

## Обратная совместимость

Все изменения обратно совместимы с точки зрения API:
- REST endpoints не изменились
- Сигнатуры Use Cases остались прежними
- Поведение приложения идентично

Изменилась только внутренняя структура и зависимости.
