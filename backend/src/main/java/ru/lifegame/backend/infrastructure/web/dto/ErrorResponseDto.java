package ru.lifegame.backend.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for REST API error representation.
 */
@Schema(description = "\u0418\u043D\u0444\u043E\u0440\u043C\u0430\u0446\u0438\u044F \u043E\u0431 \u043E\u0448\u0438\u0431\u043A\u0435")
public record ErrorResponseDto(
    @Schema(description = "\u041A\u043E\u0434 \u043E\u0448\u0438\u0431\u043A\u0438", example = "SESSION_NOT_FOUND")
    String code,
    
    @Schema(description = "\u0421\u043E\u043E\u0431\u0449\u0435\u043D\u0438\u0435 \u043E\u0431 \u043E\u0448\u0438\u0431\u043A\u0435", example = "Session not found for user: tg-123456789")
    String message,
    
    @Schema(description = "\u0412\u0440\u0435\u043C\u044F \u0432\u043E\u0437\u043D\u0438\u043A\u043D\u043E\u0432\u0435\u043D\u0438\u044F \u043E\u0448\u0438\u0431\u043A\u0438", example = "2026-02-27T15:00:00Z")
    String timestamp,
    
    @Schema(description = "\u041F\u0443\u0442\u044C \u0437\u0430\u043F\u0440\u043E\u0441\u0430", example = "/api/v1/game/state")
    String path
) {
}
