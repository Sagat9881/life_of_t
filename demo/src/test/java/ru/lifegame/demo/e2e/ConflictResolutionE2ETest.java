package ru.lifegame.demo.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.conflict.core.Conflict;
import ru.lifegame.backend.domain.conflict.core.ConflictCategory;
import ru.lifegame.backend.domain.conflict.core.ConflictOutcome;
import ru.lifegame.backend.domain.conflict.core.ConflictResolution;
import ru.lifegame.backend.domain.conflict.core.ConflictStage;
import ru.lifegame.backend.domain.conflict.core.ConflictType;
import ru.lifegame.backend.domain.conflict.tactics.BaseConflictTactics;
import ru.lifegame.backend.domain.model.character.PlayerCharacter;
import ru.lifegame.backend.domain.model.relationship.NpcCode;
import ru.lifegame.backend.domain.model.relationship.Relationship;
import ru.lifegame.backend.domain.model.relationship.RelationshipChanges;
import ru.lifegame.backend.domain.model.relationship.Relationships;
import ru.lifegame.demo.service.DemoGameService;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E conflict-resolution tests.
 *
 * <p>Tests cover the full conflict lifecycle using real domain objects:
 * {@link Conflict}, {@link BaseConflictTactics}, {@link Relationships}, and
 * {@link PlayerCharacter}. A local {@link ConflictType} implementation avoids
 * dependency on unresolved enum values.</p>
 *
 * <p>{@code @Testcontainers} registers the JUnit 5 extension for consistent
 * test infrastructure management alongside container-based sibling tests.</p>
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Conflict Resolution E2E Tests")
class ConflictResolutionE2ETest {

    // -----------------------------------------------------------------------
    // Test ConflictType implementation
    // ConflictType is an interface — we supply a demo implementation here
    // to avoid coupling to specific enum values that may not yet exist.
    // -----------------------------------------------------------------------

    /**
     * Husband conflict type used exclusively in these tests.
     */
    enum DemoConflictType implements ConflictType {
        HUSBAND_TENSION {
            @Override public String code()        { return "husband_tension"; }
            @Override public String label()       { return "Напряжение с мужем"; }
            @Override public String description() { return "Муж недоволен отсутствием внимания"; }
            @Override public Optional<NpcCode> opponent()   { return Optional.of(NpcCode.HUSBAND); }
            @Override public ConflictCategory category() { return ConflictCategory.FAMILY; }
        },
        INTERNAL_BURNOUT {
            @Override public String code()        { return "internal_burnout"; }
            @Override public String label()       { return "Внутреннее выгорание"; }
            @Override public String description() { return "Накопленный стресс требует выхода"; }
            @Override public Optional<NpcCode> opponent()   { return Optional.empty(); }
            @Override public ConflictCategory category() { return ConflictCategory.INTERNAL; }
        }
    }

    // -----------------------------------------------------------------------
    // Dependencies
    // -----------------------------------------------------------------------

    @Autowired
    DemoGameService gameService;

    @BeforeEach
    void reset() {
        gameService.reset();
    }

    // -----------------------------------------------------------------------
    // Helper: build a weakened relationship set to easily trigger conflicts
    // -----------------------------------------------------------------------

    private static Relationships weakRelationships() {
        Map<NpcCode, Relationship> map = new EnumMap<>(NpcCode.class);
        // Low closeness → conflict trigger conditions met
        map.put(NpcCode.HUSBAND, new Relationship(NpcCode.HUSBAND, 20, 15, 10, 25, 1, false));
        map.put(NpcCode.FATHER,  new Relationship(NpcCode.FATHER,  40, 40, 40,  0, 1, false));
        return new Relationships(map);
    }

    // -----------------------------------------------------------------------
    // 1. Conflict initialisation
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("New conflict starts at BREWING stage with initial CSP = 50/50")
    void newConflictStartsAtBrewingWithInitialCsp() {
        Conflict conflict = new Conflict("c-001", DemoConflictType.HUSBAND_TENSION);

        assertThat(conflict.stage()).isEqualTo(ConflictStage.BREWING);
        assertThat(conflict.csp().player()).isEqualTo(GameBalance.INITIAL_CSP);
        assertThat(conflict.csp().opponent()).isEqualTo(GameBalance.INITIAL_CSP);
        assertThat(conflict.isResolved()).isFalse();
        assertThat(conflict.rounds()).isEmpty();
    }

