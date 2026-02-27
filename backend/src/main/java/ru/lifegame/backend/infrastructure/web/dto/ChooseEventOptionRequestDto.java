package ru.lifegame.backend.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Запрос на выбор варианта ответа на случайное событие.
 */
@Schema(description = "Запрос на выбор варианта ответа на случайное событие")
public record ChooseEventOptionRequestDto(
    @Schema(description = "ID пользователя Telegram", example = "123456789", required = true)
    String telegramUserId,
    
    @Schema(description = "ID события", example = "event-uuid-456", required = true)
    String eventId,
    
    @Schema(description = "Код выбранного варианта", example = "option_a", required = true)
    String optionCode
) {
}
