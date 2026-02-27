package ru.lifegame.backend.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на выполнение игрового действия")
public record ExecuteActionRequestDto(
    @Schema(
        description = "ID пользователя Telegram",
        example = "123456789",
        required = true
    )
    long telegramUserId,
    
    @Schema(
        description = "Код действия",
        example = "WORK",
        allowableValues = {"WORK", "DATE_WITH_HUSBAND", "FEED_PETS", "PLAY_WITH_PETS", "WALK_PETS", "VISIT_FATHER", "REST", "SOCIAL_MEDIA"},
        required = true
    )
    String actionCode
) {
}
