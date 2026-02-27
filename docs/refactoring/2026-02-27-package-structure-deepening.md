# Package Structure Deepening: Conflict & Event

**Date**: 2026-02-27  
**Author**: Александр Захаров  
**Branch**: `refactor/2026-02-27-core-simulation`

## Problem

Пакеты `conflict` и `event` содержали слишком много файлов в одном уровне, что затрудняло навигацию и понимание структуры:

- `conflict/` — 18 файлов на одном уровне
- `event/` — 14 файлов на одном уровне

Это нарушало принцип **организации кода по доменным концепциям** и делало структуру плоской.

---

## Solution: Глубокая субпакетная структура

### Новая структура conflict

```
conflict/
├── core/                  # Основные entity и value objects
│   ├── Conflict.java
│   ├── ConflictType.java
│   ├── ConflictStage.java
│   ├── ConflictCategory.java
│   ├── ConflictOutcome.java
│   ├── ConflictStressPoints.java
│   ├── CspChanges.java
│   ├── ConflictRound.java
│   └── ConflictResolution.java
├── tactics/               # Тактики конфликтов
│   ├── ConflictTactic.java (interface)
│   ├── TacticEffects.java
│   ├── BaseConflictTactics.java
│   └── SkillBasedConflictTactics.java
├── types/                 # Типы конфликтов (enums)
│   ├── HusbandConflicts.java
│   ├── FatherConflicts.java
│   ├── InternalConflicts.java
│   └── DynamicConflict.java
└── triggers/              # Триггеры конфликтов (уже существовал)
    ├── ConflictTrigger.java (interface)
    ├── BurnoutTrigger.java
    ├── GuiltTrigger.java
    ├── IdentityCrisisTrigger.java
    ├── FatherNeglectedTrigger.java
    ├── FatherConcernTrigger.java
    ├── FatherCriticismTrigger.java
    ├── LackOfAttentionTrigger.java
    ├── RomanticCrisisTrigger.java
    ├── HouseholdDutiesTrigger.java
    └── FinancialDisagreementTrigger.java
```

### Новая структура event

```
event/
├── domain/                # Domain events (для Event Sourcing)
│   ├── DomainEvent.java (interface)
│   ├── ActionExecutedEvent.java
│   ├── ConflictResolvedEvent.java
│   ├── ConflictTacticAppliedEvent.java
│   ├── ConflictTriggeredEvent.java
│   ├── DayEndedEvent.java
│   ├── EndingAchievedEvent.java
│   ├── EventTriggeredEvent.java
│   ├── GameOverEvent.java
│   ├── QuestProgressUpdatedEvent.java
│   └── RelationshipBrokenEvent.java
└── game/                  # Game events (игровые события для игрока)
    ├── GameEvent.java
    ├── EventOption.java
    ├── EventResult.java
    └── EventStatus.java
```

---

## Design Decisions

### 1. conflict/core
**Цель**: Центральные сущности конфликтной системы  
**Содержит**: Entity (`Conflict`), интерфейсы (`ConflictType`), value objects (`ConflictStressPoints`, `CspChanges`, `ConflictRound`)

### 2. conflict/tactics
**Цель**: Инкапсуляция логики тактик  
**Содержит**: Интерфейс `ConflictTactic`, реализации (`BaseConflictTactics`, `SkillBasedConflictTactics`), результаты (`TacticEffects`)

### 3. conflict/types
**Цель**: Каталог типов конфликтов  
**Содержит**: Enums для семейных конфликтов (`HusbandConflicts`, `FatherConflicts`), внутренних (`InternalConflicts`), динамических (`DynamicConflict`)

### 4. conflict/triggers
**Цель**: Условия возникновения конфликтов  
**Содержит**: Интерфейс `ConflictTrigger`, конкретные реализации триггеров

### 5. event/domain
**Цель**: События для Event Sourcing и интеграции  
**Содержит**: Domain events для отслеживания изменений состояния игры

### 6. event/game
**Цель**: Игровые события для взаимодействия с игроком  
**Содержит**: Entity `GameEvent`, опции выбора, результаты, статусы

---

## Benefits

### 1. **Навигация**
Теперь легко найти нужный класс:
- Ищешь тактику? → `conflict/tactics/`
- Ищешь триггер? → `conflict/triggers/`
- Ищешь domain event? → `event/domain/`

