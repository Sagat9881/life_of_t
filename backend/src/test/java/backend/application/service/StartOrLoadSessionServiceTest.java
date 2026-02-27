package backend.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.lifegame.backend.application.command.StartSessionCommand;
import ru.lifegame.backend.application.port.out.SessionRepository;
import ru.lifegame.backend.application.service.StartOrLoadSessionService;
import ru.lifegame.backend.application.view.GameStateView;
import ru.lifegame.backend.domain.model.session.GameSession;
import ru.lifegame.backend.infrastructure.web.mapper.GameStateViewMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class StartOrLoadSessionServiceTest {

    private SessionRepository sessionRepository;
    private GameStateViewMapper mapper;
    private StartOrLoadSessionService service;

    @BeforeEach
    void setUp() {
        sessionRepository = mock(SessionRepository.class);
        mapper = new GameStateViewMapper(List.of());
        service = new StartOrLoadSessionService(sessionRepository, mapper);
    }

    @Test
    void execute_shouldReturnExistingSession_whenFound() {
        GameSession existing = GameSession.createNew("user123");
        when(sessionRepository.findByTelegramUserId("user123")).thenReturn(Optional.of(existing));

        GameStateView result = service.execute(new StartSessionCommand("user123"));

        assertThat(result.telegramUserId()).isEqualTo("user123");
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void execute_shouldCreateNewSession_whenNotFound() {
        when(sessionRepository.findByTelegramUserId("user456")).thenReturn(Optional.empty());

        GameStateView result = service.execute(new StartSessionCommand("user456"));

        assertThat(result.telegramUserId()).isEqualTo("user456");
        assertThat(result.player().name()).isEqualTo("Татьяна");
        verify(sessionRepository).save(any(GameSession.class));
    }
}
