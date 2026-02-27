package ru.lifegame.backend.domain.event.game;

public enum GameEventType {
    RELATIONSHIP_MILESTONE("Веха отношений", "Важное событие в отношениях"),
    WORK_OPPORTUNITY("Рабочая возможность", "Новая возможность на работе"),
    FAMILY_EVENT("Семейное событие", "Событие в семье"),
    PERSONAL_GROWTH("Личностный рост", "Событие личного развития"),
    CRISIS("Кризис", "Кризисная ситуация"),
    RANDOM_ENCOUNTER("Случайная встреча", "Неожиданное событие");

    private final String label;
    private final String description;

    GameEventType(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String label() {
        return label;
    }

    public String description() {
        return description;
    }
}
