package ru.lifegame.backend.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ExecuteActionRequestDto(
        @NotBlank String telegramUserId,
        @NotBlank String actionCode
) {}
