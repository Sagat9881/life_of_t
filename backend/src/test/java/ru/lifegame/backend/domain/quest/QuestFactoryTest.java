package ru.lifegame.backend.domain.quest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuestFactoryTest {

    @Test
    void createsThreeQuests() {
        QuestLog log = QuestFactory.createInitialQuestLog();
        assertEquals(3, log.activeQuests().size());
    }

    @Test
    void careerQuestHasThreeSteps() {
        QuestLog log = QuestFactory.createInitialQuestLog();
        Quest career = log.all().get("quest_career_growth");
        assertNotNull(career);
        assertEquals(3, career.steps().size());
        assertTrue(career.isActive());
    }

    @Test
    void questCompletionWorks() {
        QuestLog log = QuestFactory.createInitialQuestLog();
        Quest career = log.all().get("quest_career_growth");
        for (int i = 0; i < career.steps().size(); i++) {
            QuestStepState step = career.steps().get(i);
            career.updateStep(i, step.addProgress(step.objective().required()));
        }
        career.checkCompletion();
        assertTrue(career.isCompleted());
    }
}
