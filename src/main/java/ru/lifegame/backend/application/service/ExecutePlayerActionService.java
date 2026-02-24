package ru.lifegame.backend.application.service;

import ru.lifegame.backend.application.command.ExecuteActionCommand;
import ru.lifegame.backend.application.port.in.ExecutePlayerActionUseCase;
import ru.lifegame.backend.application.port.out.EventPublisher;
import ru.lifegame.backend.application.port.out.SessionRepository;
import ru.lifegame.backend.application.view.GameStateView;
import ru.lifegame.backend.domain.action.ActionResult;
import ru.lifegame.backend.domain.exception.SessionNotFoundException;
import ru.lifegame.backend.domain.model.GameSession;
import ru.lifegame.backend.domain.service.GameEngine;

public class ExecutePlayerActionService implements ExecutePlayerActionUseCase {

    private final SessionRepository sessionRepository;
    private final GameEngine gameEngine;
    private final EventPublisher eventPublisher;
    private final GameStateViewMapper mapper;

    public ExecutePlayerActionService(SessionRepository sessionRepository,
                                       GameEngine gameEngine,
                                       EventPublisher eventPublisher,
                                       GameStateViewMapper mapper) {
        this.sessionRepository = sessionRepository;
        this.gameEngine = gameEngine;
        this.eventPublisher = eventPublisher;
        this.mapper = mapper;
    }

    @Override
    public GameStateView execute(ExecuteActionCommand command) {
        GameSession session = sessionRepository.findByTelegramUserId(command.telegramUserId())
                .orElseThrow(() -> new SessionNotFoundException(command.telegramUserId()));
        session.executeAction()
        ActionResult result = session.executeAction(command.actionType());
        session.clearDomainEvents();

        sessionRepository.save(session);
        return mapper.toView(session, result);
    }
}
