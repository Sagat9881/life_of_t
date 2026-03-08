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

    public void checkTriggers(Map<String, Object> context) {
        for (QuestSpec spec : questSpecs) {
            if (activeQuests.containsKey(spec.id())) continue;
            if (spec.triggerDay() != null) {
                Object day = context.get("currentDay");
                if (day instanceof Number n && n.intValue() >= spec.triggerDay()) {
                    activeQuests.put(spec.id(), new QuestState(spec));
                }
            }
        }
    }

    public List<StepCompletionResult> checkStepCompletion(Map<String, Object> context) {
        List<StepCompletionResult> results = new ArrayList<>();
        for (QuestState state : activeQuests.values()) {
            if (state.completed || state.failed) continue;
            StepSpec step = state.currentStep();
            if (step == null) { state.completed = true; continue; }
            if (isStepComplete(step, context)) {
                state.currentStepIndex++;
                boolean questDone = state.currentStepIndex >= state.spec.steps().size();
                if (questDone) state.completed = true;
                results.add(new StepCompletionResult(state.spec.id(), step.id(),
                        step.dialogue(), questDone ? state.spec.rewards() : List.of()));
            }
        }
        return results;
    }

    public record StepCompletionResult(String questId, String stepId,
                                       List<DialogueEntry> dialogue, List<RewardSpec> rewards) {}

    private boolean isStepComplete(StepSpec step, Map<String, Object> context) {
        if (step.conditions() == null) return false;
        return step.conditions().stream().allMatch(c -> {
            Object val = context.get(c.target());
            if (val == null) return false;
            if (val instanceof Number n) {
                double actual = n.doubleValue();
                double expected = Double.parseDouble(c.value());
                return switch (c.operator()) {
                    case "gte" -> actual >= expected;
                    case "lte" -> actual <= expected;
                    case "eq" -> actual == expected;
                    default -> false;
                };
            }
            return val.toString().equals(c.value());
        });
    }

    public Map<String, QuestState> activeQuests() { return Collections.unmodifiableMap(activeQuests); }
}
