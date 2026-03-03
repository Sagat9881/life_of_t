package ru.lifegame.bot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.lifegame.backend.application.port.in.GetGameStateUseCase;
import ru.lifegame.backend.application.port.in.StartOrLoadSessionUseCase;
import ru.lifegame.backend.application.view.*;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Тесты сервиса TelegramGameService.
 */
@ExtendWith(MockitoExtension.class)
class TelegramGameServiceTest {

    @Mock
    private StartOrLoadSessionUseCase startOrLoadSessionUseCase;

    @Mock
    private GetGameStateUseCase getGameStateUseCase;

    private TelegramGameService service;

    @BeforeEach
    void setUp() {
        service = new TelegramGameService(startOrLoadSessionUseCase, getGameStateUseCase);
    }

    // ------------------------------------------------------------------ //
    //  startNewGame
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("startNewGame вызывает use case с правильным chatId")
    void startNewGame_delegatesToUseCase() {
        String chatId = "12345";
        GameStateView mockState = buildMockState();
        when(startOrLoadSessionUseCase.execute(any())).thenReturn(mockState);

        GameStateView result = service.startNewGame(chatId);

        assertThat(result).isEqualTo(mockState);
        verify(startOrLoadSessionUseCase).execute(any());
    }

    // ------------------------------------------------------------------ //
    //  getStats
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("getStats возвращает непустую строку с данными игры")
    void getStats_returnsFormattedString() {
        String chatId = "99";
        GameStateView state = buildMockState();
        when(getGameStateUseCase.execute(any())).thenReturn(state);

        String result = service.getStats(chatId);

        assertThat(result).isNotBlank();
        verify(getGameStateUseCase).execute(any());
    }

    // ------------------------------------------------------------------ //
    //  formatStatsMessage
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("formatStatsMessage с null возвращает сообщение об ошибке")
    void formatStatsMessage_withNull_returnsErrorMessage() {
        String result = service.formatStatsMessage(null);

        assertThat(result).contains("сессия не найдена");
    }

    @Test
    @DisplayName("formatStatsMessage содержит имя игрока и статистику")
    void formatStatsMessage_containsPlayerNameAndStats() {
        GameStateView state = buildMockState();

        String result = service.formatStatsMessage(state);

        assertThat(result)
                .contains("Татьяна")
                .contains("Энергия")
                .contains("Здоровье");
    }

    @Test
    @DisplayName("formatStatsMessage с высокой энергией содержит заполненный бар")
    void formatStatsMessage_highEnergy_showsFilledBar() {
        GameStateView state = buildStateWithEnergy(90);

        String result = service.formatStatsMessage(state);

        // 9 filled blocks out of 10
        assertThat(result).contains("█████████");
    }

    @Test
    @DisplayName("formatStatsMessage с нулевой энергией содержит пустой бар")
    void formatStatsMessage_zeroEnergy_showsEmptyBar() {
        GameStateView state = buildStateWithEnergy(0);

        String result = service.formatStatsMessage(state);

        assertThat(result).contains("░░░░░░░░░░");
    }

    // ------------------------------------------------------------------ //
    //  Вспомогательные методы
    // ------------------------------------------------------------------ //

    private GameStateView buildMockState() {
        StatsView stats = new StatsView(70, 80, 30, 75, 1500, 65);
        PlayerView player = new PlayerView("Татьяна", stats, null, "дом");
        TimeView time = new TimeView(3, 9);
        return new GameStateView("session-1", player, Collections.emptyList(),
                Collections.emptyList(), time, Collections.emptyList(),
                null, null, null, null);
    }

    private GameStateView buildStateWithEnergy(int energy) {
        StatsView stats = new StatsView(energy, 80, 30, 75, 1500, 65);
        PlayerView player = new PlayerView("Татьяна", stats, null, "дом");
        TimeView time = new TimeView(1, 10);
        return new GameStateView("session-2", player, Collections.emptyList(),
                Collections.emptyList(), time, Collections.emptyList(),
                null, null, null, null);
    }
}
