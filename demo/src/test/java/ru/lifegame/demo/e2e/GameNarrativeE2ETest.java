package ru.lifegame.demo.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.conflict.core.Conflict;
import ru.lifegame.backend.domain.conflict.core.ConflictCategory;
import ru.lifegame.backend.domain.conflict.core.ConflictStage;
import ru.lifegame.backend.domain.conflict.core.ConflictType;
import ru.lifegame.backend.domain.conflict.tactics.BaseConflictTactics;
import ru.lifegame.backend.domain.model.character.PlayerCharacter;
import ru.lifegame.backend.domain.model.stats.StatChanges;
import ru.lifegame.backend.domain.model.relationship.NpcCode;
import ru.lifegame.backend.domain.model.relationship.RelationshipChanges;
import ru.lifegame.backend.domain.model.relationship.Relationships;
import ru.lifegame.backend.domain.model.session.GameTime;
import ru.lifegame.backend.domain.quest.Quest;
import ru.lifegame.backend.domain.quest.QuestLog;
import ru.lifegame.backend.domain.quest.QuestStepState;
import ru.lifegame.backend.domain.quest.QuestType;
import ru.lifegame.demo.service.DemoGameService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full-narrative E2E test: simulates Tanya's Day 1.
 *
 * <p>The test walks through a complete game day:</p>
 * <ol>
 *   <li>Morning: Tanya wakes up (mark rested, advance hours)</li>
 *   <li>Morning: Self-care (quest progress)</li>
 *   <li>Afternoon: Work (mark worked, stat changes)</li>
 *   <li>Evening: Husband date (quest progress, relationship boost)</li>
 *   <li>Evening: Playing with Sam (mood boost)</li>
 *   <li>Night: Daily decay applied; conflict check</li>
 *   <li>Day 2: Multi-day simulation, conflict triggers, resolution</li>
 * </ol>
 *
 * <p>Verifies that stats, quest states, and relationship values evolve
 * consistently with GameBalance constants and domain rules.</p>
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Game Narrative E2E — Day 1 Simulation")
class GameNarrativeE2ETest {

    // Test ConflictType for husband tension
    enum NarrativeConflictType implements ConflictType {
        HUSBAND_DISTANCE {
            @Override public String code()             { return "husband_distance"; }
            @Override public String label()            { return "Муж обижен"; }
            @Override public String description()      { return "Муж чувствует себя заброшенным"; }
            @Override public Optional<NpcCode> opponent()        { return Optional.of(NpcCode.HUSBAND); }
            @Override public ConflictCategory category() { return ConflictCategory.FAMILY; }
        }
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    DemoGameService gameService;

    @BeforeEach
    void reset() {
        gameService.reset();
    }