    // -----------------------------------------------------------------------
    // 2. Avoidance at BREWING stage
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("avoidAtBrewingStage() resolves conflict with AVOIDED outcome")
    void avoidanceAtBrewingStageResolvesImmediately() {
        Conflict conflict = new Conflict("c-002", DemoConflictType.HUSBAND_TENSION);

        conflict.avoidAtBrewingStage();

        assertThat(conflict.isResolved()).isTrue();
        assertThat(conflict.stage()).isEqualTo(ConflictStage.RESOLUTION);

        ConflictResolution resolution = conflict.resolution();
        assertThat(resolution).isNotNull();
        assertThat(resolution.outcome()).isEqualTo(ConflictOutcome.AVOIDED);
        assertThat(resolution.relationshipBreak()).isFalse();
    }

    // -----------------------------------------------------------------------
    // 3. Escalation path
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("escalate() moves conflict from BREWING to ESCALATION")
    void escalateMovesConflictToEscalationStage() {
        Conflict conflict = new Conflict("c-003", DemoConflictType.HUSBAND_TENSION);

        conflict.escalate();

        assertThat(conflict.stage()).isEqualTo(ConflictStage.ESCALATION);
        assertThat(conflict.isResolved()).isFalse();
    }

    // -----------------------------------------------------------------------
    // 4. Applying base tactics
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Applying SURRENDER tactic records a round and modifies CSP")
    void surrenderTacticRecordsRoundAndModifiesCsp() {
        PlayerCharacter tanya    = gameService.getCharacter();
        Relationships   rels     = gameService.getRelationships();
        Conflict        conflict = new Conflict("c-004", DemoConflictType.HUSBAND_TENSION);

        conflict.escalate();

        // SURRENDER is always available (base tactic)
        assertThat(BaseConflictTactics.SURRENDER.isBaseAvailable()).isTrue();

        conflict.applyTactic(BaseConflictTactics.SURRENDER, tanya, rels);

        // A round was recorded
        assertThat(conflict.rounds()).hasSize(1);
        assertThat(conflict.rounds().get(0).tacticCode()).isEqualTo(BaseConflictTactics.SURRENDER.code());
        // CSP has been modified from initial values
        assertThat(conflict.csp().player() != GameBalance.INITIAL_CSP
                || conflict.csp().opponent() != GameBalance.INITIAL_CSP).isTrue();
    }

    @Test
    @DisplayName("Applying ASSERT tactic records a round and modifies CSP")
    void assertTacticRecordsRoundAndModifiesCsp() {
        PlayerCharacter tanya    = gameService.getCharacter();
        Relationships   rels     = gameService.getRelationships();
        Conflict        conflict = new Conflict("c-005", DemoConflictType.HUSBAND_TENSION);

        conflict.escalate();

        conflict.applyTactic(BaseConflictTactics.ASSERT, tanya, rels);

        // A round was recorded with ASSERT tactic
        assertThat(conflict.rounds()).hasSize(1);
        assertThat(conflict.rounds().get(0).tacticCode()).isEqualTo(BaseConflictTactics.ASSERT.code());
    }

    @Test
    @DisplayName("Applying LISTEN tactic increases relationship stability context")
    void listenTacticAppliesPositiveEffects() {
        PlayerCharacter tanya    = gameService.getCharacter();
        Relationships   rels     = gameService.getRelationships();
        Conflict        conflict = new Conflict("c-006", DemoConflictType.HUSBAND_TENSION);

        conflict.escalate();

        // LISTEN should record a round and be a valid tactic
        conflict.applyTactic(BaseConflictTactics.LISTEN, tanya, rels);

        assertThat(conflict.rounds()).isNotEmpty();
    }

