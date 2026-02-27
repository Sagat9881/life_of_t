# Migration to Multi-Module Structure

## Обзор изменений

Проект реорганизован в multi-module Maven структуру:

```
life_of_t/
├── pom.xml                 # Root POM (parent)
├── backend/                # Backend module
│   ├── pom.xml
│   └── src/main/java/ru/lifegame/backend/*
├── frontend/               # Frontend module
│   ├── pom.xml
│   └── (React app)
└── application/            # Application module
    ├── pom.xml
    └── src/main/java/ru/lifegame/Application.java
```

---

## Что нужно сделать

### 1. Переместить backend код

**Текущая структура**:
```
src/main/java/ru/lifegame/backend/*
```

**Новая структура**:
```
backend/src/main/java/ru/lifegame/backend/*
```

**Команды**:
```bash
# Создать директорию backend/src
mkdir -p backend/src/main/java
mkdir -p backend/src/main/resources
mkdir -p backend/src/test/java

# Переместить код
mv src/main/java/ru backend/src/main/java/
mv src/main/resources/* backend/src/main/resources/ 2>/dev/null || true
mv src/test backend/src/test 2>/dev/null || true

# Удалить старую структуру
rm -rf src
```

### 2. Обновить импорты (если нужно)

Пакеты остаются прежними: `ru.lifegame.backend.*`

Никаких изменений в коде не требуется!

### 3. Сборка проекта

**Root level**:
```bash
mvn clean install
```

Это соберёт все 3 модуля:
1. `backend` → JAR с backend кодом
2. `frontend` → JAR со статикой (dist/)
3. `application` → Исполняемый JAR с backend + frontend

### 4. Запуск

**Вариант 1: Через Maven**
```bash
cd application
mvn spring-boot:run
```

**Вариант 2: Через JAR**
```bash
java -jar application/target/life-of-t.jar
```

**Вариант 3: Docker**
```bash
docker-compose up --build
```

---

## Как это работает

### Backend module
- Содержит весь игровой код
- REST API controllers
- Domain logic
- Собирается в JAR
- Не запускается самостоятельно

### Frontend module
- React + TypeScript приложение
- Собирается через `frontend-maven-plugin`:
  1. Устанавливает Node.js
  2. Запускает `npm install`
  3. Запускает `npm run build`
  4. Копирует `dist/` в `target/classes/static/`
- Собирается в JAR со статикой
- Не запускается самостоятельно

### Application module
- Главный модуль-лаунчер
- Зависит от backend и frontend
- Содержит `@SpringBootApplication`
- Включает оба JAR'а в финальную сборку
- Spring Boot автоматически:
  - Запускает backend API
  - Раздаёт frontend статику из `/static/`
  - Настраивает роутинг

---

## URL Structure

После запуска:

- **Frontend**: `http://localhost:8080/` → React приложение
- **Backend API**: `http://localhost:8080/api/v1/game/*` → REST API
- **Swagger UI**: `http://localhost:8080/swagger-ui.html` → API документация
- **Health**: `http://localhost:8080/actuator/health` → Health check

---

## CI/CD изменения

### GitHub Actions

```yaml
- name: Build with Maven
  run: mvn clean install
  
- name: Run tests
  run: mvn test
  
- name: Build Docker image
  run: docker build -t life-of-t .
```

### Dockerfile

Обновить для multi-module:

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy all POMs
COPY pom.xml .
COPY backend/pom.xml backend/
COPY frontend/pom.xml frontend/
COPY application/pom.xml application/

# Copy source code
COPY backend/src backend/src
COPY frontend frontend/
COPY application/src application/src

# Build
RUN mvn clean package -DskipTests

# Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/application/target/life-of-t.jar .

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "life-of-t.jar"]
```

---

## Преимущества новой структуры

✅ **Разделение ответственности**
- Backend — чистая бизнес-логика
- Frontend — чистый UI
- Application — только запуск

✅ **Независимая разработка**
- Backend разработчики работают в `backend/`
- Frontend разработчики работают в `frontend/`
- Нет конфликтов

✅ **Переиспользование**
- Backend модуль можно использовать в других проектах
- Frontend можно собирать отдельно

✅ **Упрощённая сборка**
- Один `mvn clean install` собирает всё
- Один JAR содержит всё необходимое

✅ **Лучше для Docker**
- Кэширование слоёв
- Быстрее пересборка

---

## Проверка

```bash
# 1. Сборка
mvn clean install

# 2. Проверка структуры JAR'ов
jar -tf backend/target/backend-0.1.0-SNAPSHOT.jar | head
jar -tf frontend/target/frontend-0.1.0-SNAPSHOT.jar | head
jar -tf application/target/life-of-t.jar | head

# 3. Проверка статики во frontend JAR
jar -tf frontend/target/frontend-0.1.0-SNAPSHOT.jar | grep static

# 4. Запуск
java -jar application/target/life-of-t.jar

# 5. Проверка эндпоинтов
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/v1/game/state?telegramUserId=12345
```

---

## Troubleshooting

### Frontend не собирается

**Проблема**: `npm install` или `npm run build` падает

**Решение**:
```bash
cd frontend
npm install
npm run build
```

Если работает локально, Maven тоже соберёт.

### Статика не раздаётся

**Проблема**: 404 на `/`

**Решение**: Проверить, что frontend JAR содержит `/static/`:
```bash
jar -tf frontend/target/frontend-0.1.0-SNAPSHOT.jar | grep static
```

Должны быть файлы вроде:
```
static/index.html
static/assets/index.js
static/assets/index.css
```

### Backend API не работает

**Проблема**: 404 на `/api/v1/game/*`

**Решение**: Проверить, что backend модуль подключён:
```bash
jar -tf application/target/life-of-t.jar | grep "ru/lifegame/backend"
```

---

**Следующий шаг**: Выполнить миграцию кода согласно инструкциям выше.
