package ru.lifegame.backend.domain.model;

import org.junit.jupiter.api.Test;
import ru.lifegame.backend.domain.model.session.GameTime;

import static org.junit.jupiter.api.Assertions.*;

class GameTimeTest {

    @Test
    void initialTimeIsDay1Hour8() {
        GameTime time = GameTime.initial();
        assertEquals(1, time.day());
        assertEquals(8, time.hour());
    }

    @Test
    void advanceHoursWorks() {
        GameTime time = GameTime.initial().advanceHours(3);
        assertEquals(11, time.hour());
    }

    @Test
    void hasEnoughTimeChecksCorrectly() {
        GameTime time = GameTime.initial(); // hour 8, end at 24 -> 16 hours left
        assertTrue(time.hasEnoughTime(8));
        assertTrue(time.hasEnoughTime(16));
        assertFalse(time.hasEnoughTime(17));
    }

    @Test
    void startNewDayResetsHour() {
        GameTime time = GameTime.initial().advanceHours(10);
        GameTime nextDay = time.startNewDay();
        assertEquals(2, nextDay.day());
        assertEquals(8, nextDay.hour());
    }
}
