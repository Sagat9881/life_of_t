package ru.lifegame.backend.application.service;

import ru.lifegame.backend.application.command.ChooseConflictTacticCommand;
import ru.lifegame.backend.application.port.in.ChooseConflictTacticUseCase;
import ru.lifegame.backend.application.port.out.EventPublisher;
import ru.lifegame.backend.application.port.out.SessionRepository;
import ru.lifegame.backend.application.view.GameStateView;
import ru.lifegame.backend.domain.conflict.tactics.ConflictTactic;
import ru.lifegame.backend.domain.exception.SessionNotFoundException;
import ru.lifegame.backend.domain.model.session.GameSession;
import ru.lifegame.backend.infrastructure.web.mapper.GameStateViewMapper;

public class ChooseConflictTacticService implements ChooseConflictTacticUseCase {

    private final SessionRepository sessionRepository;
    private final EventPublisher eventPublisher;
    private final GameStateViewMapper mapper;

    public ChooseConflictTacticService(SessionRepository sessionRepository,
                                        EventPublisher eventPublisher,
                                        GameStateViewMapper mapper) {
        this.sessionRepository = sessionRepository;
        this.eventPublisher = eventPublisher;
        this.mapper = mapper;
    }

    @Override
    public GameStateView execute(ChooseConflictTacticCommand command) {
        GameSession session = sessionRepository.findByTelegramUserId(command.telegramUserId())
                .orElseThrow(() -> new SessionNotFoundException(command.telegramUserId()));

        ConflictTactic tactic = ConflictTactic.valueOf(command.tacticCode());
        session.applyTacticToActiveConflict(tactic);

        session.drainDomainEvents().forEach(eventPublisher::publish);
        sessionRepository.save(session);

        return mapper.toView(session);
    }
}
