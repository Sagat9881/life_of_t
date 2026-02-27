package ru.lifegame.backend.application.service;

import ru.lifegame.backend.application.port.in.GetGameStateUseCase;
import ru.lifegame.backend.application.port.out.SessionRepository;
import ru.lifegame.backend.application.query.GetStateQuery;
import ru.lifegame.backend.application.view.GameStateView;
import ru.lifegame.backend.domain.exception.SessionNotFoundException;
import ru.lifegame.backend.domain.model.session.GameSession;
import ru.lifegame.backend.infrastructure.web.mapper.GameStateViewMapper;

public class GetGameStateService implements GetGameStateUseCase {

    private final SessionRepository sessionRepository;
    private final GameStateViewMapper mapper;

    public GetGameStateService(SessionRepository sessionRepository, GameStateViewMapper mapper) {
        this.sessionRepository = sessionRepository;
        this.mapper = mapper;
    }

    @Override
    public GameStateView execute(GetStateQuery query) {
        GameSession session = sessionRepository.findByTelegramUserId(query.telegramUserId())
                .orElseThrow(() -> new SessionNotFoundException(query.telegramUserId()));
        return mapper.toView(session);
    }
}
