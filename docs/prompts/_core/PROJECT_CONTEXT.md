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

## Статус разработки (01.03.2026)

### ✅ Готово

#### Backend
- Domain модели (Player, NPC, Pet, Action, Conflict, Event)
- Application Use Cases (ExecuteAction, ResolveConflict, SelectChoice)
- Infrastructure (REST API, H2 database)
- GameSimulationService с временной системой

#### Frontend
- **Shared компоненты**: Button, Card, StatBar, LoadingSpinner, ErrorMessage
- **Layout**: AppLayout, BottomNav
- **Game компоненты**:
  - PlayerPanel (панель игрока)
  - ActionCard, ActionList (действия)
  - NPCCard, PetCard, RelationshipList (отношения)
  - TacticCard, ConflictResolver (конфликты)
  - ChoiceButton, EventChoice (события)
  - Character (персонаж с PixiJS анимацией)
- **Pages (ВСЕ РОУТЫ РАБОТАЮТ!)**:
  - HomePage (главная с действиями/конфликтами/событиями)
  - RoomPage (комната Татьяны с изометрией)
  - OfficePage (офис с объектами)
  - ParkPage (парк с объектами)
  - ActionsPage (список действий)
  - RelationshipsPage (отношения)
  - StatsPage (детальная статистика)
  - PetsPage (управление питомцами)
  - QuestsPage (квесты)
  - ProfilePage (профиль)
  - EndingPage (экран концовки)
  - BackgroundTest (тест фонов)
- **Routing**: React Router v6 с полной навигацией
- **Store**: gameStore (Zustand) с поддержкой quests
- **Services**: API client
- **Types**: Полные TypeScript типы (Job, Quest, Pet.species)
- **Styles**: CSS Variables, все компоненты стилизованы

#### Infrastructure
- Maven multi-module структура
- Frontend интеграция через frontend-maven-plugin
- Demo приложение с desktop launcher
- ComponentTest.tsx для визуальной проверки

### 🔄 Следующие шаги (приоритеты)

1. **Bottom Navigation Integration**
   - Добавить BottomNav на все основные страницы
   - Переключение между: Room, Actions, Relationships, Stats

2. **Backend Actions Registration**
   - Зарегистрировать действия из RoomPage (CALL_HUSBAND, REST_AT_HOME, и т.д.)
   - Добавить действия из OfficePage и ParkPage

3. **Game Loop & State Management**
   - Автоматическое продвижение времени
   - Кнопка "End Day" для завершения дня

4. **Content Filling**
   - Добавить реальные действия, конфликты, события
   - Квесты и достижения

### 📋 Запланировано
- Сохранение/загрузка игры
- Система достижений
- Telegram WebApp deployment
- Анимации персонажа (полный набор эмоций)

---

## Важные файлы

- `docs/prompts/PROJECT_CONTEXT.md` — этот файл
- `docs/prompts/FRONTEND_SYSTEM_PROMPT.md` — промпт для фронтенда
- `frontend/src/ComponentTest.tsx` — демо всех компонентов
- `frontend/src/store/gameStore.ts` — глобальное состояние игры
- `frontend/src/App.tsx` — роутер с всеми страницами
- `application/src/main/java/.../GameSimulationService.java` — симуляция игры

---

**Дата обновления**: 01 марта 2026  
**Версия**: 0.1.0-SNAPSHOT (MVP)  
**Автор**: Александр Захаров
