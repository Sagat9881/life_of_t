package ru.lifegame.backend.application.service;

import ru.lifegame.backend.application.command.ExecutePlayerActionCommand;
import ru.lifegame.backend.application.port.in.ExecutePlayerActionUseCase;
import ru.lifegame.backend.application.port.out.SessionRepository;
import ru.lifegame.backend.application.view.GameStateView;
import ru.lifegame.backend.domain.action.ActionResult;
import ru.lifegame.backend.domain.action.ActionType;
import ru.lifegame.backend.domain.action.GameAction;
import ru.lifegame.backend.domain.exception.SessionNotFoundException;
import ru.lifegame.backend.domain.model.session.GameSession;
import ru.lifegame.backend.infrastructure.web.mapper.GameStateViewMapper;

import java.util.List;

public class ExecutePlayerActionService implements ExecutePlayerActionUseCase {

    private final SessionRepository sessionRepository;
    private final List<GameAction> allActions;
    private final GameStateViewMapper mapper;

    public ExecutePlayerActionService(SessionRepository sessionRepository,
                                       List<GameAction> allActions,
                                       GameStateViewMapper mapper) {
        this.sessionRepository = sessionRepository;
        this.allActions = allActions;
        this.mapper = mapper;
    }

    @Override
    public GameStateView execute(ExecutePlayerActionCommand command) {
        GameSession session = sessionRepository.findByTelegramUserId(command.telegramUserId())
                .orElseThrow(() -> new SessionNotFoundException(command.telegramUserId()));

        GameAction action = allActions.stream()
                .filter(a -> a.type().code().equals(command.actionCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown action: " + command.actionCode()));

        ActionResult result = session.executeAction(action.type());

        sessionRepository.save(session);

        return mapper.toView(session, result);
    }
}
