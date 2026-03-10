package ru.lifegame.backend.domain.ending;

public record Ending(
        String endingId,
        String category,
        String title,
        String summary,
        String epilogueText
) {}