### 2. **Cohesion**
Каждый пакет содержит классы с высокой связанностью:
- `conflict/core` — основные концепции конфликтов
- `conflict/tactics` — только тактики и их эффекты
- `event/domain` vs `event/game` — четкое разделение технических и игровых событий

### 3. **Расширяемость**
Добавление нового типа конфликта:
1. Создай enum в `conflict/types/`
2. Добавь триггеры в `conflict/triggers/`
3. Готово!

### 4. **Clean Architecture**
Ясное разделение:
- **Core** — доменные сущности
- **Tactics** — бизнес-логика
- **Types** — конфигурация
- **Triggers** — правила активации

---

## Import Updates

Все новые файлы используют правильные импорты:

```java
// OLD (плоская структура)
import ru.lifegame.backend.domain.conflict.*;

// NEW (глубокая структура)
import ru.lifegame.backend.domain.conflict.core.*;
import ru.lifegame.backend.domain.conflict.tactics.*;
import ru.lifegame.backend.domain.conflict.types.*;
import ru.lifegame.backend.domain.conflict.triggers.*;
```

```java
// Event package
import ru.lifegame.backend.domain.event.domain.*;  // Domain events
import ru.lifegame.backend.domain.event.game.*;    // Game events
```

---

## Migration

### Старые файлы (должны быть удалены вручную):

**conflict/** (корень):
- ❌ Conflict.java → ✅ conflict/core/Conflict.java
- ❌ ConflictType.java → ✅ conflict/core/ConflictType.java
- ❌ ConflictTactic.java → ✅ conflict/tactics/ConflictTactic.java
- ❌ BaseConflictTactics.java → ✅ conflict/tactics/BaseConflictTactics.java
- ❌ HusbandConflicts.java → ✅ conflict/types/HusbandConflicts.java
- ❌ FatherConflicts.java → ✅ conflict/types/FatherConflicts.java
- ❌ InternalConflicts.java → ✅ conflict/types/InternalConflicts.java
- ❌ DynamicConflict.java → ✅ conflict/types/DynamicConflict.java
- ❌ ConflictTrigger.java → ✅ conflict/triggers/ConflictTrigger.java (обновить package)
- ❌ ConflictTriggers.java (coordinator) — обновить импорты
- ❌ Все остальные в conflict/

**event/** (корень):
- ❌ DomainEvent.java → ✅ event/domain/DomainEvent.java
- ❌ ActionExecutedEvent.java → ✅ event/domain/ActionExecutedEvent.java
- ❌ GameEvent.java → ✅ event/game/GameEvent.java
- ❌ EventOption.java → ✅ event/game/EventOption.java
- ❌ Все остальные в event/

---

## Commits

1. [f96dc28](https://github.com/Sagat9881/life_of_t/commit/f96dc28d6c00314597ada78fa7c9849cedc6b667) — Create conflict/core subpackage (9 files)
2. [88bdeb6](https://github.com/Sagat9881/life_of_t/commit/88bdeb6c712f5e1c34900bd2d11304053288cd11) — Create conflict/tactics subpackage (3 files)
3. [bbf7611](https://github.com/Sagat9881/life_of_t/commit/bbf7611e0de25e72a017533d29e151d2d7b5f075) — Create conflict/types subpackage (4 files)
4. [3a794a3](https://github.com/Sagat9881/life_of_t/commit/3a794a3d130354fc329908b74fce77684b7741d8) — Create event/domain & event/game subpackages (15 files)

**Итого**: 4 коммита, 31 новый файл в правильной структуре

---

## Next Steps

1. ✅ Новая структура создана
2. ⏭️ **Вручную удалить старые файлы** из корня `conflict/` и `event/`
3. ⏭️ Обновить импорты в:
   - `ConflictTriggers.java` (coordinator)
   - `GameSession.java`
   - `ActionExecutor.java`
   - `ConflictManager.java`
   - `DayEndProcessor.java`
   - Все остальные файлы, использующие conflict/event классы
4. ⏭️ Компиляция и тестирование

---

## Conclusion

Глубокая субпакетная структура:
- ✅ Улучшает навигацию (4 подпакета вместо 32 файлов на одном уровне)
- ✅ Повышает cohesion (связанные классы вместе)
- ✅ Облегчает расширение (ясно, куда добавлять новые классы)
- ✅ Соответствует Clean Architecture (разделение по слоям и доменам)

**Статус**: ✅ Новые файлы созданы, готово к удалению старых и обновлению импортов.
