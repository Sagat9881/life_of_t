# Life of T 🎮

Уютный life-sim симулятор про Татьяну для Telegram Mini App.

## 🚀 Особенности

- ✨ **DDD Архитектура** — Clean Architecture с чёткими границами
- 🎨 **LPC Sprite Generator** — Локальная композиция спрайтов
- ⚡ **React + TypeScript** — Современный фронтенд
- 🛠️ **Spring Boot 3.2** — Надёжный бэкенд
- 📦 **Maven Multi-Module** — Модульная структура

## 📚 Структура проекта

```
life_of_t/
├── backend/                   # Spring Boot бэкенд
├── frontend/                  # React фронтенд
├── assets/                    # Статические ресурсы
├── lpc-sprite-compositor/     # Композитор LPC спрайтов
├── lpc-spritesheets/          # Git submodule с PNG слоями
├── application/               # Spring Boot приложение
└── demo/                      # Demo JAR
```

## 🛠️ Быстрый старт

### Требования

- **Java 21+**
- **Maven 3.8+**
- **Node.js 20+**
- **Git**

### Установка

```bash
# 1. Клонируй с submodules
git clone --recurse-submodules https://github.com/Sagat9881/life_of_t.git
cd life_of_t

# 2. Собери проект
mvn clean install

# 3. Запусти demo
java -jar demo/target/life-of-t-demo.jar

# Или используй скрипт
.\rebuild-and-run.bat  # Windows
./rebuild-demo.sh      # Linux/Mac
```

### Браузер

Открой: http://localhost:8080

## 🎨 LPC Sprite Generator

### Что это?

Локальный композитор PNG-слоёв для генерации спрайтов персонажей.

**Преимущества:**
- ✅ Никаких сетевых запросов
- ✅ Никакой автоматизации браузера
- ✅ Предгенерация в CI/CD
- ✅ Runtime-генерация в Java

### Как использовать?

См. [📝 LPC Setup Guide](docs/LPC_SETUP.md)

### Пример

```java
CharacterConfig tatyana = CharacterConfig.builder()
    .id("tatyana-default")
    .gender("female")
    .body("light")
    .hairStyle("long")
    .hair("brown")
    .clothes(List.of("shirt-white", "pants-jeans"))
    .build();

LPCSpriteCompositor compositor = new LPCSpriteCompositor(spritesheetsPath);
BufferedImage sprite = compositor.generateSprite(tatyana);
compositor.saveSprite(sprite, Paths.get("tatyana.png"));
```

## 💻 Разработка

### Backend

```bash
cd backend
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

### Тесты

```bash
mvn test
```

## 📦 Сборка production

```bash
mvn clean package -Pproduction

# JAR в: application/target/life-of-t-application.jar
```

## 📚 Документация

- [📝 LPC Setup Guide](docs/LPC_SETUP.md)
- [📝 Migration Guide](MIGRATION.md)
- [📝 Refactoring Log](REFACTORING.md)
- [📝 Phase 2 Refactoring](REFACTORING_PHASE2.md)

## 👥 Команда

- **Backend**: Java 21, Spring Boot 3.2, DDD
- **Frontend**: React 18, TypeScript, Vite
- **Sprites**: LPC Generator (CC-BY-SA 3.0)

## 📜 Лицензия

- **Код проекта**: MIT
- **LPC Spritesheets**: CC-BY-SA 3.0 / GPL 3.0

## ⭐ Ссылки

- [Universal LPC Generator](https://github.com/sanderfrenken/Universal-LPC-Spritesheet-Character-Generator)
- [Liberated Pixel Cup](https://lpc.opengameart.org/)