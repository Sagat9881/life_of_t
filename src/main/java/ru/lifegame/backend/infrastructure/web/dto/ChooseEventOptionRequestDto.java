package ru.lifegame.backend.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ChooseEventOptionRequestDto(
        @NotBlank String telegramUserId,
        @NotBlank String eventId,
        @NotBlank String optionCode
) {}
