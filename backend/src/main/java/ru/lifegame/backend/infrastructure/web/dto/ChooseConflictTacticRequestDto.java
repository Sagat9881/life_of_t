package ru.lifegame.backend.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Запрос на выбор тактики разрешения конфликта.
 */
@Schema(description = "Запрос на выбор тактики разрешения конфликта")
public record ChooseConflictTacticRequestDto(
    @Schema(
        description = "ID пользователя Telegram",
        example = "123456789",
        required = true
    )
    String telegramUserId,

    @Schema(
        description = "ID конфликта",
        example = "conflict-uuid-123",
        required = true
    )
    String conflictId,

    @Schema(
        description = "Код выбранной тактики",
        example = "compromise",
        required = true,
        allowableValues = {"avoid", "force", "compromise", "accommodate", "collaborate",
                           "humor", "rhetoric", "empathy", "assertiveness"}
    )
    String tacticCode
) {
}
