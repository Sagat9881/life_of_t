package ru.lifegame.bot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.lifegame.backend.application.command.StartSessionCommand;
import ru.lifegame.backend.application.port.in.GetGameStateUseCase;
import ru.lifegame.backend.application.port.in.StartOrLoadSessionUseCase;
import ru.lifegame.backend.application.query.GetStateQuery;
import ru.lifegame.backend.application.view.GameStateView;
import ru.lifegame.backend.application.view.StatsView;

/**
 * Сервис, оборачивающий backend use cases для Telegram-контекста.
 *
 * <p>Отвечает за:
 * <ul>
 *   <li>Запуск или перезагрузку игровой сессии</li>
 *   <li>Получение текущего состояния игры</li>
 *   <li>Форматирование данных для отправки в Telegram (MarkdownV2)</li>
 * </ul>
 */
@Service
public class TelegramGameService {

    private static final Logger log = LoggerFactory.getLogger(TelegramGameService.class);

    private final StartOrLoadSessionUseCase startOrLoadSessionUseCase;
    private final GetGameStateUseCase getGameStateUseCase;

    public TelegramGameService(StartOrLoadSessionUseCase startOrLoadSessionUseCase,
                               GetGameStateUseCase getGameStateUseCase) {
        this.startOrLoadSessionUseCase = startOrLoadSessionUseCase;
        this.getGameStateUseCase = getGameStateUseCase;
    }

    /**
     * Запускает новую игровую сессию для указанного chatId.
     *
     * @param chatId идентификатор чата Telegram
     * @return созданное состояние игры
     */
    public GameStateView startNewGame(String chatId) {
        log.info("Запуск новой игры для chatId={}", chatId);
        StartSessionCommand command = new StartSessionCommand(chatId);
        return startOrLoadSessionUseCase.execute(command);
    }

    /**
     * Возвращает отформатированное сообщение со статистикой для указанного chatId.
     *
     * @param chatId идентификатор чата Telegram
     * @return строка в формате MarkdownV2 со статистикой персонажа
     */
    public String getStats(String chatId) {
        log.debug("Запрос статистики для chatId={}", chatId);
        GetStateQuery query = new GetStateQuery(chatId);
        GameStateView state = getGameStateUseCase.execute(query);
        return formatStatsMessage(state);
    }

    /**
     * Форматирует состояние игры в текстовое сообщение для Telegram (MarkdownV2).
     *
     * <p>Специальные символы в MarkdownV2 нужно экранировать обратным слешем:
     * {@code . , ! ? - ( ) [ ] { } > # + = | ~ `}
     *
     * @param state текущее состояние игры
     * @return отформатированное сообщение
     */
    public String formatStatsMessage(GameStateView state) {
        if (state == null) {
            return "⚠️ Игровая сессия не найдена\\. Начните игру командой /start";
        }

        StatsView stats = state.player() != null ? state.player().stats() : null;
        String playerName = state.player() != null ? state.player().name() : "Татьяна";
        String location = state.player() != null ? state.player().location() : "дом";

        // Информация о времени
        String dayInfo = "";
        if (state.time() != null) {
            String period = resolvePeriod(state.time().hour());
            dayInfo = "📅 День *%d*, %s\n".formatted(
                    state.time().day(),
                    escapeMarkdown(period));
        }

        if (stats == null) {
            return dayInfo + "⚠️ Статистика недоступна";
        }

        String energyBar   = buildBar(stats.energy(),   100);
        String healthBar   = buildBar(stats.health(),   100);
        String stressBar   = buildBar(stats.stress(),   100);
        String moodBar     = buildBar(stats.mood(),     100);

        return """
                👤 *%s* — %s

                %s
                ⚡ Энергия:      %s %d
                ❤️ Здоровье:    %s %d
                😰 Стресс:       %s %d
                😊 Настроение:  %s %d
                💰 Деньги:       *%d ₽*
                💎 Самооценка: *%d*
                """.formatted(
                escapeMarkdown(playerName),
                escapeMarkdown(location),
                dayInfo,
                energyBar,  stats.energy(),
                healthBar,  stats.health(),
                stressBar,  stats.stress(),
                moodBar,    stats.mood(),
                stats.money(),
                stats.selfEsteem()
        );
    }

    // ------------------------------------------------------------------ //
    //  Приватные утилиты
    // ------------------------------------------------------------------ //

    /**
     * Определяет текстовое название периода суток по номеру часа.
     */
    private String resolvePeriod(int hour) {
        if (hour >= 6  && hour < 12) return "утро";
        if (hour >= 12 && hour < 18) return "день";
        if (hour >= 18 && hour < 22) return "вечер";
        return "ночь";
    }

    /**
     * Строит визуальный прогресс-бар из 10 символов.
     *
     * @param value   текущее значение (0–max)
     * @param max     максимальное значение
     * @return строка вида «██████░░░░»
     */
    private String buildBar(int value, int max) {
        int filled = Math.min(10, Math.max(0, (int) Math.round(value * 10.0 / max)));
        return "█".repeat(filled) + "░".repeat(10 - filled);
    }

    /**
     * Экранирует специальные символы MarkdownV2.
     */
    private String escapeMarkdown(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
                .replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace("=", "\\=")
                .replace("|", "\\|")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace(".", "\\.")
                .replace("!", "\\!");
    }
}
