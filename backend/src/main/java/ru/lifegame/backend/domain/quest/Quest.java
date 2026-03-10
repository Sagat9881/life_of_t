package ru.lifegame.backend.domain.quest;

import java.util.ArrayList;
import java.util.List;

public class Quest {
    private final String id;
    private final String type;
    private final String title;
    private final String description;
    private String status;
    private final List<QuestStepState> steps;

    public Quest(String id, String type, String title, String description, List<QuestStepState> steps) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.description = description;
        this.status = "NOT_STARTED";
        this.steps = new ArrayList<>(steps);
    }

    public void start() {
        if ("NOT_STARTED".equals(status)) {
            status = "IN_PROGRESS";
        }
    }

    public void updateStep(int index, QuestStepState newState) {
        if (index >= 0 && index < steps.size()) {
            steps.set(index, newState);
        }
    }

    public void checkCompletion() {
        if ("IN_PROGRESS".equals(status) && steps.stream().allMatch(QuestStepState::isCompleted)) {
            status = "COMPLETED";
        }
    }

    public void fail() {
        if ("IN_PROGRESS".equals(status)) {
            status = "FAILED";
        }
    }

    public int progressPercent() {
        if (steps.isEmpty()) return 0;
        long done = steps.stream().filter(QuestStepState::isCompleted).count();
        return (int) (done * 100 / steps.size());
    }

    public boolean isActive() { return "IN_PROGRESS".equals(status); }
    public boolean isCompleted() { return "COMPLETED".equals(status); }

    // Accessor methods
    public String id() { return id; }
    public String type() { return type; }
    public String title() { return title; }
    public String description() { return description; }
    public String status() { return status; }
    public List<QuestStepState> steps() { return List.copyOf(steps); }
}
