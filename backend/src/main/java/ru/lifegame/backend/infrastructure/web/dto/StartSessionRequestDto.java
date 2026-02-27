package ru.lifegame.backend.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на начало или загрузку игровой сессии")
public record StartSessionRequestDto(
    @Schema(
        description = "ID пользователя Telegram (строка для совместимости с demo-режимом)",
        example = "123456789",
        required = true
    )
    String telegramUserId
) {
}
