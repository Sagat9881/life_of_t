package ru.lifegame.bot.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.lifegame.bot.client.BackendClient;

@Component
public class WebAppDataHandler {

    private static final Logger log = LoggerFactory.getLogger(WebAppDataHandler.class);

    private final BackendClient backendClient;

    public WebAppDataHandler(BackendClient backendClient) {
        this.backendClient = backendClient;
    }

    public void handle(Long chatId, String data) {
        try {
            log.info("Received WebApp data from chat {}: {}", chatId, data);

            // Parse the action from the WebApp data
            // Expected format: {"action": "WORK", "sessionId": "uuid"}
            backendClient.sendAction(chatId, data);

            log.info("Successfully processed WebApp data for chat {}", chatId);
        } catch (Exception e) {
            log.error("Failed to process WebApp data for chat {}: {}", chatId, e.getMessage(), e);
        }
    }
}
