package ru.lifegame.backend.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.lifegame.backend.domain.action.ActionResult;
import ru.lifegame.backend.domain.action.impl.GoToWorkAction;
import ru.lifegame.backend.domain.action.impl.RestAtHomeAction;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.session.GameSession;
import ru.lifegame.backend.domain.model.session.GameTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("GameSession — игровая сессия")
class GameSessionTest {

    @Test
    @DisplayName("createNew создаёт сессию с корректными начальными значениями")
    void createNewInitializesCorrectly() {
        GameSession session = GameSession.createNew("user-123");

        assertThat(session.telegramUserId()).isEqualTo("user-123");
        assertThat(session.sessionId()).isNotBlank();
        assertThat(session.time().day()).isEqualTo(1);
        assertThat(session.time().hour()).isEqualTo(GameBalance.DAY_START_HOUR);
        assertThat(session.player()).isNotNull();
        assertThat(session.relationships()).isNotNull();
        assertThat(session.pets()).isNotNull();
        assertThat(session.isGameOver()).isFalse();
        assertThat(session.ending()).isNull();
    }

    @Test
    @DisplayName("Две разные сессии имеют уникальные sessionId")
    void twoSessionsHaveUniqueIds() {
        GameSession s1 = GameSession.createNew("user-1");
        GameSession s2 = GameSession.createNew("user-2");

        assertThat(s1.sessionId()).isNotEqualTo(s2.sessionId());
    }

    @Test
    @DisplayName("executeAction корректно меняет состояние и возвращает ActionResult")
    void executeActionChangesState() {
        GameSession session = GameSession.createNew("user-action-test");
        int energyBefore = session.player().stats().energy();

        ActionResult result = session.executeAction(new GoToWorkAction());

        assertThat(result).isNotNull();
        assertThat(result.actionType()).isNotNull();
        assertThat(result.timeCost()).isGreaterThan(0);
        // Работа стоит энергии
        assertThat(session.player().stats().energy()).isLessThan(energyBefore);
        // Работа приносит деньги
        assertThat(session.player().stats().money())
                .isGreaterThan(GameBalance.INITIAL_MONEY);
    }

    @Test
    @DisplayName("executeAction: время продвигается вперёд после действия")
    void executeActionAdvancesTime() {
        GameSession session = GameSession.createNew("user-time-test");
        int hourBefore = session.time().hour();

        session.executeAction(new GoToWorkAction());

        assertThat(session.time().hour()).isGreaterThan(hourBefore);
    }

    @Test
    @DisplayName("endDay переходит на следующий день и сбрасывает время")
    void endDayAdvancesToNextDay() {
        GameSession session = GameSession.createNew("user-endday");
        int dayBefore = session.time().day();

        session.endDay();

        assertThat(session.time().day()).isEqualTo(dayBefore + 1);
        assertThat(session.time().hour()).isEqualTo(GameBalance.DAY_START_HOUR);
    }

    @Test
    @DisplayName("checkGameOver не фиксирует конец игры при нормальных характеристиках")
    void checkGameOverNoTriggerForNormalStats() {
        GameSession session = GameSession.createNew("user-gameover-check");

        session.checkGameOver();

        assertThat(session.isGameOver()).isFalse();
    }

    @Test
    @DisplayName("drainDomainEvents возвращает события после executeAction")
    void drainEventsAfterAction() {
        GameSession session = GameSession.createNew("user-events");

        session.executeAction(new GoToWorkAction());
        var events = session.drainDomainEvents();

        assertThat(events).isNotEmpty();
    }

    @Test
    @DisplayName("activeConflicts и events изначально пусты")
    void initialStateHasNoConflictsOrEvents() {
        GameSession session = GameSession.createNew("user-empty");

        assertThat(session.activeConflicts()).isEmpty();
        assertThat(session.events()).isEmpty();
    }

    @Test
    @DisplayName("GameTime: isDayOver истинно когда hour == 24")
    void gameTimeIsDayOverAtHour24() {
        // hour=0..23 допустимы при конструировании
        // advanceHours может довести до 24
        GameTime time = new GameTime(1, 20);
        GameTime advanced = time.advanceHours(4);

        assertThat(advanced.isDayOver()).isTrue();
        assertThat(advanced.hour()).isEqualTo(24);
    }

    @Test
    @DisplayName("GameTime: startNewDay создаёт новый день с начальным часом")
    void gameTimeStartNewDay() {
        GameTime time = new GameTime(3, 22);
        GameTime newDay = time.startNewDay();

        assertThat(newDay.day()).isEqualTo(4);
        assertThat(newDay.hour()).isEqualTo(GameBalance.DAY_START_HOUR);
    }
}
