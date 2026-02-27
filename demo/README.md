# 🎮 Life of T - Component Demo

Интерактивное демо-приложение для разработчиков и дизайнеров, демонстрирующее все UI компоненты Life of T.

---

## 🚀 Быстрый старт

### Windows (.exe)

```bash
# Просто двойной клик по файлу:
demo/target/life-of-t-demo.exe
```

### JAR (Linux/Mac/Windows)

```bash
java -jar demo/target/life-of-t-demo.jar
```

### Maven

```bash
cd demo
mvn spring-boot:run
```

**Автоматически откроется**: `http://localhost:3000`

---

## 🎨 Что внутри

### UI Компоненты

#### 1. **Button**
- 4 варианта: Primary, Secondary, Accent, Outline
- 3 размера: Small, Medium, Large
- Состояния: Loading, Disabled
- Haptic feedback при клике

#### 2. **Card**
- 3 варианта: Default, Elevated, Outlined
- 4 размера padding: None, Small, Medium, Large
- Кликабельные карточки

#### 3. **StatBar**
- 6 типов статов: Energy, Health, Stress, Mood, Money, Self-Esteem
- Динамические цвета (зелёный → жёлтый → красный)
- Плавная анимация заполнения

#### 4. **LoadingSpinner**
- 3 размера: Small, Medium, Large
- Опциональный текст

#### 5. **ErrorMessage**
- Иконка AlertCircle
- Кнопка "Повторить"

#### 6. **BottomNav**
- 3 вкладки: Главная, Действия, Отношения
- Active state с розовым цветом
- Safe area для iPhone notch

---

## 🎨 Design System

### Цветовая палитра

```css
--color-primary: #FF6B9D    /* 🌸 Розовый */
--color-secondary: #4ECDC4  /* 🌿 Мятный */
--color-accent: #FFE66D     /* ☀️ Жёлтый */
--color-background: #F7F7F7 /* ☁️ Светло-серый */
--color-text: #2C3E50      /* 💬 Тёмно-синий */
```

### Типографика

- **Заголовки**: Comfortaa / Nunito (округлые, уютные)
- **Текст**: Inter (читаемый, современный)

### Анимации

- **Переходы**: 200-300ms
- **Easing**: `ease` / `cubic-bezier(0.4, 0, 0.2, 1)`
- **Scale**: `0.98` на active
- **Hover**: Увеличение тени, затемнение цвета

---

## 💻 Для разработчиков

### Сборка demo

```bash
# Из корня проекта
mvn clean install

# Или только demo модуль
cd demo
mvn clean package
```

**Результат**:
- `demo/target/life-of-t-demo.jar` — JAR файл
- `demo/target/life-of-t-demo.exe` — Windows EXE (только на Windows)

### Использование компонентов

```tsx
import { Button } from './components/shared/Button';
import { Card } from './components/shared/Card';
import { StatBar } from './components/shared/StatBar';

function MyComponent() {
  return (
    <Card variant="elevated" padding="medium">
      <h2>Пример</h2>
      <StatBar statKey="energy" value={75} />
      <Button variant="primary" onClick={() => alert('Clicked!')}>
        Кликни меня!
      </Button>
    </Card>
  );
}
```

---

## 📦 Структура модуля

```
demo/
├── pom.xml                    # Maven конфигурация
├── README.md                  # Этот файл
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ru/lifegame/demo/
│   │   │       └── DemoApplication.java
│   │   └── resources/
│   │       └── application.yml
└── target/
    ├── life-of-t-demo.jar     # JAR файл
    └── life-of-t-demo.exe     # Windows EXE
```

---

## ✨ Особенности

- ✅ **Автозапуск браузера** — приложение автоматически открывает `http://localhost:3000`
- ✅ **Windows EXE** — двойной клик для запуска
- ✅ **Кросс-платформенность** — JAR работает на Windows/Mac/Linux
- ✅ **No cache** — изменения видны сразу

---

## 🔧 Требования

- **Java 21+**
- **Maven 3.8+**
- **Windows** (для .exe файла)

---

## 📝 License

Внутренний демо-проект для Life of T.
