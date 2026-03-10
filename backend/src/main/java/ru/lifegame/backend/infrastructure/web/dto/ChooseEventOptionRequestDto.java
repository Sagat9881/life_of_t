package ru.lifegame.backend.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for POST /api/game/event/choose.
 * All fields are mandatory — blank values are rejected at the controller
 * boundary before reaching domain logic.
 */
@Schema(description = "Запрос на выбор варианта ответа на случайное событие")
public record ChooseEventOptionRequestDto(

    @NotBlank(message = "telegramUserId must not be blank")
    @Schema(description = "ID пользователя Telegram", example = "123456789", required = true)
    String telegramUserId,

    @NotBlank(message = "eventId must not be blank")
    @Schema(description = "ID события", example = "anxiety_night_event", required = true)
    String eventId,

    @NotBlank(message = "optionCode must not be blank")
    @Schema(description = "Код выбранного варианта", example = "breathe", required = true)
    String optionCode
) {}
