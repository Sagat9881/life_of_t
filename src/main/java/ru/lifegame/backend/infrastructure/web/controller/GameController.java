package ru.lifegame.backend.infrastructure.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.lifegame.backend.application.command.ChooseConflictTacticCommand;
import ru.lifegame.backend.application.command.ChooseEventOptionCommand;
import ru.lifegame.backend.application.command.ExecuteActionCommand;
import ru.lifegame.backend.application.command.StartSessionCommand;
import ru.lifegame.backend.application.port.in.*;
import ru.lifegame.backend.application.query.GetStateQuery;
import ru.lifegame.backend.application.view.GameStateView;
import ru.lifegame.backend.domain.action.Actions;
import ru.lifegame.backend.infrastructure.web.dto.ChooseConflictTacticRequestDto;
import ru.lifegame.backend.infrastructure.web.dto.ChooseEventOptionRequestDto;
import ru.lifegame.backend.infrastructure.web.dto.ExecuteActionRequestDto;
import ru.lifegame.backend.infrastructure.web.dto.StartSessionRequestDto;

@RestController
@RequestMapping("/api/v1/game")
public class GameController {

    private final StartOrLoadSessionUseCase startOrLoadSession;
    private final ExecutePlayerActionUseCase executeAction;
    private final GetGameStateUseCase getGameState;
    private final ChooseConflictTacticUseCase chooseConflictTactic;
    private final ChooseEventOptionUseCase chooseEventOption;

    public GameController(StartOrLoadSessionUseCase startOrLoadSession,
                          ExecutePlayerActionUseCase executeAction,
                          GetGameStateUseCase getGameState,
                          ChooseConflictTacticUseCase chooseConflictTactic,
                          ChooseEventOptionUseCase chooseEventOption) {
        this.startOrLoadSession = startOrLoadSession;
        this.executeAction = executeAction;
        this.getGameState = getGameState;
        this.chooseConflictTactic = chooseConflictTactic;
        this.chooseEventOption = chooseEventOption;
    }

    @PostMapping("/session/start")
    public ResponseEntity<GameStateView> startSession(@RequestBody StartSessionRequestDto request) {
        StartSessionCommand command = new StartSessionCommand(request.telegramUserId());
        GameStateView view = startOrLoadSession.execute(command);
        return ResponseEntity.status(HttpStatus.OK).body(view);
    }

    @GetMapping("/state")
    public ResponseEntity<GameStateView> getState(@RequestParam String telegramUserId) {
        GetStateQuery query = new GetStateQuery(telegramUserId);
        GameStateView view = getGameState.execute(query);
        return ResponseEntity.ok(view);
    }

    @PostMapping("/action")
    public ResponseEntity<GameStateView> executeAction(@RequestBody ExecuteActionRequestDto request) {
        ExecuteActionCommand command = new ExecuteActionCommand(
                request.telegramUserId(),
                Actions.valueOf(request.actionCode())
        );
        GameStateView view = executeAction.execute(command);
        return ResponseEntity.ok(view);
    }

    @PostMapping("/conflict/tactic")
    public ResponseEntity<GameStateView> chooseConflictTactic(@RequestBody ChooseConflictTacticRequestDto request) {
        ChooseConflictTacticCommand command = new ChooseConflictTacticCommand(
                request.telegramUserId(), request.conflictId(), request.tacticCode()
        );
        GameStateView view = chooseConflictTactic.execute(command);
        return ResponseEntity.ok(view);
    }

    @PostMapping("/event-choice")
    public ResponseEntity<GameStateView> chooseEventOption(@RequestBody ChooseEventOptionRequestDto request) {
        ChooseEventOptionCommand command = new ChooseEventOptionCommand(
                request.telegramUserId(), request.eventId(), request.optionCode()
        );
        GameStateView view = chooseEventOption.execute(command);
        return ResponseEntity.ok(view);
    }
}
