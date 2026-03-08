package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.spec.QuestSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.StepSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.RewardSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.DialogueEntry;

import java.util.*;

public class NarrativeQuestEngine {

    private final List<QuestSpec> allQuests;
    private final Map<String, QuestState> activeQuests = new LinkedHashMap<>();

    public NarrativeQuestEngine(List<QuestSpec> quests) {
        this.allQuests = new ArrayList<>(quests);
    }

    public record QuestState(String questId, QuestSpec spec, int currentStepIndex, boolean completed) {
        public StepSpec currentStep() {
            if (currentStepIndex >= spec.steps().size()) return null;
            return spec.steps().get(currentStepIndex);
        }
    }

    public void activateQuest(String questId) {
        for (QuestSpec q : allQuests) {
            if (q.id().equals(questId) && !activeQuests.containsKey(questId)) {
                activeQuests.put(questId, new QuestState(questId, q, 0, false));
                break;
            }
        }
    }

    public Optional<StepCompletionResult> tryCompleteStep(String questId, String actionId, Map<String, Object> context) {
        QuestState state = activeQuests.get(questId);
        if (state == null || state.completed()) return Optional.empty();
        StepSpec step = state.currentStep();
        if (step == null) return Optional.empty();
        if (!step.requiredAction().equals(actionId)) return Optional.empty();

        int nextIndex = state.currentStepIndex() + 1;
        boolean done = nextIndex >= state.spec().steps().size();
        activeQuests.put(questId, new QuestState(questId, state.spec(), nextIndex, done));

        return Optional.of(new StepCompletionResult(
            questId, step.id(), done,
            step.dialogue(),
            done ? state.spec().completionReward() : null
        ));
    }

    public record StepCompletionResult(
        String questId, String stepId, boolean questCompleted,
        List<DialogueEntry> dialogue,
        RewardSpec reward
    ) {}

    public Map<String, QuestState> getActiveQuests() {
        return Collections.unmodifiableMap(activeQuests);
    }

    public List<QuestSpec> getAllQuests() {
        return Collections.unmodifiableList(allQuests);
    }
}
