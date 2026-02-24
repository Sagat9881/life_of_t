package ru.lifegame.backend.application.service;

import ru.lifegame.backend.application.command.ExecuteActionCommand;
import ru.lifegame.backend.application.port.in.ExecutePlayerActionUseCase;
import ru.lifegame.backend.application.port.out.EventPublisher;
import ru.lifegame.backend.application.port.out.SessionRepository;
import ru.lifegame.backend.application.view.GameStateView;
import ru.lifegame.backend.domain.action.ActionProvider;
import ru.lifegame.backend.domain.action.ActionResult;
import ru.lifegame.backend.domain.action.GameAction;
import ru.lifegame.backend.domain.exception.SessionNotFoundException;
import ru.lifegame.backend.domain.model.session.GameSession;
import ru.lifegame.backend.infrastructure.web.mapper.GameStateViewMapper;

public class ExecutePlayerActionService implements ExecutePlayerActionUseCase {

    private final SessionRepository sessionRepository;
    private final ActionProvider actionProvider;
    private final EventPublisher eventPublisher;
    private final GameStateViewMapper mapper;

    public ExecutePlayerActionService(SessionRepository sessionRepository,
                                       ActionProvider actionProvider,
                                       EventPublisher eventPublisher,
                                       GameStateViewMapper mapper) {
        this.sessionRepository = sessionRepository;
        this.actionProvider = actionProvider;
        this.eventPublisher = eventPublisher;
        this.mapper = mapper;
    }

    @Override
    public GameStateView execute(ExecuteActionCommand command) {
        GameSession session = sessionRepository.findByTelegramUserId(command.telegramUserId())
                .orElseThrow(() -> new SessionNotFoundException(command.telegramUserId()));
        
        GameAction action = actionProvider.getAction(command.actionType().code());
        ActionResult result = session.executeAction(action);
        
        session.drainDomainEvents().forEach(eventPublisher::publish);
        sessionRepository.save(session);
        
        return mapper.toView(session, result);
    }
}