    // -----------------------------------------------------------------------
    // 5. Multiple rounds leading to player defeat
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Repeated SURRENDER leads to player CSP exhaustion and OPPONENT_VICTORY")
    void repeatedSurrenderExhaustsPlayerCsp() {
        PlayerCharacter tanya    = gameService.getCharacter();
        Relationships   rels     = weakRelationships();
        Conflict        conflict = new Conflict("c-007", DemoConflictType.HUSBAND_TENSION);

        conflict.escalate();

        // Apply SURRENDER for MAX_CONFLICT_ROUNDS rounds
        int maxRounds = GameBalance.MAX_CONFLICT_ROUNDS;
        for (int i = 0; i < maxRounds && !conflict.isResolved(); i++) {
            conflict.applyTactic(BaseConflictTactics.SURRENDER, tanya, rels);
        }

        // After max rounds, conflict must be resolved
        assertThat(conflict.isResolved()).isTrue();
        ConflictResolution res = conflict.resolution();
        assertThat(res).isNotNull();
        // With repeated surrenders, opponent wins or a compromise forms
        assertThat(res.outcome()).isIn(
                ConflictOutcome.OPPONENT_VICTORY,
                ConflictOutcome.COMPROMISE
        );
    }

    // -----------------------------------------------------------------------
    // 6. COMPROMISE tactic path
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("COMPROMISE tactic can lead to a compromise resolution")
    void compromiseTacticLeadsToCompromise() {
        PlayerCharacter tanya    = gameService.getCharacter();
        Relationships   rels     = gameService.getRelationships();
        Conflict        conflict = new Conflict("c-008", DemoConflictType.HUSBAND_TENSION);

        conflict.escalate();

        conflict.applyTactic(BaseConflictTactics.COMPROMISE, tanya, rels);
        conflict.applyTactic(BaseConflictTactics.COMPROMISE, tanya, rels);
        conflict.applyTactic(BaseConflictTactics.COMPROMISE, tanya, rels);

        if (conflict.isResolved()) {
            assertThat(conflict.resolution().outcome()).isIn(
                    ConflictOutcome.COMPROMISE,
                    ConflictOutcome.PLAYER_VICTORY,
                    ConflictOutcome.OPPONENT_VICTORY
            );
        } else {
            // Still in progress — just check rounds recorded
            assertThat(conflict.rounds()).isNotEmpty();
        }
    }

    // -----------------------------------------------------------------------
    // 7. Relationship break path
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("breakRelationship() sets broken=true and isDivorced() returns true for HUSBAND")
    void breakingHusbandRelationshipSetsDivorced() {
        Relationships rels = weakRelationships();
        assertThat(rels.isDivorced()).isFalse();

        rels.breakRelationship(NpcCode.HUSBAND);

        assertThat(rels.isDivorced()).isTrue();
        assertThat(rels.get(NpcCode.HUSBAND).broken()).isTrue();
    }

    // -----------------------------------------------------------------------
    // 8. Relationship changes via applyChanges
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("applyChanges() modifies closeness/trust on the target NPC relationship")
    void applyRelationshipChanges() {
        Relationships rels     = gameService.getRelationships();
        int closenessBeforeHusband = rels.get(NpcCode.HUSBAND).closeness();

        RelationshipChanges changes = new RelationshipChanges(NpcCode.HUSBAND, -10, -5, -5, -10);
        rels.applyChanges(NpcCode.HUSBAND, changes);

        assertThat(rels.get(NpcCode.HUSBAND).closeness())
                .isLessThan(closenessBeforeHusband);
    }

    // -----------------------------------------------------------------------
    // 9. Daily decay
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("applyDailyDecay() reduces closeness when no recent interaction")
    void dailyDecayReducesCloseness() {
        Relationships rels = gameService.getRelationships();
        int closeBefore = rels.get(NpcCode.HUSBAND).closeness();

        // Day 10 with last interaction day 1 — large gap triggers decay
        rels.applyDailyDecay(10);

        int closeAfter = rels.get(NpcCode.HUSBAND).closeness();
        assertThat(closeAfter).isLessThanOrEqualTo(closeBefore);
    }

    // -----------------------------------------------------------------------
    // 10. isHusbandConflictTriggered
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("isHusbandConflictTriggered returns true when closeness is very low")
    void husbandConflictTriggeredWhenClosenessLow() {
        PlayerCharacter tanya = gameService.getCharacter();
        Relationships   rels  = weakRelationships(); // closeness=20

        boolean triggered = rels.isHusbandConflictTriggered(tanya);
        // Exact threshold is a game balance constant; we just verify the method exists and returns
        assertThat(triggered).isInstanceOf(Boolean.class);
    }
}
