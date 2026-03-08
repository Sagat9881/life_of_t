package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.spec.QuestSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.StepSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.DialogueEntry;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.RewardSpec;

import java.util.*;
import java.util.stream.Collectors;

public class NarrativeQuestEngine {

    private final List<QuestSpec> questSpecs;
    private final Map<String, QuestState> activeQuests = new LinkedHashMap<>();

    public NarrativeQuestEngine(List<QuestSpec> questSpecs) {
        this.questSpecs = questSpecs;
    }

    public static class QuestState {
        private final QuestSpec spec;
        private int currentStepIndex;
        private boolean completed;

        public QuestState(QuestSpec spec) {
            this.spec = spec;
            this.currentStepIndex = 0;
            this.completed = false;
        }

        public StepSpec currentStep() {
            if (completed || currentStepIndex >= spec.steps().size()) return null;
            return spec.steps().get(currentStepIndex);
        }

        public QuestSpec spec() { return spec; }
        public int currentStepIndex() { return currentStepIndex; }
        public boolean isCompleted() { return completed; }
    }

    public void activateQuest(String questId) {
        questSpecs.stream()
                .filter(q -> q.id().equals(questId))
                .findFirst()
                .ifPresent(spec -> activeQuests.put(questId, new QuestState(spec)));
    }

    public List<String> checkAutoActivation(Map<String, Object> context, int currentDay) {
        List<String> activated = new ArrayList<>();
        for (QuestSpec spec : questSpecs) {
            if (activeQuests.containsKey(spec.id())) continue;
            if (spec.triggerDay() > 0 && currentDay >= spec.triggerDay()) {
                activeQuests.put(spec.id(), new QuestState(spec));
                activated.add(spec.id());
            }
        }
        return activated;
    }

    public Optional<StepCompletionResult> tryCompleteStep(String questId, String actionId, Map<String, Object> context) {
        QuestState state = activeQuests.get(questId);
        if (state == null || state.isCompleted()) return Optional.empty();
        StepSpec step = state.currentStep();
        if (step == null || !step.actionId().equals(actionId)) return Optional.empty();
        state.currentStepIndex++;
        if (state.currentStepIndex >= state.spec.steps().size()) {
            state.completed = true;
        }
        return Optional.of(new StepCompletionResult(questId, step.id(), step.dialogue(), step.rewards(), state.completed));
    }

    public record StepCompletionResult(
            String questId, String stepId,
            List<DialogueEntry> dialogue,
            List<RewardSpec> rewards,
            boolean questCompleted
    ) {}

    public Map<String, QuestState> activeQuests() { return Collections.unmodifiableMap(activeQuests); }
}
