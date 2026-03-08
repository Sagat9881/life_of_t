package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.spec.QuestSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.StepSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.DialogueEntry;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.RewardSpec;

import java.util.*;

public class NarrativeQuestEngine {

    private final List<QuestSpec> questSpecs;
    private final Map<String, QuestState> activeQuests = new LinkedHashMap<>();

    public NarrativeQuestEngine(List<QuestSpec> questSpecs) {
        this.questSpecs = questSpecs;
    }

    public record QuestState(
        String questId,
        String title,
        int currentStepIndex,
        QuestSpec spec,
        boolean completed
    ) {
        public StepSpec currentStep() {
            if (currentStepIndex >= spec.steps().size()) return null;
            return spec.steps().get(currentStepIndex);
        }
    }

    public void tryActivateQuests(Map<String, Object> context, int currentDay) {
        for (QuestSpec spec : questSpecs) {
            if (activeQuests.containsKey(spec.id())) continue;
            if (spec.triggerDay() > 0 && currentDay >= spec.triggerDay()) {
                activeQuests.put(spec.id(), new QuestState(spec.id(), spec.title(), 0, spec, false));
            }
        }
    }

    public List<QuestState> getActiveQuests() {
        return activeQuests.values().stream().filter(q -> !q.completed()).toList();
    }

    public List<QuestState> getCompletedQuests() {
        return activeQuests.values().stream().filter(QuestState::completed).toList();
    }

    public Optional<StepCompletionResult> tryCompleteStep(String questId, String actionId, Map<String, Object> context) {
        QuestState state = activeQuests.get(questId);
        if (state == null || state.completed()) return Optional.empty();
        StepSpec step = state.currentStep();
        if (step == null) return Optional.empty();
        if (!step.actionId().equals(actionId)) return Optional.empty();

        int nextIndex = state.currentStepIndex() + 1;
        boolean isComplete = nextIndex >= state.spec().steps().size();
        activeQuests.put(questId, new QuestState(
            questId, state.title(), nextIndex, state.spec(), isComplete
        ));

        return Optional.of(new StepCompletionResult(
            questId, step.id(), isComplete,
            step.dialogue(), step.reward()
        ));
    }

    public record StepCompletionResult(
        String questId,
        String stepId,
        boolean questCompleted,
        DialogueEntry dialogue,
        RewardSpec reward
    ) {}
}
