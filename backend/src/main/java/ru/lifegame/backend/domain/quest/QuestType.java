package ru.lifegame.backend.domain.quest;

public enum QuestType {
    CAREER_GROWTH("Карьерный рост", "Добиться повышения на работе"),
    SELF_CARE_ARC("Забота о себе", "Научиться заботиться о себе"),
    FAMILY_HARMONY("Семейная гармония", "Укрепить семейные отношения");

    private final String label;
    private final String description;

    QuestType(String label, String description) {
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
