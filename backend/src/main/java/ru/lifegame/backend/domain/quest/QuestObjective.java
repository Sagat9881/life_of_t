package ru.lifegame.backend.domain.quest;

public record QuestObjective(
        String type,
        String target,
        int required,
        String description
) {}
