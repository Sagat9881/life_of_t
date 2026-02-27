package ru.lifegame.backend.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на выбор варианта ответа на событие")
public record ChooseEventOptionRequestDto(
    @Schema(
        description = "ID пользователя Telegram",
        example = "123456789",
        required = true
    )
    long telegramUserId,
    
    @Schema(
        description = "ID активного события",
        example = "event-uuid-456",
        required = true
    )
    String eventId,
    
    @Schema(
        description = "Код выбранного варианта ответа",
        example = "option-1",
        required = true
    )
    String optionCode
) {
}
