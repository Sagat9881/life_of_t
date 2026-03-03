package ru.lifegame.bot.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.WebAppData;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LifeOfTBotTest {

    @Mock
    private WebAppDataHandler webAppDataHandler;

    private LifeOfTBot bot;

    @BeforeEach
    void setUp() {
        bot = new LifeOfTBot("test-token", "test-bot", webAppDataHandler);
    }

    @Test
    void onUpdateReceived_withWebAppData_callsHandler() {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        WebAppData webAppData = mock(WebAppData.class);

        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.getWebAppData()).thenReturn(webAppData);
        when(webAppData.getData()).thenReturn("{\"action\":\"WORK\"}");
        when(message.getChatId()).thenReturn(12345L);

        bot.onUpdateReceived(update);

        verify(webAppDataHandler).handle(12345L, "{\"action\":\"WORK\"}");
    }

    @Test
    void onUpdateReceived_withTextMessage_noWebAppHandler() {
        Update update = mock(Update.class);
        Message message = mock(Message.class);

        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.getWebAppData()).thenReturn(null);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn("/start");
        when(message.getChatId()).thenReturn(12345L);

        bot.onUpdateReceived(update);

        verify(webAppDataHandler, never()).handle(any(), any());
    }

    @Test
    void onUpdateReceived_noMessage_doesNothing() {
        Update update = mock(Update.class);
        when(update.hasMessage()).thenReturn(false);

        bot.onUpdateReceived(update);

        verify(webAppDataHandler, never()).handle(any(), any());
    }

    @Test
    void getBotUsername_returnsConfiguredUsername() {
        assert bot.getBotUsername().equals("test-bot");
    }
}
