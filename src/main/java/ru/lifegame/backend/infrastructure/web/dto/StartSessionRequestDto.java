package ru.lifegame.backend.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StartSessionRequestDto(
        @NotBlank @Size(min = 1, max = 50) String telegramUserId
) {}

record ErrorResponseDto(
        String code,
        String message,
        String timestamp,
        String path
) {}
