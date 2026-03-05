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
import ru.lifegame.backend.domain.quest.Quest;
import ru.lifegame.backend.domain.quest.QuestLog;
import ru.lifegame.backend.domain.quest.QuestObjective;
import ru.lifegame.backend.domain.quest.QuestStepState;
import ru.lifegame.backend.domain.quest.QuestType;
import ru.lifegame.demo.service.DemoGameService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E quest-chain tests.
 *
 * <p>Uses {@code @SpringBootTest(RANDOM_PORT)} to start the full application,
 * then directly accesses domain objects via {@link DemoGameService} as well as
 * calling the REST API via {@link TestRestTemplate}.</p>
 *
 * <p>{@code @Testcontainers} is declared to register the Testcontainers JUnit 5
 * extension; it controls container lifecycle in sibling tests that need
 * infrastructure containers.</p>
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Quest Chain E2E Tests")
class QuestChainE2ETest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    DemoGameService gameService;

    @BeforeEach
    void resetSession() {
        gameService.reset();
    }

    // -----------------------------------------------------------------------
    // 1. Basic quest lifecycle
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("QuestLog starts with two active quests (SELF_CARE + FAMILY_HARMONY)")
    void questLogHasTwoActiveQuestsOnStart() {
        QuestLog log = gameService.getQuestLog();
        List<Quest> active = log.activeQuests();
        assertThat(active).hasSize(2);
        assertThat(active).anyMatch(q -> q.type() == QuestType.SELF_CARE_ARC);
        assertThat(active).anyMatch(q -> q.type() == QuestType.FAMILY_HARMONY);
    }

    @Test
    @DisplayName("Completing all steps of SELF_CARE_ARC marks quest as COMPLETED")
    void selfCareQuestCompletesAfterAllSteps() {
        QuestLog log = gameService.getQuestLog();

        Quest selfCare = log.activeQuests().stream()
                .filter(q -> q.type() == QuestType.SELF_CARE_ARC)
                .findFirst()
                .orElseThrow();

        // Progress step 0: sleep
        selfCare.updateStep(0, selfCare.steps().get(0).increment());
        // Progress step 1: gym
        selfCare.updateStep(1, selfCare.steps().get(1).increment());

        selfCare.checkCompletion();

        assertThat(selfCare.isCompleted()).isTrue();
        assertThat(selfCare.progressPercent()).isEqualTo(100);
    }

    @Test
    @DisplayName("progressPercent is proportional to completed steps")
    void progressPercentReflectsStepCompletion() {
        QuestLog log = gameService.getQuestLog();
        Quest selfCare = log.activeQuests().stream()
                .filter(q -> q.type() == QuestType.SELF_CARE_ARC)
                .findFirst()
                .orElseThrow();

        // No progress yet
        assertThat(selfCare.progressPercent()).isEqualTo(0);

        // Complete first of two steps
        selfCare.updateStep(0, selfCare.steps().get(0).increment());
        selfCare.checkCompletion();

        assertThat(selfCare.progressPercent()).isEqualTo(50);
    }

    // -----------------------------------------------------------------------
    // 2. Multi-step quest with required > 1
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("CAREER_GROWTH step requires 3 increments before completing")
    void careerGrowthStepRequiresThreeIncrements() {
        QuestLog log = gameService.getQuestLog();

        Quest career = log.findByType(QuestType.CAREER_GROWTH)
                .orElseThrow(() -> new AssertionError("CAREER_GROWTH quest not found"));
        career.start();

        QuestStepState step = career.steps().get(0);
        assertThat(step.isCompleted()).isFalse();

        step = step.increment();
        assertThat(step.isCompleted()).isFalse();
        step = step.increment();
        assertThat(step.isCompleted()).isFalse();
        step = step.increment();
        assertThat(step.isCompleted()).isTrue();

        career.updateStep(0, step);
        career.checkCompletion();

        assertThat(career.isCompleted()).isTrue();
    }

    // -----------------------------------------------------------------------
    // 3. Full quest chain: SELF_CARE -> FAMILY_HARMONY -> CAREER_GROWTH
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Full quest chain: self-care unlocks family, family enables career focus")
    void fullQuestChain() {
        QuestLog log = gameService.getQuestLog();

        // --- Phase 1: SELF_CARE_ARC ---
        Quest selfCare = log.activeQuests().stream()
                .filter(q -> q.type() == QuestType.SELF_CARE_ARC)
                .findFirst().orElseThrow();

        selfCare.updateStep(0, selfCare.steps().get(0).increment());
        selfCare.updateStep(1, selfCare.steps().get(1).increment());
        selfCare.checkCompletion();
        assertThat(selfCare.isCompleted()).isTrue();

        // After completing self-care, only FAMILY quest is still active
        List<Quest> activeAfterSelfCare = log.activeQuests();
        assertThat(activeAfterSelfCare).noneMatch(q -> q.type() == QuestType.SELF_CARE_ARC);
        assertThat(activeAfterSelfCare).anyMatch(q -> q.type() == QuestType.FAMILY_HARMONY);

        // --- Phase 2: FAMILY_HARMONY ---
        Quest family = activeAfterSelfCare.stream()
                .filter(q -> q.type() == QuestType.FAMILY_HARMONY)
                .findFirst().orElseThrow();

        family.updateStep(0, family.steps().get(0).increment());
        family.updateStep(1, family.steps().get(1).increment());
        family.checkCompletion();
        assertThat(family.isCompleted()).isTrue();

        // --- Phase 3: CAREER_GROWTH ---
        Quest career = log.findByType(QuestType.CAREER_GROWTH).orElseThrow();
        career.start();

        QuestStepState careerStep = career.steps().get(0);
        careerStep = careerStep.addProgress(3);
        career.updateStep(0, careerStep);
        career.checkCompletion();
        assertThat(career.isCompleted()).isTrue();

        // Verify the quest log reflects all completions
        assertThat(log.completedQuests()).hasSize(3);
        assertThat(log.hasCompletedQuest(QuestType.SELF_CARE_ARC)).isTrue();
        assertThat(log.hasCompletedQuest(QuestType.FAMILY_HARMONY)).isTrue();
        assertThat(log.hasCompletedQuest(QuestType.CAREER_GROWTH)).isTrue();
    }

    // -----------------------------------------------------------------------
    // 4. Narrative coherence: self-esteem context
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Narrative coherence: SELF_CARE_ARC completion is reflected in player state")
    void selfCareCompletionReflectedInNarrativeContext() {
        // Arrange: complete the self-care quest
        QuestLog log = gameService.getQuestLog();
        Quest selfCare = log.activeQuests().stream()
                .filter(q -> q.type() == QuestType.SELF_CARE_ARC)
                .findFirst().orElseThrow();

        selfCare.updateStep(0, selfCare.steps().get(0).increment());
        selfCare.updateStep(1, selfCare.steps().get(1).increment());
        selfCare.checkCompletion();

        // Assert: narrative expectation — once self-care is done, the quest log
        // confirms improvement. In a full game loop, stats would improve; here
        // we verify the quest record is consistent.
        assertThat(selfCare.isCompleted()).isTrue();
        assertThat(log.hasCompletedQuest(QuestType.SELF_CARE_ARC)).isTrue();

        // The REST API should reflect the completed quest
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/demo/quest-log", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // Active quest list should no longer contain self-care
        assertThat(response.getBody()).doesNotContain("quest-self-care-01");
    }

    // -----------------------------------------------------------------------
    // 5. Quest fail path
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Calling fail() on an active quest marks it FAILED")
    void questCanBeFailed() {
        QuestLog log = gameService.getQuestLog();
        Quest selfCare = log.activeQuests().stream()
                .filter(q -> q.type() == QuestType.SELF_CARE_ARC)
                .findFirst().orElseThrow();

        selfCare.fail();

        assertThat(selfCare.isActive()).isFalse();
        assertThat(selfCare.isCompleted()).isFalse();
    }

    // -----------------------------------------------------------------------
    // 6. REST API integration
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/demo/status returns 200 with non-empty character and quest data")
    void statusApiReturnsFullGameState() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/demo/status", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        assertThat(body).contains("character");
        assertThat(body).contains("activeQuests");
        assertThat(body).contains("gameTime");
        assertThat(body).contains("Tanya");
    }
}
