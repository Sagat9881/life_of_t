package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.spec.QuestSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.*;
import ru.lifegame.backend.domain.engine.spec.ConditionSpec;

import java.util.*;

public class NarrativeQuestEngine {

    private final List<QuestSpec> questSpecs;
    private final Map<String, QuestState> activeQuests = new LinkedHashMap<>();
    private final Set<String> completedQuests = new HashSet<>();

    public NarrativeQuestEngine(List<QuestSpec> questSpecs) {
        this.questSpecs = questSpecs;
    }

    public record QuestState(QuestSpec spec, int currentStepIndex) {
        public StepSpec currentStep() {
            if (currentStepIndex >= spec.steps().size()) return null;
            return spec.steps().get(currentStepIndex);
        }
        public QuestState advance() { return new QuestState(spec, currentStepIndex + 1); }
        public boolean isComplete() { return currentStepIndex >= spec.steps().size(); }
    }

    public void checkAndActivateQuests(int currentDay, Object gameContext) {
        for (QuestSpec spec : questSpecs) {
            if (activeQuests.containsKey(spec.id()) || completedQuests.contains(spec.id())) continue;
            if (currentDay >= spec.meta().triggerDay()) {
                activeQuests.put(spec.id(), new QuestState(spec, 0));
            }
        }
    }

    public record StepCompletionResult(String questId, DialogueEntry dialogue, RewardSpec reward) {}

    public Optional<StepCompletionResult> tryCompleteStep(String questId, Object gameContext) {
        QuestState state = activeQuests.get(questId);
        if (state == null || state.isComplete()) return Optional.empty();

        StepSpec step = state.currentStep();
        QuestState advanced = state.advance();
        if (advanced.isComplete()) {
            activeQuests.remove(questId);
            completedQuests.add(questId);
        } else {
            activeQuests.put(questId, advanced);
        }
        return Optional.of(new StepCompletionResult(questId, step.dialogue(), step.reward()));
    }

    public Map<String, QuestState> activeQuests() { return Collections.unmodifiableMap(activeQuests); }
    public Set<String> completedQuests() { return Collections.unmodifiableSet(completedQuests); }
}
