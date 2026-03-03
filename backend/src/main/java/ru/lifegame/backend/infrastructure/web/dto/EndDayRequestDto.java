package ru.lifegame.backend.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на завершение дня")
public record EndDayRequestDto(
    @Schema(description = "ID пользователя Telegram", example = "123456789", required = true)
    String telegramUserId
) {}
