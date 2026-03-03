package ru.lifegame.backend.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.lifegame.backend.application.command.ExecuteActionCommand;
import ru.lifegame.backend.application.port.out.SessionRepository;
import ru.lifegame.backend.application.service.ExecutePlayerActionService;
import ru.lifegame.backend.application.view.GameStateView;
import ru.lifegame.backend.domain.action.GameAction;
import ru.lifegame.backend.domain.action.impl.GoToWorkAction;
import ru.lifegame.backend.domain.action.impl.RestAtHomeAction;
import ru.lifegame.backend.domain.action.StandardActionType;
import ru.lifegame.backend.domain.exception.SessionNotFoundException;
import ru.lifegame.backend.domain.model.session.GameSession;
import ru.lifegame.backend.infrastructure.web.mapper.GameStateViewMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExecutePlayerActionService — выполнение действий игрока")
class ExecutePlayerActionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    private ExecutePlayerActionService service;

    private List<GameAction> allActions;

    @BeforeEach
    void setUp() {
        allActions = List.of(new GoToWorkAction(), new RestAtHomeAction());
        GameStateViewMapper mapper = new GameStateViewMapper(allActions);
        service = new ExecutePlayerActionService(sessionRepository, allActions, mapper);
    }

    @Test
    @DisplayName("execute: выполняет действие 'work' и возвращает обновлённое состояние")
    void executeWorkActionReturnsUpdatedState() {
        GameSession session = GameSession.createNew("user-exec-1");
        when(sessionRepository.findByTelegramUserId("user-exec-1"))
                .thenReturn(Optional.of(session));

        ExecuteActionCommand command = new ExecuteActionCommand("user-exec-1", "GO_TO_WORK");
        GameStateView result = service.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.telegramUserId()).isEqualTo("user-exec-1");
        assertThat(result.lastActionResult()).isNotNull();
        assertThat(result.lastActionResult().actionCode()).isEqualTo("GO_TO_WORK");
        verify(sessionRepository).save(session);
    }

    @Test
    @DisplayName("execute: выполняет действие 'rest_at_home' и возвращает обновлённое состояние")
    void executeRestActionReturnsUpdatedState() {
        GameSession session = GameSession.createNew("user-exec-rest");
        when(sessionRepository.findByTelegramUserId("user-exec-rest"))
                .thenReturn(Optional.of(session));

        ExecuteActionCommand command = new ExecuteActionCommand("user-exec-rest", "REST_AT_HOME");
        GameStateView result = service.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.lastActionResult()).isNotNull();
        assertThat(result.lastActionResult().actionCode()).isEqualTo("REST_AT_HOME");
    }

    @Test
    @DisplayName("execute: бросает SessionNotFoundException если сессия не найдена")
    void executeThrowsWhenSessionNotFound() {
        when(sessionRepository.findByTelegramUserId("ghost-user"))
                .thenReturn(Optional.empty());

        ExecuteActionCommand command = new ExecuteActionCommand("ghost-user", "GO_TO_WORK");

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(SessionNotFoundException.class);
    }

    @Test
    @DisplayName("execute: бросает IllegalArgumentException при неизвестном коде действия")
    void executeThrowsForUnknownActionCode() {
        GameSession session = GameSession.createNew("user-unknown-action");
        when(sessionRepository.findByTelegramUserId("user-unknown-action"))
                .thenReturn(Optional.of(session));

        ExecuteActionCommand command = new ExecuteActionCommand("user-unknown-action", "fly_to_moon");

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fly_to_moon");
    }

    @Test
    @DisplayName("execute: сохраняет сессию после успешного действия")
    void executeAlwaysSavesSessionAfterSuccess() {
        GameSession session = GameSession.createNew("user-save-check");
        when(sessionRepository.findByTelegramUserId("user-save-check"))
                .thenReturn(Optional.of(session));

        service.execute(new ExecuteActionCommand("user-save-check", "GO_TO_WORK"));

        verify(sessionRepository, times(1)).save(any(GameSession.class));
    }

    @Test
    @DisplayName("execute: действие работы уменьшает энергию игрока")
    void workActionDecreaseEnergy() {
        GameSession session = GameSession.createNew("user-energy-check");
        int energyBefore = session.player().stats().energy();
        when(sessionRepository.findByTelegramUserId("user-energy-check"))
                .thenReturn(Optional.of(session));

        GameStateView view = service.execute(new ExecuteActionCommand("user-energy-check", "GO_TO_WORK"));

        assertThat(view.player().stats().energy()).isLessThan(energyBefore);
    }

    @Test
    @DisplayName("execute: возвращённое view содержит доступные действия")
    void executeResultContainsActionOptions() {
        GameSession session = GameSession.createNew("user-options-check");
        when(sessionRepository.findByTelegramUserId("user-options-check"))
                .thenReturn(Optional.of(session));

        GameStateView view = service.execute(new ExecuteActionCommand("user-options-check", "GO_TO_WORK"));

        assertThat(view.availableActions()).isNotEmpty();
    }
}
