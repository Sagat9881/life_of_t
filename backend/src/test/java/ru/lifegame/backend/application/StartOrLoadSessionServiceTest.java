package ru.lifegame.backend.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.lifegame.backend.application.command.StartSessionCommand;
import ru.lifegame.backend.application.port.out.SessionRepository;
import ru.lifegame.backend.application.service.StartOrLoadSessionService;
import ru.lifegame.backend.application.view.GameStateView;
import ru.lifegame.backend.domain.action.GameAction;
import ru.lifegame.backend.domain.action.impl.GoToWorkAction;
import ru.lifegame.backend.domain.action.impl.RestAtHomeAction;
import ru.lifegame.backend.domain.model.session.GameSession;
import ru.lifegame.backend.infrastructure.web.mapper.GameStateViewMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StartOrLoadSessionService — создание и загрузка сессии")
class StartOrLoadSessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    private StartOrLoadSessionService service;

    @BeforeEach
    void setUp() {
        List<GameAction> actions = List.of(new GoToWorkAction(), new RestAtHomeAction());
        GameStateViewMapper mapper = new GameStateViewMapper(actions);
        service = new StartOrLoadSessionService(sessionRepository, mapper);
    }

    @Test
    @DisplayName("execute: создаёт новую сессию если не существует")
    void createsNewSessionWhenNotExists() {
        when(sessionRepository.findByTelegramUserId("new-user"))
                .thenReturn(Optional.empty());

        StartSessionCommand command = new StartSessionCommand("new-user");
        GameStateView result = service.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.telegramUserId()).isEqualTo("new-user");
        verify(sessionRepository).save(any(GameSession.class));
    }

    @Test
    @DisplayName("execute: загружает существующую сессию без создания новой")
    void loadsExistingSessionWithoutCreatingNew() {
        GameSession existing = GameSession.createNew("existing-user");
        when(sessionRepository.findByTelegramUserId("existing-user"))
                .thenReturn(Optional.of(existing));

        StartSessionCommand command = new StartSessionCommand("existing-user");
        GameStateView result = service.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.telegramUserId()).isEqualTo("existing-user");
        // Не должны сохранять при загрузке существующей сессии
        verify(sessionRepository, never()).save(any(GameSession.class));
    }

    @Test
    @DisplayName("execute: возвращённое view содержит корректный sessionId")
    void returnedViewHasCorrectSessionId() {
        GameSession existing = GameSession.createNew("user-session-id");
        when(sessionRepository.findByTelegramUserId("user-session-id"))
                .thenReturn(Optional.of(existing));

        GameStateView result = service.execute(new StartSessionCommand("user-session-id"));

        assertThat(result.sessionId()).isEqualTo(existing.sessionId());
    }

    @Test
    @DisplayName("execute: новая сессия начинается на 1-м дне и в 8:00")
    void newSessionStartsOnDay1Hour8() {
        when(sessionRepository.findByTelegramUserId("fresh-user"))
                .thenReturn(Optional.empty());

        GameStateView result = service.execute(new StartSessionCommand("fresh-user"));

        assertThat(result.time().day()).isEqualTo(1);
        assertThat(result.time().hour()).isEqualTo(8);
    }

    @Test
    @DisplayName("execute: новая сессия имеет ненулевые начальные характеристики")
    void newSessionHasNonZeroInitialStats() {
        when(sessionRepository.findByTelegramUserId("stats-user"))
                .thenReturn(Optional.empty());

        GameStateView result = service.execute(new StartSessionCommand("stats-user"));

        assertThat(result.player()).isNotNull();
        assertThat(result.player().stats().energy()).isGreaterThan(0);
        assertThat(result.player().stats().health()).isGreaterThan(0);
        assertThat(result.player().stats().money()).isGreaterThan(0);
    }

    @Test
    @DisplayName("execute: возвращённое view содержит список доступных действий")
    void newSessionHasAvailableActions() {
        when(sessionRepository.findByTelegramUserId("actions-user"))
                .thenReturn(Optional.empty());

        GameStateView result = service.execute(new StartSessionCommand("actions-user"));

        assertThat(result.availableActions()).isNotEmpty();
        assertThat(result.availableActions()).anyMatch(a -> a.code().equals("GO_TO_WORK"));
    }

    @Test
    @DisplayName("execute: новая сессия не имеет активного финала (game not over)")
    void newSessionHasNoEnding() {
        when(sessionRepository.findByTelegramUserId("ending-user"))
                .thenReturn(Optional.empty());

        GameStateView result = service.execute(new StartSessionCommand("ending-user"));

        assertThat(result.ending()).isNull();
        assertThat(result.lastActionResult()).isNull();
    }

    @Test
    @DisplayName("execute: view содержит питомцев (Барсика и Сэма)")
    void newSessionHasPets() {
        when(sessionRepository.findByTelegramUserId("pet-user"))
                .thenReturn(Optional.empty());

        GameStateView result = service.execute(new StartSessionCommand("pet-user"));

        assertThat(result.pets()).isNotEmpty();
        // Pets: barsik (Барсик) and sam (Сэм)
        assertThat(result.pets()).anyMatch(p -> p.name().contains("Барсик") || p.name().contains("Сэм"));
    }
}
