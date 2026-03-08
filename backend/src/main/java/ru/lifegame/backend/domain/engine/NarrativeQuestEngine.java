package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.spec.QuestSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.StepSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.DialogueEntry;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.RewardSpec;

import java.util.*;

public class NarrativeQuestEngine {

    private final Map<String, QuestState> activeQuests = new LinkedHashMap<>();

    public void registerQuest(QuestSpec spec) {
        activeQuests.put(spec.id(), new QuestState(spec, 0));
    }

    public void registerAll(List<QuestSpec> specs) {
        if (specs != null) specs.forEach(this::registerQuest);
    }

    public static class QuestState {
        private final QuestSpec spec;
        private int currentStepIndex;
        private boolean completed;

        public QuestState(QuestSpec spec, int currentStepIndex) {
            this.spec = spec;
            this.currentStepIndex = currentStepIndex;
            this.completed = false;
        }

        public StepSpec currentStep() {
            if (completed || currentStepIndex >= spec.steps().size()) return null;
            return spec.steps().get(currentStepIndex);
        }

        public boolean isCompleted() { return completed; }
        public QuestSpec spec() { return spec; }
        public int currentStepIndex() { return currentStepIndex; }
    }

    public Optional<StepCompletionResult> tryCompleteStep(String questId, String actionId, Map<String, Object> context) {
        QuestState state = activeQuests.get(questId);
        if (state == null || state.isCompleted()) return Optional.empty();

        StepSpec step = state.currentStep();
        if (step == null) return Optional.empty();
        if (!step.triggerAction().equals(actionId)) return Optional.empty();

        state.currentStepIndex++;
        boolean questDone = state.currentStepIndex >= state.spec.steps().size();
        if (questDone) state.completed = true;

        return Optional.of(new StepCompletionResult(
                questId, step.id(), questDone,
                step.dialogue(), questDone ? state.spec.rewards() : List.of()
        ));
    }

    public record StepCompletionResult(
            String questId, String stepId, boolean questCompleted,
            List<DialogueEntry> dialogue, List<RewardSpec> rewards
    ) {}

    public Map<String, QuestState> getActiveQuests() {
        return Collections.unmodifiableMap(activeQuests);
    }
}
