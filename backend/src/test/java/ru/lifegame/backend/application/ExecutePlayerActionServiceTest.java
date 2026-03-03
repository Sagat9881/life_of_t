package ru.lifegame.backend.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.lifegame.backend.domain.GameSession;
import ru.lifegame.backend.domain.GameSessionRepository;
import ru.lifegame.backend.domain.PlayerAction;
import ru.lifegame.backend.domain.PlayerActionRepository;
import ru.lifegame.backend.api.mapper.GameSessionMapper;
import ru.lifegame.backend.api.dto.GameSessionDto;
import ru.lifegame.backend.api.dto.PlayerActionDto;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExecutePlayerActionServiceTest {

    @Mock
    private GameSessionRepository gameSessionRepository;

    @Mock
    private PlayerActionRepository playerActionRepository;

    @Mock
    private GameSessionMapper gameSessionMapper;

    @InjectMocks
    private ExecutePlayerActionService executePlayerActionService;

    private UUID sessionId;
    private GameSession gameSession;

    @BeforeEach
    void setUp() {
        sessionId = UUID.randomUUID();
        gameSession = new GameSession();
        gameSession.setId(sessionId);
        gameSession.setTelegramUserId(12345L);
        gameSession.setCurrentDay(1);
        gameSession.setScore(0);
        gameSession.setStatus(GameSession.Status.ACTIVE);
    }

    @Test
    void executeAction_success() {
        PlayerActionDto actionDto = new PlayerActionDto();
        actionDto.setSessionId(sessionId);
        actionDto.setActionType("WORK");

        GameSessionDto expectedDto = new GameSessionDto();
        expectedDto.setId(sessionId);

        when(gameSessionRepository.findById(sessionId)).thenReturn(Optional.of(gameSession));
        when(playerActionRepository.save(any(PlayerAction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(gameSessionRepository.save(any(GameSession.class))).thenAnswer(inv -> inv.getArgument(0));
        when(gameSessionMapper.toDto(any(GameSession.class))).thenReturn(expectedDto);

        GameSessionDto result = executePlayerActionService.executeAction(actionDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(sessionId);
        verify(playerActionRepository).save(any(PlayerAction.class));
        verify(gameSessionRepository).save(any(GameSession.class));
    }

    @Test
    void executeAction_sessionNotFound() {
        PlayerActionDto actionDto = new PlayerActionDto();
        actionDto.setSessionId(sessionId);
        actionDto.setActionType("WORK");

        when(gameSessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> executePlayerActionService.executeAction(actionDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(sessionId.toString());
    }

    @Test
    void executeAction_sessionNotActive() {
        gameSession.setStatus(GameSession.Status.COMPLETED);

        PlayerActionDto actionDto = new PlayerActionDto();
        actionDto.setSessionId(sessionId);
        actionDto.setActionType("WORK");

        when(gameSessionRepository.findById(sessionId)).thenReturn(Optional.of(gameSession));

        assertThatThrownBy(() -> executePlayerActionService.executeAction(actionDto))
                .isInstanceOf(IllegalStateException.class);
    }
}
