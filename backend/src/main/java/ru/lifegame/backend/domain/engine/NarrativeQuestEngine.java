package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.spec.QuestSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.StepSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.RewardSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.DialogueEntry;

import java.util.*;

public class NarrativeQuestEngine {

    private final Map<String, QuestState> activeQuests = new LinkedHashMap<>();
    private final Map<String, QuestState> completedQuests = new LinkedHashMap<>();

    public void activateQuest(QuestSpec spec) {
        if (!activeQuests.containsKey(spec.id()) && !completedQuests.containsKey(spec.id())) {
            activeQuests.put(spec.id(), new QuestState(spec, 0));
        }
    }

    public static class QuestState {
        private final QuestSpec spec;
        private int currentStepIndex;

        public QuestState(QuestSpec spec, int currentStepIndex) {
            this.spec = spec;
            this.currentStepIndex = currentStepIndex;
        }

        public StepSpec currentStep() {
            if (currentStepIndex >= spec.steps().size()) return null;
            return spec.steps().get(currentStepIndex);
        }

        public boolean isComplete() {
            return currentStepIndex >= spec.steps().size();
        }

        public QuestSpec spec() { return spec; }
        public int currentStepIndex() { return currentStepIndex; }
    }

    public Optional<StepCompletionResult> tryCompleteStep(String questId, String actionId, Map<String, Object> context) {
        QuestState state = activeQuests.get(questId);
        if (state == null || state.isComplete()) return Optional.empty();

        StepSpec step = state.currentStep();
        if (step == null) return Optional.empty();

        if (!step.requiredAction().equals(actionId)) return Optional.empty();

        state.currentStepIndex++;

        if (state.isComplete()) {
            activeQuests.remove(questId);
            completedQuests.put(questId, state);
        }

        return Optional.of(new StepCompletionResult(
                questId, step.id(), state.isComplete(),
                step.dialogue(), step.rewards()
        ));
    }

    public record StepCompletionResult(
            String questId, String stepId, boolean questCompleted,
            List<DialogueEntry> dialogue, List<RewardSpec> rewards
    ) {}

    public Map<String, QuestState> activeQuests() { return Collections.unmodifiableMap(activeQuests); }
    public Map<String, QuestState> completedQuests() { return Collections.unmodifiableMap(completedQuests); }
}
