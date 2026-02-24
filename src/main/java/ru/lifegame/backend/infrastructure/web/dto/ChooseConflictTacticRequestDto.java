package ru.lifegame.backend.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ChooseConflictTacticRequestDto(
        @NotBlank String telegramUserId,
        @NotBlank String conflictId,
        @NotBlank String tacticCode
) {}
