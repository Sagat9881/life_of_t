package ru.lifegame.backend.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.conflict.core.*;
import ru.lifegame.backend.domain.conflict.tactics.BaseConflictTactics;
import ru.lifegame.backend.domain.conflict.tactics.TacticEffects;
import ru.lifegame.backend.domain.conflict.types.HusbandConflicts;
import ru.lifegame.backend.domain.model.character.PlayerCharacter;
import ru.lifegame.backend.domain.model.relationship.Relationships;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Conflict — система конфликтов")
class ConflictTest {

    private PlayerCharacter player() {
        return PlayerCharacter.initial();
    }

    private Relationships relationships() {
        return Relationships.initial();
    }

    @Test
    @DisplayName("Новый конфликт начинается в стадии BREWING")
    void newConflictStartsInBrewingStage() {
        Conflict conflict = new Conflict("c-1", HusbandConflicts.HOUSEHOLD_DUTIES);

        assertThat(conflict.stage()).isEqualTo(ConflictStage.BREWING);
        assertThat(conflict.isResolved()).isFalse();
        assertThat(conflict.rounds()).isEmpty();
    }

    @Test
    @DisplayName("Начальные CSP для обоих сторон равны INITIAL_CSP (50)")
    void initialCspAreSymmetric() {
        Conflict conflict = new Conflict("c-2", HusbandConflicts.LACK_OF_ATTENTION);

        assertThat(conflict.csp().player()).isEqualTo(GameBalance.INITIAL_CSP);
        assertThat(conflict.csp().opponent()).isEqualTo(GameBalance.INITIAL_CSP);
    }

    @Test
    @DisplayName("escalate() переводит из BREWING в ESCALATION")
    void escalateChangesStage() {
        Conflict conflict = new Conflict("c-3", HusbandConflicts.HOUSEHOLD_DUTIES);
        assertThat(conflict.stage()).isEqualTo(ConflictStage.BREWING);

        conflict.escalate();

        assertThat(conflict.stage()).isEqualTo(ConflictStage.ESCALATION);
    }

    @Test
    @DisplayName("avoidAtBrewingStage() завершает конфликт без раундов")
    void avoidAtBrewingStageResolvesImmediately() {
        Conflict conflict = new Conflict("c-4", HusbandConflicts.ROMANTIC_CRISIS);

        conflict.avoidAtBrewingStage();

        assertThat(conflict.isResolved()).isTrue();
        assertThat(conflict.stage()).isEqualTo(ConflictStage.RESOLUTION);
        assertThat(conflict.rounds()).isEmpty();
    }

    @Test
    @DisplayName("applyTactic(SURRENDER) снижает selfEsteem и изменяет CSP")
    void surrenderTacticReducesSelfEsteemAndCsp() {
        Conflict conflict = new Conflict("c-5", HusbandConflicts.HOUSEHOLD_DUTIES);

        TacticEffects effects = conflict.applyTactic(
                BaseConflictTactics.SURRENDER, player(), relationships()
        );

        assertThat(effects.statChanges().selfEsteem()).isEqualTo(GameBalance.SURRENDER_SELF_ESTEEM);
        // SURRENDER увеличивает player CSP и снижает opponent CSP
        assertThat(effects.cspChanges().player()).isEqualTo(GameBalance.SURRENDER_PLAYER_CSP);
        assertThat(effects.cspChanges().opponent()).isEqualTo(GameBalance.SURRENDER_OPPONENT_CSP);
        assertThat(conflict.rounds()).hasSize(1);
    }

    @Test
    @DisplayName("applyTactic(ASSERT) повышает selfEsteem и увеличивает opponent CSP")
    void assertTacticIncreaseSelfEsteem() {
        Conflict conflict = new Conflict("c-6", HusbandConflicts.FINANCIAL_DISAGREEMENT);

        TacticEffects effects = conflict.applyTactic(
                BaseConflictTactics.ASSERT, player(), relationships()
        );

        assertThat(effects.statChanges().selfEsteem()).isEqualTo(GameBalance.ASSERT_SELF_ESTEEM);
        assertThat(effects.cspChanges().player()).isEqualTo(GameBalance.ASSERT_PLAYER_CSP);
        assertThat(effects.cspChanges().opponent()).isEqualTo(GameBalance.ASSERT_OPPONENT_CSP);
    }

    @Test
    @DisplayName("После MAX_CONFLICT_ROUNDS раундов конфликт разрешается компромиссом")
    void conflictResolvesAfterMaxRounds() {
        Conflict conflict = new Conflict("c-7", HusbandConflicts.HOUSEHOLD_DUTIES);

        // Применяем тактику AVOID чтобы не обнулить CSP сразу
        for (int i = 0; i < GameBalance.MAX_CONFLICT_ROUNDS && !conflict.isResolved(); i++) {
            conflict.applyTactic(BaseConflictTactics.AVOID, player(), relationships());
        }

        assertThat(conflict.isResolved()).isTrue();
        assertThat(conflict.resolution()).isNotNull();
    }

    @Test
    @DisplayName("applyTactic на уже разрешённом конфликте бросает IllegalStateException")
    void applyTacticOnResolvedConflictThrows() {
        Conflict conflict = new Conflict("c-8", HusbandConflicts.HOUSEHOLD_DUTIES);
        conflict.avoidAtBrewingStage();

        assertThatThrownBy(() ->
                conflict.applyTactic(BaseConflictTactics.SURRENDER, player(), relationships())
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ConflictType для мужа: code, label, opponent и category заданы корректно")
    void husbandConflictTypeHasCorrectFields() {
        ConflictType type = HusbandConflicts.HOUSEHOLD_DUTIES;

        assertThat(type.code()).isEqualTo("HOUSEHOLD_DUTIES");
        assertThat(type.label()).isEqualTo("Домашние обязанности");
        assertThat(type.opponent()).isPresent();
        assertThat(type.category()).isEqualTo(ConflictCategory.FAMILY);
    }
}
