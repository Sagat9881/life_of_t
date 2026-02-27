package ru.lifegame.backend.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на выбор тактики разрешения конфликта")
public record ChooseConflictTacticRequestDto(
    @Schema(
        description = "ID пользователя Telegram",
        example = "123456789",
        required = true
    )
    long telegramUserId,
    
    @Schema(
        description = "ID активного конфликта",
        example = "conflict-uuid-123",
        required = true
    )
    String conflictId,
    
    @Schema(
        description = "Код тактики",
        example = "SURRENDER",
        allowableValues = {
            "SURRENDER", "ASSERT", "COMPROMISE", "AVOID",
            "LISTEN_AND_UNDERSTAND", "USE_HUMOR", "LOGICAL_ARGUMENT",
            "EMOTIONAL_APPEAL", "SET_BOUNDARIES"
        },
        required = true
    )
    String tacticCode
) {
}
