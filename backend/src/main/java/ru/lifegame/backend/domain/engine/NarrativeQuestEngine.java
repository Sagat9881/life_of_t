package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.spec.QuestSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.StepSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.DialogueEntry;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.RewardSpec;

import java.util.*;

public class NarrativeQuestEngine {

    private final Map<String, QuestState> activeQuests = new LinkedHashMap<>();
    private final Map<String, QuestState> completedQuests = new LinkedHashMap<>();

    public void activateQuest(QuestSpec spec) {
        if (!activeQuests.containsKey(spec.id()) && !completedQuests.containsKey(spec.id())) {
            activeQuests.put(spec.id(), new QuestState(spec, 0));
        }
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

        public QuestSpec spec() { return spec; }
        public int currentStepIndex() { return currentStepIndex; }
    }

    public List<StepCompletionResult> checkProgress(Map<String, Object> context) {
        List<StepCompletionResult> results = new ArrayList<>();
        Iterator<Map.Entry<String, QuestState>> it = activeQuests.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, QuestState> entry = it.next();
            QuestState state = entry.getValue();
            StepSpec step = state.currentStep();
            if (step != null && isStepComplete(step, context)) {
                state.currentStepIndex++;
                results.add(new StepCompletionResult(
                        state.spec.id(), step.id(),
                        step.dialogue(), step.rewards(),
                        state.isComplete()));
                if (state.isComplete()) {
                    it.remove();
                    completedQuests.put(entry.getKey(), state);
                }
            }
        }
        return results;
    }

    public record StepCompletionResult(
            String questId, String stepId,
            List<DialogueEntry> dialogue,
            List<RewardSpec> rewards,
            boolean questComplete) {}

    private boolean isStepComplete(StepSpec step, Map<String, Object> context) {
        if (step.conditions() == null) return false;
        for (var c : step.conditions()) {
            Object val = context.get(c.target());
            if (val == null) return false;
            if (val instanceof Number num) {
                double v = num.doubleValue();
                double threshold = Double.parseDouble(c.value());
                boolean met = switch (c.operator()) {
                    case "gte" -> v >= threshold;
                    case "lte" -> v <= threshold;
                    case "eq" -> v == threshold;
                    default -> false;
                };
                if (!met) return false;
            }
        }
        return true;
    }

    public Map<String, QuestState> activeQuests() { return Collections.unmodifiableMap(activeQuests); }
    public Map<String, QuestState> completedQuests() { return Collections.unmodifiableMap(completedQuests); }
}
