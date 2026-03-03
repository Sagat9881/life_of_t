package ru.lifegame.backend.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.lifegame.backend.application.view.GameStateView;
import ru.lifegame.backend.infrastructure.web.dto.*;

@Tag(name = "Game API", description = "API для управления игровой сессией Life of T")
@RequestMapping("/api/v1/game")
public interface GameController {

    @Operation(summary = "Начать или загрузить игровую сессию",
               description = "Создаёт новую сессию или загружает существующую")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Сессия успешно создана или загружена",
                     content = @Content(schema = @Schema(implementation = GameStateView.class))),
        @ApiResponse(responseCode = "400", description = "Некорректные данные запроса")
    })
    @PostMapping("/session/start")
    ResponseEntity<GameStateView> startSession(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true)
        @RequestBody StartSessionRequestDto request
    );

    @Operation(summary = "Получить текущее состояние игры")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Состояние игры получено",
                     content = @Content(schema = @Schema(implementation = GameStateView.class))),
        @ApiResponse(responseCode = "404", description = "Сессия не найдена")
    })
    @GetMapping("/state")
    ResponseEntity<GameStateView> getState(
        @Parameter(description = "ID пользователя Telegram", required = true)
        @RequestParam("telegramUserId") String telegramUserId
    );

    @Operation(summary = "Выполнить игровое действие")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Действие выполнено",
                     content = @Content(schema = @Schema(implementation = GameStateView.class))),
        @ApiResponse(responseCode = "400", description = "Действие недоступно"),
        @ApiResponse(responseCode = "404", description = "Сессия не найдена")
    })
    @PostMapping("/action")
    ResponseEntity<GameStateView> executeAction(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true)
        @RequestBody ExecuteActionRequestDto request
    );

    @Operation(summary = "Выбрать тактику разрешения конфликта")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Тактика применена",
                     content = @Content(schema = @Schema(implementation = GameStateView.class))),
        @ApiResponse(responseCode = "400", description = "Неизвестная тактика или нет активного конфликта"),
        @ApiResponse(responseCode = "404", description = "Сессия не найдена")
    })
    @PostMapping("/conflict/tactic")
    ResponseEntity<GameStateView> chooseConflictTactic(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true)
        @RequestBody ChooseConflictTacticRequestDto request
    );

    @Operation(summary = "Выбрать вариант ответа на событие")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Выбор сделан",
                     content = @Content(schema = @Schema(implementation = GameStateView.class))),
        @ApiResponse(responseCode = "400", description = "Событие не активно"),
        @ApiResponse(responseCode = "404", description = "Сессия не найдена")
    })
    @PostMapping("/event-choice")
    ResponseEntity<GameStateView> chooseEventOption(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true)
        @RequestBody ChooseEventOptionRequestDto request
    );

    @Operation(summary = "Завершить текущий день",
               description = "Запускает обработку конца дня: распад характеристик, триггеры конфликтов, проверка окончания игры")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "День завершён",
                     content = @Content(schema = @Schema(implementation = GameStateView.class))),
        @ApiResponse(responseCode = "404", description = "Сессия не найдена")
    })
    @PostMapping("/end-day")
    ResponseEntity<GameStateView> endDay(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true)
        @RequestBody EndDayRequestDto request
    );
}
