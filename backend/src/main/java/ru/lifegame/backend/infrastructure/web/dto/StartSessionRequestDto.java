package ru.lifegame.backend.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "\u0417\u0430\u043F\u0440\u043E\u0441 \u043D\u0430 \u043D\u0430\u0447\u0430\u043B\u043E \u0438\u043B\u0438 \u0437\u0430\u0433\u0440\u0443\u0437\u043A\u0443 \u0438\u0433\u0440\u043E\u0432\u043E\u0439 \u0441\u0435\u0441\u0441\u0438\u0438")
public record StartSessionRequestDto(
    @Schema(
        description = "ID \u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u0435\u043B\u044F Telegram",
        example = "123456789",
        required = true
    )
    String telegramUserId
) {
}
