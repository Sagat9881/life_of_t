package ru.lifegame.backend.application.service;

import ru.lifegame.backend.application.command.ChooseEventOptionCommand;
import ru.lifegame.backend.application.port.in.ChooseEventOptionUseCase;
import ru.lifegame.backend.application.port.out.SessionRepository;
import ru.lifegame.backend.application.view.GameStateView;
import ru.lifegame.backend.domain.exception.SessionNotFoundException;
import ru.lifegame.backend.domain.model.session.GameSession;
import ru.lifegame.backend.infrastructure.web.mapper.GameStateViewMapper;

public class ChooseEventOptionService implements ChooseEventOptionUseCase {

    private final SessionRepository sessionRepository;
    private final GameStateViewMapper mapper;

    public ChooseEventOptionService(SessionRepository sessionRepository, GameStateViewMapper mapper) {
        this.sessionRepository = sessionRepository;
        this.mapper = mapper;
    }

    @Override
    public GameStateView execute(ChooseEventOptionCommand command) {
        GameSession session = sessionRepository.findByTelegramUserId(command.telegramUserId())
                .orElseThrow(() -> new SessionNotFoundException(command.telegramUserId()));

        session.chooseEventOption(command.eventId(), command.optionCode());

        sessionRepository.save(session);

        return mapper.toView(session);
    }
}
