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

    public record QuestState(String questId, QuestSpec spec, int currentStepIndex, boolean completed) {
        public StepSpec currentStep() {
            if (currentStepIndex >= spec.steps().size()) return null;
            return spec.steps().get(currentStepIndex);
        }
    }

    public void activateQuest(String questId) {
        questSpecs.stream()
                .filter(q -> q.id().equals(questId))
                .findFirst()
                .ifPresent(spec -> activeQuests.put(questId, new QuestState(questId, spec, 0, false)));
    }

    public Optional<StepCompletionResult> tryCompleteStep(String questId, String actionId, Map<String, Object> context) {
        QuestState state = activeQuests.get(questId);
        if (state == null || state.completed()) return Optional.empty();
        StepSpec step = state.currentStep();
        if (step == null) return Optional.empty();
        if (!step.triggerAction().equals(actionId)) return Optional.empty();

        int nextIndex = state.currentStepIndex() + 1;
        boolean done = nextIndex >= state.spec().steps().size();
        activeQuests.put(questId, new QuestState(questId, state.spec(), nextIndex, done));

        return Optional.of(new StepCompletionResult(
                questId, step.id(), done,
                step.dialogue() != null ? step.dialogue() : List.of(),
                done ? state.spec().rewards() : List.of()
        ));
    }

    public List<QuestState> activeQuests() {
        return new ArrayList<>(activeQuests.values());
    }

    public record StepCompletionResult(
            String questId,
            String stepId,
            boolean questCompleted,
            List<DialogueEntry> dialogue,
            List<RewardSpec> rewards
    ) {}
}
