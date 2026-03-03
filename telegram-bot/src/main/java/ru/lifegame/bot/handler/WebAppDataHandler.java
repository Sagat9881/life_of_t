package ru.lifegame.bot.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.lifegame.bot.service.TelegramGameService;

/**
 * Обработчик данных, приходящих из Telegram Mini App через механизм web_app_data.
 *
 * <p>Telegram Mini App может отправлять данные боту через метод
 * {@code Telegram.WebApp.sendData(data)}. Эти данные приходят в поле
 * {@code message.web_app_data.data} в виде строки (обычно JSON).
 *
 * <p>Формат ожидаемых команд:
 * <pre>
 * {"action": "END_DAY"}
 * {"action": "EXECUTE_ACTION", "actionId": "go_to_work"}
 * {"action": "NEW_GAME"}
 * </pre>
 */
@Component
public class WebAppDataHandler {

    private static final Logger log = LoggerFactory.getLogger(WebAppDataHandler.class);

    private static final String ACTION_END_DAY        = "END_DAY";
    private static final String ACTION_EXECUTE_ACTION = "EXECUTE_ACTION";
    private static final String ACTION_NEW_GAME       = "NEW_GAME";

    private final TelegramGameService gameService;

    public WebAppDataHandler(TelegramGameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Обрабатывает входящее сообщение с web_app_data.
     *
     * @param bot     экземпляр бота для отправки ответов
     * @param message входящее сообщение от пользователя
     */
    public void handle(LifeOfTBot bot, Message message) {
        if (!message.hasWebAppData()) {
            return;
        }

        String rawData = message.getWebAppData().getData();
        Long chatId = message.getChatId();

        log.debug("Получены данные от Mini App, chatId={}, data={}", chatId, rawData);

        try {
            String action = extractAction(rawData);
            processAction(bot, chatId, action, rawData);
        } catch (Exception e) {
            log.error("Ошибка обработки web_app_data, chatId={}: {}", chatId, e.getMessage(), e);
            bot.sendMarkdownMessage(chatId,
                    "⚠️ Произошла ошибка при обработке действия\\. Попробуйте снова\\.", null);
        }
    }

    private void processAction(LifeOfTBot bot, Long chatId, String action, String rawData) {
        switch (action) {
            case ACTION_NEW_GAME -> {
                gameService.startNewGame(String.valueOf(chatId));
                bot.sendMarkdownMessage(chatId,
                        "🌟 *Новая игра началась\!* Удачи, Татьяна\!", null);
            }
            case ACTION_END_DAY -> {
                String stats = gameService.getStats(String.valueOf(chatId));
                bot.sendMarkdownMessage(chatId,
                        "🌙 *День завершён\!*\n\n" + stats, null);
            }
            case ACTION_EXECUTE_ACTION -> {
                log.debug("Выполнено действие через Mini App: {}", rawData);
                // Действие уже обработано фронтендом через REST API;
                // бот только подтверждает получение, не дублируя логику
            }
            default -> log.warn("Неизвестное действие от Mini App: {}", action);
        }
    }

    /**
     * Примитивный парсер JSON: извлекает значение поля "action" без внешних зависимостей.
     * Для полноценного парсинга используйте Jackson ObjectMapper.
     */
    private String extractAction(String json) {
        if (json == null || json.isBlank()) return "";
        // Ищем "action": "VALUE"
        int keyIdx = json.indexOf("\"action\"");
        if (keyIdx < 0) return "";
        int colonIdx = json.indexOf(':', keyIdx);
        if (colonIdx < 0) return "";
        int openQuote = json.indexOf('"', colonIdx + 1);
        if (openQuote < 0) return "";
        int closeQuote = json.indexOf('"', openQuote + 1);
        if (closeQuote < 0) return "";
        return json.substring(openQuote + 1, closeQuote);
    }
}
