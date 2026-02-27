package ru.lifegame.backend.infrastructure.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.lifegame.backend.application.command.ChooseConflictTacticCommand;
import ru.lifegame.backend.application.command.ChooseEventOptionCommand;
import ru.lifegame.backend.application.command.ExecutePlayerActionCommand;
import ru.lifegame.backend.application.command.StartSessionCommand;
import ru.lifegame.backend.application.port.in.*;
import ru.lifegame.backend.application.query.GetStateQuery;
import ru.lifegame.backend.application.view.GameStateView;
import ru.lifegame.backend.infrastructure.web.dto.ChooseConflictTacticRequestDto;
import ru.lifegame.backend.infrastructure.web.dto.ChooseEventOptionRequestDto;
import ru.lifegame.backend.infrastructure.web.dto.ExecuteActionRequestDto;
import ru.lifegame.backend.infrastructure.web.dto.StartSessionRequestDto;

@RestController
public class GameControllerImpl implements GameController {

    private final StartOrLoadSessionUseCase startOrLoadSession;
    private final ExecutePlayerActionUseCase executeAction;
    private final GetGameStateUseCase getGameState;
    private final ChooseConflictTacticUseCase chooseConflictTactic;
    private final ChooseEventOptionUseCase chooseEventOption;

    public GameControllerImpl(StartOrLoadSessionUseCase startOrLoadSession,
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

    @Override
    public ResponseEntity<GameStateView> startSession(StartSessionRequestDto request) {
        StartSessionCommand command = new StartSessionCommand(request.telegramUserId());
        GameStateView view = startOrLoadSession.execute(command);
        return ResponseEntity.status(HttpStatus.OK).body(view);
    }

    @Override
    public ResponseEntity<GameStateView> getState(String telegramUserId) {
        GetStateQuery query = new GetStateQuery(telegramUserId);
        GameStateView view = getGameState.execute(query);
        return ResponseEntity.ok(view);
    }

    @Override
    public ResponseEntity<GameStateView> executeAction(ExecuteActionRequestDto request) {
        ExecutePlayerActionCommand command = new ExecutePlayerActionCommand(
                request.telegramUserId(),
                request.actionCode()
        );
        GameStateView view = executeAction.execute(command);
        return ResponseEntity.ok(view);
    }

    @Override
    public ResponseEntity<GameStateView> chooseConflictTactic(ChooseConflictTacticRequestDto request) {
        ChooseConflictTacticCommand command = new ChooseConflictTacticCommand(
                request.telegramUserId(), request.conflictId(), request.tacticCode()
        );
        GameStateView view = chooseConflictTactic.execute(command);
        return ResponseEntity.ok(view);
    }

    @Override
    public ResponseEntity<GameStateView> chooseEventOption(ChooseEventOptionRequestDto request) {
        ChooseEventOptionCommand command = new ChooseEventOptionCommand(
                request.telegramUserId(), request.eventId(), request.optionCode()
        );
        GameStateView view = chooseEventOption.execute(command);
        return ResponseEntity.ok(view);
    }
}
