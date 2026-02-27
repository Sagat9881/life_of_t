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
import ru.lifegame.backend.infrastructure.web.dto.ChooseConflictTacticRequestDto;
import ru.lifegame.backend.infrastructure.web.dto.ChooseEventOptionRequestDto;
import ru.lifegame.backend.infrastructure.web.dto.ExecuteActionRequestDto;
import ru.lifegame.backend.infrastructure.web.dto.StartSessionRequestDto;

@Tag(name = "Game API", description = "API для управления игровой сессией Life of T")
@RequestMapping("/api/v1/game")
public interface GameController {

    @Operation(
        summary = "Начать или загрузить игровую сессию",
        description = "Создаёт новую игровую сессию для пользователя или загружает существующую, если она уже была создана"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Сессия успешно создана или загружена",
            content = @Content(schema = @Schema(implementation = GameStateView.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Некорректные данные запроса"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Внутренняя ошибка сервера"
        )
    })
    @PostMapping("/session/start")
    ResponseEntity<GameStateView> startSession(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для начала сессии",
            required = true,
            content = @Content(schema = @Schema(implementation = StartSessionRequestDto.class))
        )
        @RequestBody StartSessionRequestDto request
    );

    @Operation(
        summary = "Получить текущее состояние игры",
        description = "Возвращает полное состояние игровой сессии: персонаж, отношения, питомцы, квесты, конфликты и события"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Состояние игры успешно получено",
            content = @Content(schema = @Schema(implementation = GameStateView.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Сессия не найдена"
        )
    })
    @GetMapping("/state")
    ResponseEntity<GameStateView> getState(
        @Parameter(description = "ID пользователя Telegram", required = true, example = "123456789")
        @RequestParam String telegramUserId
    );

    @Operation(
        summary = "Выполнить игровое действие",
        description = "Выполняет действие игрока: работа, свидание с мужем, уход за питомцами, визит к отцу и т.д."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Действие успешно выполнено",
            content = @Content(schema = @Schema(implementation = GameStateView.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Неизвестное действие или действие недоступно"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Сессия не найдена"
        )
    })
    @PostMapping("/action")
    ResponseEntity<GameStateView> executeAction(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Запрос на выполнение действия",
            required = true,
            content = @Content(schema = @Schema(implementation = ExecuteActionRequestDto.class))
        )
        @RequestBody ExecuteActionRequestDto request
    );

    @Operation(
        summary = "Выбрать тактику разрешения конфликта",
        description = "Применяет выбранную тактику для активного конфликта: уступить, настоять, компромисс, избежать, эмпатия и т.д."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Тактика успешно применена",
            content = @Content(schema = @Schema(implementation = GameStateView.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Неизвестная тактика или нет активного конфликта"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Сессия не найдена"
        )
    })
    @PostMapping("/conflict/tactic")
    ResponseEntity<GameStateView> chooseConflictTactic(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Запрос на выбор тактики конфликта",
            required = true,
            content = @Content(schema = @Schema(implementation = ChooseConflictTacticRequestDto.class))
        )
        @RequestBody ChooseConflictTacticRequestDto request
    );

    @Operation(
        summary = "Выбрать вариант ответа на событие",
        description = "Делает выбор в текущем игровом событии, который влияет на развитие сюжета и отношения"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Выбор успешно сделан",
            content = @Content(schema = @Schema(implementation = GameStateView.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Неизвестный вариант или событие не активно"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Сессия не найдена"
        )
    })
    @PostMapping("/event-choice")
    ResponseEntity<GameStateView> chooseEventOption(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Запрос на выбор варианта события",
            required = true,
            content = @Content(schema = @Schema(implementation = ChooseEventOptionRequestDto.class))
        )
        @RequestBody ChooseEventOptionRequestDto request
    );
}
