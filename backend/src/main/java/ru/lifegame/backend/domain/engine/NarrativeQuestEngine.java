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
        private boolean failed;

        public QuestState(QuestSpec spec) {
            this.spec = spec;
            this.currentStepIndex = 0;
        }

        public StepSpec currentStep() {
            if (currentStepIndex >= spec.steps().size()) return null;
            return spec.steps().get(currentStepIndex);
        }

        public QuestSpec spec() { return spec; }
        public int currentStepIndex() { return currentStepIndex; }
        public boolean isCompleted() { return completed; }
        public boolean isFailed() { return failed; }
    }

    public void activateQuest(String questId) {
        questSpecs.stream()
                .filter(q -> q.id().equals(questId))
                .findFirst()
                .ifPresent(q -> activeQuests.put(questId, new QuestState(q)));
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

    public record StepCompletionResult(
            String questId,
            String stepId,
            boolean questCompleted,
            List<DialogueEntry> dialogue,
            List<RewardSpec> rewards
    ) {}

    public Optional<StepCompletionResult> tryCompleteStep(String questId, String actionId, Map<String, Object> context) {
        QuestState state = activeQuests.get(questId);
        if (state == null || state.completed || state.failed) return Optional.empty();

        StepSpec step = state.currentStep();
        if (step == null) return Optional.empty();
        if (!step.requiredAction().equals(actionId)) return Optional.empty();

        state.currentStepIndex++;
        boolean questDone = state.currentStepIndex >= state.spec.steps().size();
        if (questDone) state.completed = true;

        return Optional.of(new StepCompletionResult(
                questId, step.id(), questDone,
                step.dialogue() != null ? step.dialogue() : List.of(),
                questDone && state.spec.rewards() != null ? state.spec.rewards() : List.of()
        ));
    }

    public Map<String, QuestState> activeQuests() { return Collections.unmodifiableMap(activeQuests); }
}
