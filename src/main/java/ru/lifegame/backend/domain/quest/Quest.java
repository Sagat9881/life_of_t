package ru.lifegame.backend.domain.quest;

import java.util.*;

public class Quest {
    private final String id;
    private final QuestType type;
    private final String title;
    private final String description;
    private QuestStatus status;
    private final List<QuestStepState> steps;

    public Quest(String id, QuestType type, String title, String description, List<QuestStepState> steps) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.description = description;
        this.status = QuestStatus.NOT_STARTED;
        this.steps = new ArrayList<>(steps);
    }

    public void start() {
        if (status == QuestStatus.NOT_STARTED) {
            status = QuestStatus.IN_PROGRESS;
        }
    }

    public void updateStep(int index, QuestStepState newState) {
        if (index >= 0 && index < steps.size()) {
            steps.set(index, newState);
        }
    }

    public void checkCompletion() {
        if (status == QuestStatus.IN_PROGRESS && steps.stream().allMatch(QuestStepState::isCompleted)) {
            status = QuestStatus.COMPLETED;
        }
    }

    public void fail() {
        if (status == QuestStatus.IN_PROGRESS) {
            status = QuestStatus.FAILED;
        }
    }

    public int progressPercent() {
        if (steps.isEmpty()) return 0;
        long done = steps.stream().filter(QuestStepState::isCompleted).count();
        return (int) (done * 100 / steps.size());
    }

    public boolean isActive() { return status == QuestStatus.IN_PROGRESS; }
    public boolean isCompleted() { return status == QuestStatus.COMPLETED; }

    // Accessor methods
    public String id() { return id; }
    public QuestType type() { return type; }
    public String title() { return title; }
    public String description() { return description; }
    public QuestStatus status() { return status; }
    public List<QuestStepState> steps() { return List.copyOf(steps); }
}
