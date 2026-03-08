package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.spec.QuestSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.StepSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.RewardSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.DialogueEntry;
import ru.lifegame.backend.domain.engine.spec.ConditionSpec;

import java.util.*;

public class NarrativeQuestEngine {

    private final List<QuestSpec> questSpecs;
    private final Map<String, QuestState> activeQuests = new LinkedHashMap<>();

    public NarrativeQuestEngine(List<QuestSpec> questSpecs) {
        this.questSpecs = questSpecs;
    }

    public record QuestState(
            QuestSpec spec,
            int currentStepIndex,
            boolean completed
    ) {
        public StepSpec currentStep() {
            if (currentStepIndex >= spec.steps().size()) return null;
            return spec.steps().get(currentStepIndex);
        }
    }

    public void activateQuest(String questId) {
        questSpecs.stream()
                .filter(q -> q.id().equals(questId))
                .findFirst()
                .ifPresent(spec -> activeQuests.put(questId, new QuestState(spec, 0, false)));
    }

    public List<QuestState> getActiveQuests() {
        return List.copyOf(activeQuests.values());
    }

    public Optional<StepCompletionResult> tryCompleteStep(String questId, Map<String, Object> context) {
        QuestState state = activeQuests.get(questId);
        if (state == null || state.completed()) return Optional.empty();

        StepSpec step = state.currentStep();
        if (step == null) return Optional.empty();

        boolean conditionsMet = step.conditions().stream()
                .allMatch(c -> evaluateCondition(c, context));

        if (!conditionsMet) return Optional.empty();

        int nextIndex = state.currentStepIndex() + 1;
        boolean isComplete = nextIndex >= state.spec().steps().size();
        activeQuests.put(questId, new QuestState(state.spec(), nextIndex, isComplete));

        return Optional.of(new StepCompletionResult(
                questId, step.id(), isComplete,
                step.dialogue(), step.reward()
        ));
    }

    public record StepCompletionResult(
            String questId,
            String stepId,
            boolean questCompleted,
            DialogueEntry dialogue,
            RewardSpec reward
    ) {}

    private boolean evaluateCondition(ConditionSpec c, Map<String, Object> context) {
        Object val = context.get(c.target());
        if (val == null) return false;
        if (val instanceof Number num) {
            double v = num.doubleValue();
            double threshold = Double.parseDouble(c.value());
            return switch (c.operator()) {
                case "gte" -> v >= threshold;
                case "lte" -> v <= threshold;
                case "gt" -> v > threshold;
                case "lt" -> v < threshold;
                case "eq" -> v == threshold;
                default -> false;
            };
        }
        return String.valueOf(val).equals(c.value());
    }
}
