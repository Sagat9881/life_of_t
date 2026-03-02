# ⚠️ DEPRECATED MODULE

**Этот модуль устарел и заменён на `lpc-sprite-compositor`**

## Почему устарел?

❌ **Selenium** — медленный, нестабильный, требует браузер  
❌ **Сетевые вызовы** — зависимость от внешнего API  
❌ **Flaky тесты** — проблемы в CI/CD  

## Замена

✅ **lpc-sprite-compositor** — локальная композиция PNG  
✅ **Без браузера** — чистый Java BufferedImage  
✅ **Быстро** — <100ms вместо 5-10s  
✅ **Стабильно** — никаких flaky тестов  

## Миграция

Старый код:
```java
// Selenium подход (DEPRECATED)
LPCGenerator generator = new LPCGenerator();
generator.openBrowser();
generator.selectOptions(...);
byte[] png = generator.downloadSprite();
```

Новый код:
```java
// Локальный композитор
CharacterConfig config = CharacterConfig.builder()
    .gender("female")
    .body("light")
    .hair("brown")
    .build();

LPCSpriteCompositor compositor = new LPCSpriteCompositor(spritesheetsPath);
BufferedImage sprite = compositor.generateSprite(config);
```

## Документация

См. [lpc-sprite-compositor/README.md](../lpc-sprite-compositor/README.md)

---

**Этот модуль будет удалён в следующей мажорной версии.**