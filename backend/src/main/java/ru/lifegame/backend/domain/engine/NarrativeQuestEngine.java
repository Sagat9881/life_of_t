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

    public void checkQuestTriggers(Map<String, Object> context) {
        for (QuestSpec spec : questSpecs) {
            if (activeQuests.containsKey(spec.id())) continue;
            if (spec.triggerDay() != null) {
                Object day = context.get("day");
                if (day instanceof Number d && d.intValue() >= spec.triggerDay()) {
                    activeQuests.put(spec.id(), new QuestState(spec));
                }
            }
        }
    }

    public List<StepCompletionResult> checkStepCompletion(String actionId, Map<String, Object> context) {
        List<StepCompletionResult> results = new ArrayList<>();
        for (QuestState state : activeQuests.values()) {
            if (state.completed) continue;
            StepSpec step = state.currentStep();
            if (step == null) continue;
            if (step.actionId() != null && step.actionId().equals(actionId)) {
                state.currentStepIndex++;
                if (state.currentStepIndex >= state.spec.steps().size()) {
                    state.completed = true;
                }
                results.add(new StepCompletionResult(state.spec.id(), step.id(),
                        step.dialogue(), step.rewards(), state.completed));
            }
        }
        return results;
    }

    public record StepCompletionResult(String questId, String stepId,
                                       List<DialogueEntry> dialogue, List<RewardSpec> rewards,
                                       boolean questCompleted) {}

    public Map<String, QuestState> activeQuests() { return Collections.unmodifiableMap(activeQuests); }
}
