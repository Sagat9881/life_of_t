package ru.lifegame.backend.application.service;

import ru.lifegame.backend.application.command.EndDayCommand;
import ru.lifegame.backend.application.port.in.EndDayUseCase;
import ru.lifegame.backend.application.port.out.SessionRepository;
import ru.lifegame.backend.application.view.GameStateView;
import ru.lifegame.backend.domain.exception.SessionNotFoundException;
import ru.lifegame.backend.domain.model.session.GameSession;
import ru.lifegame.backend.infrastructure.web.mapper.GameStateViewMapper;

public class EndDayService implements EndDayUseCase {

    private final SessionRepository sessionRepository;
    private final GameStateViewMapper mapper;

    public EndDayService(SessionRepository sessionRepository, GameStateViewMapper mapper) {
        this.sessionRepository = sessionRepository;
        this.mapper = mapper;
    }

    @Override
    public GameStateView endDay(EndDayCommand command) {
        GameSession session = sessionRepository.findByTelegramUserId(command.telegramUserId())
                .orElseThrow(() -> new SessionNotFoundException(command.telegramUserId()));
        session.endDay();
        sessionRepository.save(session);
        return mapper.toView(session);
    }
}
