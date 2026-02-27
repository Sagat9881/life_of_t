package ru.lifegame.backend.domain.ending;

public record Ending(
        EndingType type,
        EndingCategory category,
        String title,
        String summary,
        String epilogueText
) {}
