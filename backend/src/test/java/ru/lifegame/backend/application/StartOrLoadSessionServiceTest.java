package ru.lifegame.backend.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.lifegame.backend.domain.GameSession;
import ru.lifegame.backend.domain.GameSessionRepository;
import ru.lifegame.backend.api.mapper.GameSessionMapper;
import ru.lifegame.backend.api.dto.GameSessionDto;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StartOrLoadSessionServiceTest {

    @Mock
    private GameSessionRepository gameSessionRepository;

    @Mock
    private GameSessionMapper gameSessionMapper;

    @InjectMocks
    private StartOrLoadSessionService startOrLoadSessionService;

    private Long telegramUserId;
    private GameSession existingSession;

    @BeforeEach
    void setUp() {
        telegramUserId = 12345L;
        existingSession = new GameSession();
        existingSession.setId(UUID.randomUUID());
        existingSession.setTelegramUserId(telegramUserId);
        existingSession.setCurrentDay(5);
        existingSession.setScore(100);
        existingSession.setStatus(GameSession.Status.ACTIVE);
    }

    @Test
    void startOrLoad_existingActiveSession() {
        GameSessionDto expectedDto = new GameSessionDto();
        expectedDto.setId(existingSession.getId());
        expectedDto.setCurrentDay(5);

        when(gameSessionRepository.findByTelegramUserIdAndStatus(telegramUserId, GameSession.Status.ACTIVE))
                .thenReturn(Optional.of(existingSession));
        when(gameSessionMapper.toDto(existingSession)).thenReturn(expectedDto);

        GameSessionDto result = startOrLoadSessionService.startOrLoad(telegramUserId);

        assertThat(result).isNotNull();
        assertThat(result.getCurrentDay()).isEqualTo(5);
        verify(gameSessionRepository, never()).save(any());
    }

    @Test
    void startOrLoad_noActiveSession_createsNew() {
        GameSession newSession = new GameSession();
        newSession.setId(UUID.randomUUID());
        newSession.setTelegramUserId(telegramUserId);
        newSession.setCurrentDay(1);
        newSession.setScore(0);
        newSession.setStatus(GameSession.Status.ACTIVE);

        GameSessionDto expectedDto = new GameSessionDto();
        expectedDto.setId(newSession.getId());
        expectedDto.setCurrentDay(1);

        when(gameSessionRepository.findByTelegramUserIdAndStatus(telegramUserId, GameSession.Status.ACTIVE))
                .thenReturn(Optional.empty());
        when(gameSessionRepository.save(any(GameSession.class))).thenReturn(newSession);
        when(gameSessionMapper.toDto(newSession)).thenReturn(expectedDto);

        GameSessionDto result = startOrLoadSessionService.startOrLoad(telegramUserId);

        assertThat(result).isNotNull();
        assertThat(result.getCurrentDay()).isEqualTo(1);
        verify(gameSessionRepository).save(any(GameSession.class));
    }
}
