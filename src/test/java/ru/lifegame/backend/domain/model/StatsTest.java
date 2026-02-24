package ru.lifegame.backend.domain.model;

import org.junit.jupiter.api.Test;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.StatChanges;
import ru.lifegame.backend.domain.model.Stats;

import static org.assertj.core.api.Assertions.*;

class StatsTest {

    @Test
    void apply_shouldClampValues() {
        Stats stats = Stats.initial();
        StatChanges extreme = new StatChanges(-200, -200, 200, -200, -5000, -200);
        Stats result = stats.apply(extreme);

        assertThat(result.energy()).isEqualTo(0);
        assertThat(result.health()).isEqualTo(0);
        assertThat(result.stress()).isEqualTo(GameBalance.STAT_MAX);
        assertThat(result.mood()).isEqualTo(0);
        assertThat(result.selfEsteem()).isEqualTo(0);
    }

    @Test
    void changeEnergy_shouldNotExceedMax() {
        Stats stats = Stats.initial();
        Stats result = stats.changeEnergy(500);
        assertThat(result.energy()).isEqualTo(GameBalance.STAT_MAX);
    }
}
