package ru.lifegame.backend.application.service;

import ru.lifegame.backend.application.command.StartSessionCommand;
import ru.lifegame.backend.application.port.in.StartOrLoadSessionUseCase;
import ru.lifegame.backend.application.port.out.SessionRepository;
import ru.lifegame.backend.application.view.GameStateView;
import ru.lifegame.backend.domain.model.GameSession;
import ru.lifegame.backend.infrastructure.web.mapper.GameStateViewMapper;

public class StartOrLoadSessionService implements StartOrLoadSessionUseCase {

    private final SessionRepository sessionRepository;
    private final GameStateViewMapper mapper;

    public StartOrLoadSessionService(SessionRepository sessionRepository, GameStateViewMapper mapper) {
        this.sessionRepository = sessionRepository;
        this.mapper = mapper;
    }

    @Override
    public GameStateView execute(StartSessionCommand command) {
        GameSession session = sessionRepository.findByTelegramUserId(command.telegramUserId())
                .orElseGet(() -> {
                    GameSession newSession = GameSession.createNew(command.telegramUserId());
                    sessionRepository.save(newSession);
                    return newSession;
                });
        return mapper.toView(session);
    }
}