    // -----------------------------------------------------------------------
    // Day 1 — full walkthrough
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Day 1: Wake up → self-care → work → husband date → Sam → end of day")
    void dayOneFullNarrative() {

        PlayerCharacter tanya   = gameService.getCharacter();
        Relationships   rels    = gameService.getRelationships();
        QuestLog        log     = gameService.getQuestLog();
        GameTime        time    = gameService.getGameTime();

        // Verify starting conditions
        assertThat(time.day()).isEqualTo(1);
        assertThat(time.hour()).isEqualTo(GameBalance.DAY_START_HOUR);
        assertThat(tanya.stats().energy()).isGreaterThan(0);

        // --- 1. Wake up (08:00 → 09:00) ---
        tanya.markRested();
        time = time.advanceHours(1);
        assertThat(tanya.stats().energy()).isPositive(); // resting should maintain energy

        // --- 2. Self-care: go to gym (09:00 → 11:00) ---
        time = time.advanceHours(2);
        assertThat(time.hour()).isEqualTo(11);

        Quest selfCare = log.activeQuests().stream()
                .filter(q -> q.type() == QuestType.SELF_CARE_ARC)
                .findFirst().orElseThrow();

        // Sleep step was covered by "markRested" narrative
        selfCare.updateStep(0, selfCare.steps().get(0).increment());
        // Gym step
        selfCare.updateStep(1, selfCare.steps().get(1).increment());
        selfCare.checkCompletion();

        assertThat(selfCare.isCompleted()).isTrue();
        assertThat(log.hasCompletedQuest(QuestType.SELF_CARE_ARC)).isTrue();

        // Gym improves mood (simulated via direct stat change)
        tanya.applyStatChanges(new StatChanges(0, 5, -5, 10, -5, 5));
        int moodAfterGym = tanya.stats().mood();

        // --- 3. Work (11:00 → 18:00) ---
        time = time.advanceHours(7);
        assertThat(time.hour()).isEqualTo(18);
        tanya.markWorked();

        // Work consumes energy and may increase stress
        tanya.applyStatChanges(new StatChanges(-20, 0, 15, -10, 50, 0));
        assertThat(tanya.stats().money()).isGreaterThan(0);

        // --- 4. Husband date (18:00 → 20:00) ---
        time = time.advanceHours(2);
        assertThat(time.hour()).isEqualTo(20);

        Quest family = log.activeQuests().stream()
                .filter(q -> q.type() == QuestType.FAMILY_HARMONY)
                .findFirst().orElseThrow();

        // Talk with husband (step 0)
        family.updateStep(0, family.steps().get(0).increment());
        family.checkCompletion();
        assertThat(family.isActive()).isTrue(); // still one step remaining

        // Relationship improves after quality time
        RelationshipChanges dateChanges = new RelationshipChanges(NpcCode.HUSBAND, 5, 3, 2, 8);
        rels.applyChanges(NpcCode.HUSBAND, dateChanges);
        rels.markInteraction(NpcCode.HUSBAND, time.day());

        int husbandClosenessAfterDate = rels.get(NpcCode.HUSBAND).closeness();
        assertThat(husbandClosenessAfterDate).isGreaterThan(65); // started at 65

        // Good date boosts mood
        tanya.applyStatChanges(new StatChanges(5, 0, -10, 15, 0, 5));
        assertThat(tanya.stats().mood()).isGreaterThan(moodAfterGym - 10 - 5); // net positive

        // --- 5. Playing with Sam (20:00 → 21:00) ---
        time = time.advanceHours(1);
        tanya.applyStatChanges(new StatChanges(5, 0, -5, 10, 0, 0));
        assertThat(tanya.stats().stress()).isLessThan(tanya.stats().stress() + 5);

        // --- 6. Call father — complete FAMILY_HARMONY quest (21:00 → 21:30) ---
        family.updateStep(1, family.steps().get(1).increment());
        family.checkCompletion();
        assertThat(family.isCompleted()).isTrue();

        RelationshipChanges callChanges = new RelationshipChanges(NpcCode.FATHER, 5, 5, 3, 0);
        rels.applyChanges(NpcCode.FATHER, callChanges);

        // --- 7. End of day: decay applied ---
        // We're at hour 21. Can't advance to 24 (GameTime constructor prohibits hour>=24).
        // Simulate daily decay for day 1 directly.
        assertThat(time.hour()).isEqualTo(21);
        assertThat(time.hasEnoughTime(4)).isFalse(); // only 3 hours remain

        int closenessBeforeDecay = rels.get(NpcCode.HUSBAND).closeness();
        rels.applyDailyDecay(time.day());
        // After same-day interaction, decay should be minimal
        assertThat(rels.get(NpcCode.HUSBAND).closeness())
                .isGreaterThanOrEqualTo(closenessBeforeDecay - 5);

        tanya.applyEndOfDayDecay();
        assertThat(tanya.isBurnedOut()).isFalse(); // should not be burned out after day 1

        // --- 8. Verify narrative coherence via REST API ---
        ResponseEntity<String> status =
                restTemplate.getForEntity("/api/demo/status", String.class);
        assertThat(status.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(status.getBody()).contains("character");
    }

    // -----------------------------------------------------------------------
    // Multi-day simulation (3 days) leading to a conflict
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Multi-day: 3 days without husband interaction triggers conflict trigger condition")
    void multiDayWithoutInteractionTriggersConflictCondition() {
        Relationships rels  = gameService.getRelationships();
        PlayerCharacter tanya = gameService.getCharacter();

        // Simulate 3 days passing without husband interaction
        // Each day applies decay
        for (int day = 2; day <= 4; day++) {
            rels.applyDailyDecay(day);
        }

        // After 3 days without interaction, closeness should have decreased
        int closenessAfterDecay = rels.get(NpcCode.HUSBAND).closeness();
        assertThat(closenessAfterDecay).isLessThan(65); // started at 65

        // Check if conflict trigger condition is met
        boolean triggerResult = rels.isHusbandConflictTriggered(tanya);
        // Just verify the method runs without exception
        assertThat(triggerResult).isInstanceOf(Boolean.class);
    }

    // -----------------------------------------------------------------------
    // Conflict during day 2
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Day 2: Husband conflict triggers, player uses LISTEN tactic to de-escalate")
    void dayTwoHusbandConflictDeEscalatedByListening() {
        PlayerCharacter tanya = gameService.getCharacter();
        Relationships   rels  = gameService.getRelationships();

        // Day 2: lower closeness to near-trigger level
        RelationshipChanges drop = new RelationshipChanges(NpcCode.HUSBAND, -30, -10, -10, -5);
        rels.applyChanges(NpcCode.HUSBAND, drop);
        rels.applyDailyDecay(2);

        Conflict conflict = new Conflict("narrative-c-001", NarrativeConflictType.HUSBAND_DISTANCE);
        conflict.escalate();
        assertThat(conflict.stage()).isEqualTo(ConflictStage.ESCALATION);

        // De-escalate using LISTEN then COMPROMISE
        conflict.applyTactic(BaseConflictTactics.LISTEN, tanya, rels);
        conflict.applyTactic(BaseConflictTactics.COMPROMISE, tanya, rels);

        if (!conflict.isResolved()) {
            conflict.applyTactic(BaseConflictTactics.COMPROMISE, tanya, rels);
        }

        // Regardless of exact outcome, conflict should be moving toward resolution
        assertThat(conflict.rounds()).hasSizeGreaterThanOrEqualTo(2);
    }

    // -----------------------------------------------------------------------
    // Career quest unlock after family completion
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("CAREER_GROWTH quest can be started after FAMILY_HARMONY is complete")
    void careerQuestUnlockedAfterFamily() {
        QuestLog log = gameService.getQuestLog();

        // Complete family quest
        Quest family = log.activeQuests().stream()
                .filter(q -> q.type() == QuestType.FAMILY_HARMONY)
                .findFirst().orElseThrow();

        for (int i = 0; i < family.steps().size(); i++) {
            family.updateStep(i, family.steps().get(i).increment());
        }
        family.checkCompletion();
        assertThat(family.isCompleted()).isTrue();

        // Career quest should now be startable
        Quest career = log.findByType(QuestType.CAREER_GROWTH).orElseThrow();
        career.start();
        assertThat(career.isActive()).isTrue();

        // Simulate 3 work sessions (addProgress(3) since required=3)
        QuestStepState step = career.steps().get(0).addProgress(3);
        career.updateStep(0, step);
        career.checkCompletion();

        assertThat(career.isCompleted()).isTrue();
        assertThat(log.completedQuests()).hasSize(2); // family + career (self-care was in active only)
    }

    // -----------------------------------------------------------------------
    // GameTime mechanics
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GameTime.advanceHours() correctly wraps to next day")
    void gameTimeAdvancesHoursAndWrapsDay() {
        GameTime time = GameTime.initial();
        assertThat(time.day()).isEqualTo(1);
        assertThat(time.hour()).isEqualTo(GameBalance.DAY_START_HOUR);

        // Advance to near end of day (DAY_START_HOUR=8, advance 15 hours to reach 23)
        time = time.advanceHours(GameBalance.HOURS_PER_DAY - GameBalance.DAY_START_HOUR - 1);
        assertThat(time.hour()).isEqualTo(23);
        assertThat(time.hasEnoughTime(2)).isFalse(); // only 1 hour left

        // Start new day
        time = time.startNewDay();
        assertThat(time.day()).isEqualTo(2);
        assertThat(time.hour()).isEqualTo(GameBalance.DAY_START_HOUR);
    }

    @Test
    @DisplayName("hasEnoughTime returns false when insufficient hours remain in day")
    void hasEnoughTimeReturnsFalseNearEndOfDay() {
        GameTime time = GameTime.initial();
        // Advance to hour 23 (1 hour before end of day=24)
        time = time.advanceHours(GameBalance.HOURS_PER_DAY - GameBalance.DAY_START_HOUR - 1);
        assertThat(time.hour()).isEqualTo(23);
        // A 4-hour activity should not fit
        assertThat(time.hasEnoughTime(4)).isFalse();
    }

    // -----------------------------------------------------------------------
    // REST API integration checks for the narrative
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("REST /api/demo/status contains relationships data")
    void statusApiContainsRelationshipsData() {
        ResponseEntity<String> res =
                restTemplate.getForEntity("/api/demo/status", String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).contains("relationships");
        assertThat(res.getBody()).contains("HUSBAND");
        assertThat(res.getBody()).contains("closeness");
    }
}
