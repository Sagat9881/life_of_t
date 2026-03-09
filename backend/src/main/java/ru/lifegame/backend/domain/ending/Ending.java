package ru.lifegame.backend.domain.ending;

public record Ending(
        String endingId,
        EndingCategory category,
        String title,
        String summary,
        String epilogueText
) {}
