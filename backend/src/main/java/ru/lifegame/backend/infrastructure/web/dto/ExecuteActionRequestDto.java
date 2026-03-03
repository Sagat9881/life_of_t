package ru.lifegame.backend.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на выполнение игрового действия")
public record ExecuteActionRequestDto(
    @Schema(description = "ID пользователя Telegram", example = "123456789", required = true)
    String telegramUserId,

    @Schema(description = "Код действия", example = "GO_TO_WORK", required = true)
    String actionCode
) {}
