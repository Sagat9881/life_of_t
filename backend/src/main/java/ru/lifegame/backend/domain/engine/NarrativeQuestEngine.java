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
        this.questSpecs = questSpecs != null ? questSpecs : List.of();
    }

    public record QuestState(QuestSpec spec, int currentStepIndex, boolean completed) {
        public StepSpec currentStep() {
            if (completed || currentStepIndex >= spec.steps().size()) return null;
            return spec.steps().get(currentStepIndex);
        }
    }

    public void activateQuest(String questId) {
        questSpecs.stream()
                .filter(q -> q.id().equals(questId))
                .findFirst()
                .ifPresent(spec -> activeQuests.putIfAbsent(questId, new QuestState(spec, 0, false)));
    }

    public Optional<StepCompletionResult> tryCompleteStep(String questId, String actionId, Map<String, Object> context) {
        QuestState state = activeQuests.get(questId);
        if (state == null || state.completed()) return Optional.empty();

        StepSpec step = state.currentStep();
        if (step == null) return Optional.empty();

        if (!step.triggerAction().equals(actionId)) return Optional.empty();

        if (step.conditions() != null) {
            boolean conditionsMet = step.conditions().stream().allMatch(c -> {
                Object val = context.get(c.target());
                if (val == null) return false;
                if (val instanceof Number num) {
                    double actual = num.doubleValue();
                    double expected = Double.parseDouble(c.value());
                    return switch (c.operator()) {
                        case "gte" -> actual >= expected;
                        case "lte" -> actual <= expected;
                        default -> false;
                    };
                }
                return false;
            });
            if (!conditionsMet) return Optional.empty();
        }

        int nextIndex = state.currentStepIndex() + 1;
        boolean nowComplete = nextIndex >= state.spec().steps().size();
        activeQuests.put(questId, new QuestState(state.spec(), nextIndex, nowComplete));

        return Optional.of(new StepCompletionResult(step.id(), step.dialogue(), step.rewards(), nowComplete));
    }

    public record StepCompletionResult(String stepId, List<DialogueEntry> dialogue, List<RewardSpec> rewards, boolean questCompleted) {}

    public Map<String, QuestState> getActiveQuests() {
        return Collections.unmodifiableMap(activeQuests);
    }
}
