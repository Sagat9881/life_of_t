package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.spec.QuestSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.StepSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.RewardSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.DialogueEntry;

import java.util.*;

public class NarrativeQuestEngine {

    private final List<QuestSpec> questSpecs;
    private final Map<String, QuestState> activeQuests = new LinkedHashMap<>();

    public NarrativeQuestEngine(List<QuestSpec> questSpecs) {
        this.questSpecs = questSpecs;
    }

    public void activateQuest(String questId) {
        questSpecs.stream()
            .filter(q -> q.id().equals(questId))
            .findFirst()
            .ifPresent(spec -> activeQuests.put(questId, new QuestState(spec, 0)));
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

    public Optional<StepCompletionResult> tryCompleteStep(String questId, String actionId, Map<String, Object> gameState) {
        QuestState state = activeQuests.get(questId);
        if (state == null || state.isComplete()) return Optional.empty();

        StepSpec step = state.currentStep();
        if (step == null) return Optional.empty();

        if (step.requiredAction().equals(actionId)) {
            state.advance();
            return Optional.of(new StepCompletionResult(
                questId, step.id(), state.isComplete(),
                step.dialogueOnComplete(), step.reward()
            ));
        }
        return Optional.empty();
    }

    public record StepCompletionResult(
        String questId, String stepId, boolean questComplete,
        List<DialogueEntry> dialogue, RewardSpec reward
    ) {}

    public Map<String, QuestState> activeQuests() { return activeQuests; }
}
