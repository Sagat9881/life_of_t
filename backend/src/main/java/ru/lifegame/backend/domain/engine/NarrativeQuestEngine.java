package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.spec.QuestSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.*;

import java.util.*;

public class NarrativeQuestEngine {

    private final List<QuestSpec> questSpecs;
    private final Map<String, QuestState> activeQuests = new LinkedHashMap<>();

    public NarrativeQuestEngine(List<QuestSpec> questSpecs) {
        this.questSpecs = questSpecs;
    }

    public record QuestState(QuestSpec spec, int currentStepIndex, boolean completed) {
        public StepSpec currentStep() {
            if (currentStepIndex >= spec.steps().size()) return null;
            return spec.steps().get(currentStepIndex);
        }
    }

    public void checkTriggers(int currentDay) {
        for (QuestSpec qs : questSpecs) {
            if (!activeQuests.containsKey(qs.id()) && currentDay >= qs.meta().triggerDay()) {
                activeQuests.put(qs.id(), new QuestState(qs, 0, false));
            }
        }
    }

    public record StepCompletionResult(String questId, String stepId, String dialogue, List<RewardSpec> rewards) {}

    public List<StepCompletionResult> checkProgress(Map<String, Integer> gameState) {
        List<StepCompletionResult> results = new ArrayList<>();
        for (var entry : activeQuests.entrySet()) {
            QuestState state = entry.getValue();
            if (state.completed()) continue;
            StepSpec step = state.currentStep();
            if (step == null) continue;

            boolean allMet = step.objectives().stream().allMatch(obj -> {
                Integer val = gameState.get(obj.target());
                if (val == null) return false;
                int threshold = Integer.parseInt(obj.value());
                return switch (obj.operator()) {
                    case "gte" -> val >= threshold;
                    case "lte" -> val <= threshold;
                    case "eq" -> val == threshold;
                    default -> true;
                };
            });

            if (allMet) {
                results.add(new StepCompletionResult(entry.getKey(), step.stepId(), step.dialogueText(), step.rewards()));
                int nextIndex = state.currentStepIndex() + 1;
                boolean done = nextIndex >= state.spec().steps().size();
                activeQuests.put(entry.getKey(), new QuestState(state.spec(), nextIndex, done));
            }
        }
        return results;
    }

    public Map<String, QuestState> activeQuests() { return Collections.unmodifiableMap(activeQuests); }
}
