package ru.lifegame.backend.domain.quest;

import ru.lifegame.backend.domain.model.session.GameSessionContext;

import java.util.List;

public class QuestProgressUpdater {

    public void onActionExecuted(String actionCode, GameSessionContext context) {
        QuestLog log = context.questLog();
        switch (actionCode) {
            case "GO_TO_WORK" -> incrementStep(log, "quest_career_growth", 0);
            case "SELF_CARE" -> incrementStep(log, "quest_self_care", 0);
            case "DATE_WITH_HUSBAND" -> incrementStep(log, "quest_family_harmony", 0);
            case "VISIT_FATHER" -> incrementStep(log, "quest_family_harmony", 1);
            default -> { }
        }
    }

    public void onDayEnd(GameSessionContext context) {
        QuestLog log = context.questLog();
        int satisfaction = context.player().job().satisfaction();
        int selfEsteem = context.player().stats().selfEsteem();
        int mood = context.player().stats().mood();

        checkThreshold(log, "quest_career_growth", 1, satisfaction, 70);
        checkThreshold(log, "quest_career_growth", 2, satisfaction, 80);
        checkThreshold(log, "quest_self_care", 1, selfEsteem, 75);
        if (mood >= 80 && selfEsteem >= 80) {
            forceComplete(log, "quest_self_care", 2);
        }

        var husband = context.relationships().get("HUSBAND");
        if (husband != null && husband.closeness() >= 80 && husband.romance() >= 70) {
            forceComplete(log, "quest_family_harmony", 2);
        }

        log.all().values().forEach(Quest::checkCompletion);
    }

    private void incrementStep(QuestLog log, String questId, int stepIndex) {
        Quest quest = log.all().get(questId);
        if (quest != null && quest.isActive()) {
            List<QuestStepState> steps = quest.steps();
            if (stepIndex < steps.size() && !steps.get(stepIndex).isCompleted()) {
                quest.updateStep(stepIndex, steps.get(stepIndex).increment());
            }
        }
    }

    private void checkThreshold(QuestLog log, String questId, int stepIndex, int value, int threshold) {
        if (value >= threshold) forceComplete(log, questId, stepIndex);
    }

    private void forceComplete(QuestLog log, String questId, int stepIndex) {
        Quest quest = log.all().get(questId);
        if (quest != null && quest.isActive()) {
            List<QuestStepState> steps = quest.steps();
            if (stepIndex < steps.size() && !steps.get(stepIndex).isCompleted()) {
                QuestStepState step = steps.get(stepIndex);
                int remaining = step.objective().required() - step.currentCount();
                if (remaining > 0) quest.updateStep(stepIndex, step.addProgress(remaining));
            }
        }
    }
}
