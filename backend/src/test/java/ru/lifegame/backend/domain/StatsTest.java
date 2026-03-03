package ru.lifegame.backend.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.stats.StatChanges;
import ru.lifegame.backend.domain.model.stats.Stats;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Stats — характеристики персонажа")
class StatsTest {

    @Test
    @DisplayName("Начальные значения соответствуют константам GameBalance")
    void initialStatsMatchGameBalance() {
        Stats stats = Stats.initial();

        assertThat(stats.energy()).isEqualTo(GameBalance.INITIAL_ENERGY);
        assertThat(stats.health()).isEqualTo(GameBalance.INITIAL_HEALTH);
        assertThat(stats.stress()).isEqualTo(GameBalance.INITIAL_STRESS);
        assertThat(stats.mood()).isEqualTo(GameBalance.INITIAL_MOOD);
        assertThat(stats.money()).isEqualTo(GameBalance.INITIAL_MONEY);
        assertThat(stats.selfEsteem()).isEqualTo(GameBalance.INITIAL_SELF_ESTEEM);
    }

    @Test
    @DisplayName("Clamping: energy, health, stress, mood, selfEsteem зажимаются в [0, 100]")
    void clampedStatsStayInBounds() {
        // Превышение максимума
        Stats overMax = new Stats(200, 150, 120, 110, 9999, 999);
        assertThat(overMax.energy()).isEqualTo(100);
        assertThat(overMax.health()).isEqualTo(100);
        assertThat(overMax.stress()).isEqualTo(100);
        assertThat(overMax.mood()).isEqualTo(100);
        assertThat(overMax.selfEsteem()).isEqualTo(100);
        // money НЕ зажимается
        assertThat(overMax.money()).isEqualTo(9999);

        // Ниже минимума
        Stats underMin = new Stats(-10, -5, -1, -100, -9999, -3);
        assertThat(underMin.energy()).isEqualTo(0);
        assertThat(underMin.health()).isEqualTo(0);
        assertThat(underMin.stress()).isEqualTo(0);
        assertThat(underMin.mood()).isEqualTo(0);
        assertThat(underMin.selfEsteem()).isEqualTo(0);
        // money может быть отрицательным
        assertThat(underMin.money()).isEqualTo(-9999);
    }

    @Test
    @DisplayName("money НЕ зажимается — может быть отрицательным для детектирования банкротства")
    void moneyIsNotClamped() {
        Stats bankruptStats = new Stats(50, 50, 50, 50, GameBalance.BANKRUPTCY_THRESHOLD, 50);
        assertThat(bankruptStats.money()).isEqualTo(GameBalance.BANKRUPTCY_THRESHOLD);

        Stats deepNegative = new Stats(50, 50, 50, 50, -1000, 50);
        assertThat(deepNegative.money()).isEqualTo(-1000);
    }

    @Test
    @DisplayName("apply(StatChanges) корректно изменяет все характеристики")
    void applyChangesCorrectly() {
        Stats base = new Stats(70, 80, 20, 60, 1000, 50);
        StatChanges changes = new StatChanges(-10, 5, 15, -5, 200, -3);

        Stats result = base.apply(changes);

        assertThat(result.energy()).isEqualTo(60);
        assertThat(result.health()).isEqualTo(85);
        assertThat(result.stress()).isEqualTo(35);
        assertThat(result.mood()).isEqualTo(55);
        assertThat(result.money()).isEqualTo(1200);
        assertThat(result.selfEsteem()).isEqualTo(47);
    }

    @Test
    @DisplayName("apply(StatChanges) c зажиманием не выходит за пределы для зажатых полей")
    void applyChangesWithClampingAtBoundaries() {
        Stats nearMax = new Stats(95, 95, 95, 95, 100, 95);
        StatChanges bigPositive = new StatChanges(20, 20, 20, 20, 20, 20);

        Stats result = nearMax.apply(bigPositive);

        assertThat(result.energy()).isEqualTo(100);
        assertThat(result.health()).isEqualTo(100);
        assertThat(result.stress()).isEqualTo(100);
        assertThat(result.mood()).isEqualTo(100);
        assertThat(result.selfEsteem()).isEqualTo(100);
        // money не зажимается — может превышать 100
        assertThat(result.money()).isEqualTo(120);
    }

    @Test
    @DisplayName("changeEnergy/changeStress/changeMood — вспомогательные методы работают корректно")
    void helperChangeMethodsWork() {
        Stats base = new Stats(50, 50, 30, 60, 500, 50);

        assertThat(base.changeEnergy(20).energy()).isEqualTo(70);
        assertThat(base.changeEnergy(-60).energy()).isEqualTo(0); // зажимается

        assertThat(base.changeStress(30).stress()).isEqualTo(60);
        assertThat(base.changeStress(-40).stress()).isEqualTo(0); // зажимается

        assertThat(base.changeMood(25).mood()).isEqualTo(85);
        assertThat(base.changeMood(-70).mood()).isEqualTo(0); // зажимается
    }

    @Test
    @DisplayName("StatChanges.none() не меняет характеристики")
    void noneChangesDoNothing() {
        Stats original = Stats.initial();
        Stats afterNone = original.apply(StatChanges.none());

        assertThat(afterNone).isEqualTo(original);
    }
}
