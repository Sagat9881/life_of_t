package ru.lifegame.bot.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.lifegame.bot.config.BotProperties;
import ru.lifegame.bot.service.TelegramGameService;

import java.util.List;

/**
 * Основной обработчик Telegram-бота «Лиф оф Т».
 * Поддерживает команды /start, /help, /stats, /newgame и обрабатывает
 * данные от Telegram Mini App (web_app_data).
 */
@Component
public class LifeOfTBot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(LifeOfTBot.class);

    private static final String CMD_START    = "/start";
    private static final String CMD_HELP     = "/help";
    private static final String CMD_STATS    = "/stats";
    private static final String CMD_NEWGAME  = "/newgame";

    private final BotProperties botProperties;
    private final TelegramGameService gameService;
    private final WebAppDataHandler webAppDataHandler;

    public LifeOfTBot(BotProperties botProperties,
                      TelegramGameService gameService,
                      WebAppDataHandler webAppDataHandler) {
        super(botProperties.token());
        this.botProperties = botProperties;
        this.gameService = gameService;
        this.webAppDataHandler = webAppDataHandler;
    }

    @Override
    public String getBotUsername() {
        return botProperties.username();
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                var message = update.getMessage();

                // Обработка данных от Mini App
                if (message.hasWebAppData()) {
                    webAppDataHandler.handle(this, message);
                    return;
                }

                if (message.hasText()) {
                    handleTextMessage(message.getChatId(), message.getText());
                }
            }
        } catch (Exception e) {
            log.error("Ошибка обработки обновления: {}", e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------ //
    //  Обработка текстовых команд
    // ------------------------------------------------------------------ //

    private void handleTextMessage(Long chatId, String text) {
        String command = extractCommand(text);
        switch (command) {
            case CMD_START   -> handleStart(chatId);
            case CMD_HELP    -> handleHelp(chatId);
            case CMD_STATS   -> handleStats(chatId);
            case CMD_NEWGAME -> handleNewGame(chatId);
            default          -> handleUnknown(chatId, text);
        }
    }

    /**
     * Извлекает команду из текста (убирает @username суффикс при необходимости).
     */
    private String extractCommand(String text) {
        if (text == null) return "";
        String trimmed = text.trim().split("\\s+")[0];
        int atIndex = trimmed.indexOf('@');
        return atIndex > 0 ? trimmed.substring(0, atIndex) : trimmed;
    }

    // ------------------------------------------------------------------ //
    //  /start
    // ------------------------------------------------------------------ //

    private void handleStart(Long chatId) {
        String welcomeText = """
                🌸 *Добро пожаловать в Life of T\!*

                Ты играешь за *Татьяну* — молодую женщину, которая каждый день делает выбор\\:
                работа или отдых, уют дома или прогулка с мужем, забота о питомце или себе\\.

                Жизнь — это баланс\\. Найди свой путь\\.

                Нажми кнопку ниже, чтобы начать игру:
                """;

        InlineKeyboardButton playButton = InlineKeyboardButton.builder()
                .text("🎮 Играть")
                .webApp(new org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo(
                        botProperties.webappUrl()))
                .build();

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(playButton))
                .build();

        sendMarkdownMessage(chatId, welcomeText, keyboard);
    }

    // ------------------------------------------------------------------ //
    //  /help
    // ------------------------------------------------------------------ //

    private void handleHelp(Long chatId) {
        String helpText = """
                📖 *Life of T — Справка*

                *Об игре:*
                Уютный life\-sim симулятор, вдохновлённый Punch Club\.
                Управляй ежедневными делами Татьяны и находи баланс между работой, семьёй и здоровьем\.

                *Характеристики:*
                • ⚡ *Энергия* — тратится на действия, восполняется сном
                • ❤️ *Здоровье* — зависит от питания и физической активности
                • 😌 *Стресс* — накапливается на работе, снимается отдыхом
                • 😊 *Настроение* — влияет на эффективность всех действий
                • 💰 *Деньги* — расходуются на еду, досуг, питомца
                • 💎 *Самооценка* — растёт с достижениями

                *Команды:*
                /start — открыть игру
                /stats — показать текущую статистику
                /newgame — начать новую игру
                /help — эта справка

                *Советы:*
                • Следи за энергией — без неё действия недоступны
                • Позволяй Татьяне отдыхать — стресс накапливается незаметно
                • Общение с мужем и отцом улучшает настроение
                • Питомцы требуют заботы, но приносят радость
                """;

        sendMarkdownMessage(chatId, helpText, null);
    }

    // ------------------------------------------------------------------ //
    //  /stats
    // ------------------------------------------------------------------ //

    private void handleStats(Long chatId) {
        try {
            String statsMessage = gameService.getStats(String.valueOf(chatId));
            sendMarkdownMessage(chatId, statsMessage, null);
        } catch (Exception e) {
            log.warn("Не удалось получить статистику для chatId={}: {}", chatId, e.getMessage());
            sendMarkdownMessage(chatId,
                    "⚠️ Не удалось загрузить статистику\\. Начните игру командой /start",
                    null);
        }
    }

    // ------------------------------------------------------------------ //
    //  /newgame
    // ------------------------------------------------------------------ //

    private void handleNewGame(Long chatId) {
        try {
            gameService.startNewGame(String.valueOf(chatId));
            String text = """
                    🌟 *Новая игра началась\!*

                    Татьяна проснулась в понедельник утром\.
                    Впереди целая неделя новых возможностей\.

                    Нажми /start чтобы открыть игру\.
                    """;
            sendMarkdownMessage(chatId, text, null);
        } catch (Exception e) {
            log.error("Ошибка запуска новой игры для chatId={}: {}", chatId, e.getMessage(), e);
            sendMarkdownMessage(chatId,
                    "❌ Не удалось начать новую игру\\. Попробуйте позже\\.",
                    null);
        }
    }

    // ------------------------------------------------------------------ //
    //  Неизвестная команда
    // ------------------------------------------------------------------ //

    private void handleUnknown(Long chatId, String text) {
        log.debug("Получено неизвестное сообщение от chatId={}: {}", chatId, text);
        String response = """
                🤔 Я не понял эту команду\.

                Доступные команды:
                /start — открыть игру
                /stats — статистика
                /newgame — новая игра
                /help — справка
                """;
        sendMarkdownMessage(chatId, response, null);
    }

    // ------------------------------------------------------------------ //
    //  Утилита отправки
    // ------------------------------------------------------------------ //

    /**
     * Отправляет сообщение с MarkdownV2-разметкой.
     * Если клавиатура null — отправляется без неё.
     */
    public void sendMarkdownMessage(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage.SendMessageBuilder builder = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode("MarkdownV2");

        if (keyboard != null) {
            builder.replyMarkup(keyboard);
        }

        try {
            execute(builder.build());
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения chatId={}: {}", chatId, e.getMessage(), e);
        }
    }
}
