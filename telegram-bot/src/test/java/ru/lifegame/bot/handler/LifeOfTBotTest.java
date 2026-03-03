package ru.lifegame.bot.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.lifegame.bot.config.BotProperties;
import ru.lifegame.bot.service.TelegramGameService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Тесты обработки команд ботом LifeOfTBot.
 */
@ExtendWith(MockitoExtension.class)
class LifeOfTBotTest {

    // BotProperties со stub-значениями
    private static final BotProperties PROPS = new BotProperties(
            "test-token", "TestBot", "https://example.com");

    @Mock
    private TelegramGameService gameService;

    @Mock
    private WebAppDataHandler webAppDataHandler;

    // Тестируемый экземпляр создаётся как spy, чтобы перехватывать sendMarkdownMessage
    private LifeOfTBot bot;

    @BeforeEach
    void setUp() {
        bot = spy(new LifeOfTBot(PROPS, gameService, webAppDataHandler));
        // Предотвращаем реальные HTTP-вызовы к Telegram API
        doNothing().when(bot).sendMarkdownMessage(any(), anyString(), any());
    }

    // ------------------------------------------------------------------ //
    //  /start
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("/start отправляет приветственное сообщение с inline-кнопкой")
    void start_sendsWelcomeMessageWithInlineKeyboard() {
        Update update = buildTextUpdate(12345L, "/start");

        bot.onUpdateReceived(update);

        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        verify(bot).sendMarkdownMessage(eq(12345L), textCaptor.capture(), any());

        String sentText = textCaptor.getValue();
        assertThat(sentText)
                .contains("Life of T")
                .contains("Татьяну");
    }

    // ------------------------------------------------------------------ //
    //  /help
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("/help отправляет справочное сообщение")
    void help_sendsHelpText() {
        Update update = buildTextUpdate(99L, "/help");

        bot.onUpdateReceived(update);

        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        verify(bot).sendMarkdownMessage(eq(99L), textCaptor.capture(), any());

        assertThat(textCaptor.getValue())
                .contains("Справка")
                .contains("/stats")
                .contains("/newgame");
    }

    // ------------------------------------------------------------------ //
    //  /stats
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("/stats вызывает gameService и отправляет форматированные данные")
    void stats_callsServiceAndReturnsFormattedStats() {
        Long chatId = 55L;
        String formattedStats = "👤 Татьяна — дом\n⚡ Энергия: ██████░░░░ 60";
        when(gameService.getStats(String.valueOf(chatId))).thenReturn(formattedStats);

        Update update = buildTextUpdate(chatId, "/stats");
        bot.onUpdateReceived(update);

        verify(gameService).getStats(String.valueOf(chatId));

        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        verify(bot).sendMarkdownMessage(eq(chatId), textCaptor.capture(), any());
        assertThat(textCaptor.getValue()).isEqualTo(formattedStats);
    }

    @Test
    @DisplayName("/stats при ошибке сервиса отправляет сообщение об ошибке")
    void stats_whenServiceThrows_sendsErrorMessage() {
        Long chatId = 77L;
        when(gameService.getStats(anyString())).thenThrow(new RuntimeException("DB down"));

        Update update = buildTextUpdate(chatId, "/stats");
        bot.onUpdateReceived(update);

        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        verify(bot).sendMarkdownMessage(eq(chatId), textCaptor.capture(), any());
        assertThat(textCaptor.getValue()).contains("Не удалось загрузить");
    }

    // ------------------------------------------------------------------ //
    //  /newgame
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("/newgame создаёт новую сессию и отправляет подтверждение")
    void newgame_startsNewSessionAndSendsConfirmation() {
        Long chatId = 42L;

        Update update = buildTextUpdate(chatId, "/newgame");
        bot.onUpdateReceived(update);

        verify(gameService).startNewGame(String.valueOf(chatId));

        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        verify(bot).sendMarkdownMessage(eq(chatId), textCaptor.capture(), any());
        assertThat(textCaptor.getValue()).contains("Новая игра");
    }

    // ------------------------------------------------------------------ //
    //  Неизвестная команда
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Неизвестная команда обрабатывается без исключений")
    void unknownCommand_handledGracefully() {
        Update update = buildTextUpdate(1L, "/unknowncmd");

        assertThatCode(() -> bot.onUpdateReceived(update)).doesNotThrowAnyException();

        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        verify(bot).sendMarkdownMessage(eq(1L), textCaptor.capture(), any());
        assertThat(textCaptor.getValue()).contains("не понял");
    }

    // ------------------------------------------------------------------ //
    //  Утилиты для создания тестовых объектов
    // ------------------------------------------------------------------ //

    private Update buildTextUpdate(Long chatId, String text) {
        Message message = mock(Message.class);
        when(message.getChatId()).thenReturn(chatId);
        when(message.hasText()).thenReturn(true);
        when(message.hasWebAppData()).thenReturn(false);
        when(message.getText()).thenReturn(text);

        Update update = mock(Update.class);
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        return update;
    }
}
