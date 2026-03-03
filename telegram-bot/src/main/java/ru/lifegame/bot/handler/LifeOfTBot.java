package ru.lifegame.bot.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.WebAppData;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class LifeOfTBot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(LifeOfTBot.class);

    private final String botUsername;
    private final WebAppDataHandler webAppDataHandler;

    public LifeOfTBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername,
            WebAppDataHandler webAppDataHandler) {
        super(botToken);
        this.botUsername = botUsername;
        this.webAppDataHandler = webAppDataHandler;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                var message = update.getMessage();

                if (hasWebAppData(update)) {
                    WebAppData webAppData = message.getWebAppData();
                    webAppDataHandler.handle(message.getChatId(), webAppData.getData());
                } else if (message.hasText()) {
                    handleTextMessage(message.getChatId(), message.getText());
                }
            }
        } catch (Exception e) {
            log.error("Error processing update: {}", update.getUpdateId(), e);
        }
    }

    private boolean hasWebAppData(Update update) {
        return update.hasMessage()
                && update.getMessage().getWebAppData() != null
                && update.getMessage().getWebAppData().getData() != null
                && !update.getMessage().getWebAppData().getData().isBlank();
    }

    private void handleTextMessage(Long chatId, String text) {
        if ("/start".equals(text)) {
            sendWelcomeMessage(chatId);
        } else {
            sendHelpMessage(chatId);
        }
    }

    private void sendWelcomeMessage(Long chatId) {
        String welcomeText = """
                Welcome to Life of T! 🎮
                
                This is a life simulation game where you make decisions
                that shape your character's journey.
                
                Use the web app button below to start playing!
                """;
        sendTextMessage(chatId, welcomeText);
    }

    private void sendHelpMessage(Long chatId) {
        String helpText = """
                Life of T - Commands:
                
                /start - Start or resume your game
                
                Use the web app button to play the game interactively.
                """;
        sendTextMessage(chatId, helpText);
    }

    private void sendTextMessage(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .build();
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chat {}: {}", chatId, e.getMessage());
        }
    }
}
