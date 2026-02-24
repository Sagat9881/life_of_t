package ru.lifegame.backend.domain.quest;

public record QuestStepState(
        QuestObjective objective,
        int currentCount
) {
    public boolean isCompleted() {
        return currentCount >= objective.required();
    }

    public QuestStepState increment() {
        return new QuestStepState(objective, currentCount + 1);
    }

    public QuestStepState addProgress(int delta) {
        return new QuestStepState(objective, currentCount + delta);
    }
}
