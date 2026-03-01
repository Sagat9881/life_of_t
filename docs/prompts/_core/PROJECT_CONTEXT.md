# Life of T - Контекст проекта

## Описание проекта

**Life of T** — это симулятор жизни, вдохновлённый игрой Punch Club, разработанный как подарок для Тани. Игра моделирует повседневную жизнь главной героини, её отношения с близкими, управление ресурсами и принятие решений.

### Целевая аудитория
- **Основной игрок**: Таня
- **Платформа**: Telegram Mini App (WebApp)
- **Жанр**: Life Simulation, Visual Novel elements
- **Стиль**: Романтичный, лёгкий, с юмором

---

## Технический стек

### Backend
- **Язык**: Java 21
- **Фреймворк**: Spring Boot 3.2.x
- **Архитектура**: Clean Architecture (Domain, Application, Infrastructure)
- **База данных**: H2 (in-memory для MVP)
- **API**: REST JSON
- **Сборка**: Maven Multi-Module

### Frontend
- **Язык**: TypeScript
- **Фреймворк**: React 18 + Vite
- **Стейт**: Zustand
- **Стили**: CSS Modules + CSS Variables
- **UI**: Custom components (без внешних библиотек)
- **Интеграция**: Telegram WebApp API
- **Сборка**: frontend-maven-plugin

### Deployment
- **Упаковка**: Spring Boot JAR с встроенным frontend
- **Demo**: Desktop executable через jpackage

---

## Статус разработки (01.03.2026, 22:58 MSK)

### ✅ Готово

#### Backend
- **Domain модели**: Player, NPC, Pet, Action, Conflict, Event
- **Application Use Cases**: ExecuteAction, ResolveConflict, SelectChoice
- **Infrastructure**: REST API, H2 database
- **GameSimulationService** с временной системой
- **17 зарегистрированных действий**:
  - 7 базовых (work, visit_father, date_husband, play_cat, walk_dog, self_care, rest_at_home)
  - 4 RoomPage (call_husband, watch_tv, play_with_pet, + rest_at_home)
  - 3 OfficePage (work_on_project, make_coffee, talk_to_colleague)
  - 4 ParkPage (rest_on_bench, feed_ducks, jogging, walk_dog_park)

#### Frontend
- **Shared компоненты**: Button, Card, StatBar, LoadingSpinner, ErrorMessage
- **Layout**: AppLayout, BottomNav (с роутингом!)
- **Game компоненты**:
  - PlayerPanel (панель игрока)
  - ActionCard, ActionList (действия)
  - NPCCard, PetCard, RelationshipList (отношения)
  - TacticCard, ConflictResolver (конфликты)
  - ChoiceButton, EventChoice (события)
- **Pages (ВСЕ 11 РОУТОВ РАБОТАЮТ)**:
  - HomePage (главная)
  - RoomPage (комната с изометрией)
  - OfficePage, ParkPage (с объектами)
  - ActionsPage, RelationshipsPage, StatsPage
  - PetsPage, QuestsPage, ProfilePage, EndingPage
  - BackgroundTest
- **Navigation**: BottomNav с 4 кнопками (Room, Actions, Relationships, Stats)
- **Store**: gameStore (Zustand) с quests
- **Services**: API client
- **Types**: Полные TypeScript типы
- **Styles**: CSS Variables, все компоненты стилизованы

#### Infrastructure
- Maven multi-module структура
- Frontend интеграция
- Demo приложение
- ComponentTest.tsx

### 🔄 Следующие шаги (приоритеты)

1. **Game Loop & Time Display** (30 мин)
   - Отображение текущего времени в UI
   - Кнопка "End Day" для завершения дня
   - Автоматическое продвижение времени

2. **Content Filling**
   - Добавить реальные конфликты
   - Добавить реальные события
   - Квесты и достижения

3. **Polish**
   - Анимации персонажа
   - Сохранение/загрузка игры
   - Telegram WebApp deployment

### 📋 Запланировано
- Система достижений
- Музыка и звуковые эффекты
- Разные концовки игры

---

## Важные файлы

- `docs/prompts/PROJECT_CONTEXT.md` — этот файл
- `docs/TODO.md` — список задач
- `docs/prompts/FRONTEND_SYSTEM_PROMPT.md` — промпт для фронтенда
- `frontend/src/ComponentTest.tsx` — демо всех компонентов
- `frontend/src/store/gameStore.ts` — глобальное состояние
- `frontend/src/App.tsx` — роутер
- `backend/.../GameSimulationService.java` — симуляция
- `backend/.../DomainConfig.java` — регистрация действий

---

**Дата обновления**: 01 марта 2026, 22:58 MSK  
**Версия**: 0.1.0-SNAPSHOT (MVP)  
**Автор**: Александр Захаров
