package ru.lifegame.backend.application.service;

import ru.lifegame.backend.application.command.StartSessionCommand;
import ru.lifegame.backend.application.port.in.StartOrLoadSessionUseCase;
import ru.lifegame.backend.application.port.out.SessionRepository;
import ru.lifegame.backend.application.view.GameStateView;
import ru.lifegame.backend.domain.model.session.ActionExecutor;
import ru.lifegame.backend.domain.model.session.ConflictManager;
import ru.lifegame.backend.domain.model.session.GameSession;
import ru.lifegame.backend.infrastructure.web.mapper.GameStateViewMapper;

public class StartOrLoadSessionService implements StartOrLoadSessionUseCase {

    private final SessionRepository sessionRepository;
    private final GameStateViewMapper mapper;
    private final ConflictManager conflictManager;
    private final ActionExecutor actionExecutor;

    public StartOrLoadSessionService(
            SessionRepository sessionRepository,
            GameStateViewMapper mapper,
            ConflictManager conflictManager,
            ActionExecutor actionExecutor
    ) {
        this.sessionRepository = sessionRepository;
        this.mapper = mapper;
        this.conflictManager = conflictManager;
        this.actionExecutor = actionExecutor;
    }

    @Override
    public GameStateView execute(StartSessionCommand command) {
        GameSession session = sessionRepository.findByTelegramUserId(command.telegramUserId())
                .orElseGet(() -> {
                    GameSession newSession = GameSession.createNew(
                            command.telegramUserId(), conflictManager, actionExecutor
                    );
                    sessionRepository.save(newSession);
                    return newSession;
                });
        return mapper.toView(session);
    }
}
