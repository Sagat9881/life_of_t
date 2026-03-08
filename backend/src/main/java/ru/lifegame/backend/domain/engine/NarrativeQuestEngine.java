package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.spec.QuestSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.StepSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.RewardSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.DialogueEntry;

import java.util.*;

public class NarrativeQuestEngine {

    private final List<QuestSpec> allQuests;
    private final Map<String, QuestState> activeQuests = new LinkedHashMap<>();

    public NarrativeQuestEngine(List<QuestSpec> allQuests) {
        this.allQuests = allQuests;
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
            if (currentStepIndex >= spec.steps().size()) return null;
            return spec.steps().get(currentStepIndex);
        }

        public boolean isCompleted() { return completed; }
        public QuestSpec spec() { return spec; }
        public int currentStepIndex() { return currentStepIndex; }
    }

    public void activateQuest(String questId) {
        allQuests.stream()
                .filter(q -> q.id().equals(questId))
                .findFirst()
                .ifPresent(q -> activeQuests.put(questId, new QuestState(q)));
    }

    public List<String> checkAutoActivation(Map<String, Object> context, int currentDay) {
        List<String> activated = new ArrayList<>();
        for (QuestSpec q : allQuests) {
            if (!activeQuests.containsKey(q.id()) && q.triggerDay() <= currentDay) {
                activeQuests.put(q.id(), new QuestState(q));
                activated.add(q.id());
            }
        }
        return activated;
    }

    public record StepCompletionResult(
            String questId,
            String stepId,
            boolean questCompleted,
            List<DialogueEntry> dialogue,
            List<RewardSpec> rewards
    ) {}

    public Optional<StepCompletionResult> tryCompleteStep(String questId, String actionId, Map<String, Object> context) {
        QuestState state = activeQuests.get(questId);
        if (state == null || state.isCompleted()) return Optional.empty();

        StepSpec step = state.currentStep();
        if (step == null) return Optional.empty();
        if (!step.requiredAction().equals(actionId)) return Optional.empty();

        state.currentStepIndex++;
        boolean questDone = state.currentStepIndex >= state.spec.steps().size();
        if (questDone) state.completed = true;

        return Optional.of(new StepCompletionResult(
                questId, step.id(), questDone, step.dialogue(), questDone ? state.spec.rewards() : List.of()
        ));
    }

    public Map<String, QuestState> activeQuests() { return Collections.unmodifiableMap(activeQuests); }
}
