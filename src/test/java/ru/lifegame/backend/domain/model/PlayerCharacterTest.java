package ru.lifegame.backend.domain.model;

import org.junit.jupiter.api.Test;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.GameTime;
import ru.lifegame.backend.domain.model.PlayerCharacter;
import ru.lifegame.backend.domain.model.StatChanges;

import static org.assertj.core.api.Assertions.*;

class PlayerCharacterTest {

    @Test
    void isBurnedOut_shouldReturnTrue_whenBurnoutRiskExceedsThreshold() {
        PlayerCharacter player = PlayerCharacter.initial();
        // Симулируем высокий burnout через многократный end-of-day с высоким стрессом
        for (int i = 0; i < 30; i++) {
            player.applyStatChanges(new StatChanges(0, 0, 10, 0, 0, 0));
            player.applyEndOfDayDecay();
        }
        assertThat(player.isBurnedOut()).isTrue();
    }

    @Test
    void canPerformAction_shouldReturnFalse_whenNotEnoughTime() {
        PlayerCharacter player = PlayerCharacter.initial();
        GameTime lateTime = new GameTime(1, 23);
        boolean canAct = player.canPerformAction(
                ru.lifegame.backend.domain.action.Actions.GO_TO_WORK, lateTime, 8
        );
        assertThat(canAct).isFalse();
    }
}
