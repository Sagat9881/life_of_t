package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.spec.QuestSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.StepSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.DialogueEntry;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.RewardSpec;

import java.util.*;

public class NarrativeQuestEngine {

    private final Map<String, QuestState> activeQuests = new LinkedHashMap<>();
    private final List<QuestSpec> allQuests;

    public NarrativeQuestEngine(List<QuestSpec> allQuests) {
        this.allQuests = allQuests;
    }

    public void activateQuest(String questId) {
        allQuests.stream()
                .filter(q -> q.id().equals(questId))
                .findFirst()
                .ifPresent(q -> activeQuests.put(questId, new QuestState(q, 0)));
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

        public void advance() {
            currentStepIndex++;
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
        state.advance();
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
}
