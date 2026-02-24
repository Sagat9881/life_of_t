package ru.lifegame.backend.application.service;

import ru.lifegame.backend.application.command.ChooseEventOptionCommand;
import ru.lifegame.backend.application.port.in.ChooseEventOptionUseCase;
import ru.lifegame.backend.application.port.out.EventPublisher;
import ru.lifegame.backend.application.port.out.SessionRepository;
import ru.lifegame.backend.application.view.GameStateView;
import ru.lifegame.backend.domain.exception.SessionNotFoundException;
import ru.lifegame.backend.domain.model.GameSession;
import ru.lifegame.backend.infrastructure.web.mapper.GameStateViewMapper;

public class ChooseEventOptionService implements ChooseEventOptionUseCase {

    private final SessionRepository sessionRepository;
    private final EventPublisher eventPublisher;
    private final GameStateViewMapper mapper;

    public ChooseEventOptionService(SessionRepository sessionRepository,
                                     EventPublisher eventPublisher,
                                     GameStateViewMapper mapper) {
        this.sessionRepository = sessionRepository;
        this.eventPublisher = eventPublisher;
        this.mapper = mapper;
    }

    @Override
    public GameStateView execute(ChooseEventOptionCommand command) {
        GameSession session = sessionRepository.findByTelegramUserId(command.telegramUserId())
                .orElseThrow(() -> new SessionNotFoundException(command.telegramUserId()));

        session.applyEventChoice(command.eventId(), command.optionCode());

        session.drainDomainEvents().forEach(eventPublisher::publish);
        sessionRepository.save(session);

        return mapper.toView(session);
    }
}
