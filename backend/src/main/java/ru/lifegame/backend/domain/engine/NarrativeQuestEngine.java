package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.spec.QuestSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.StepSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.RewardSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.DialogueEntry;

import java.util.*;

public class NarrativeQuestEngine {

    private final Map<String, QuestState> activeQuests = new LinkedHashMap<>();
    private final Map<String, QuestState> completedQuests = new LinkedHashMap<>();

    public void activateQuest(QuestSpec spec) {
        activeQuests.put(spec.id(), new QuestState(spec, 0));
    }

    public record QuestState(QuestSpec spec, int currentStepIndex) {
        public boolean isComplete() {
            return currentStepIndex >= spec.steps().size();
        }
        public StepSpec currentStep() {
            if (isComplete()) return null;
            return spec.steps().get(currentStepIndex);
        }
        public QuestState advance() {
            return new QuestState(spec, currentStepIndex + 1);
        }
    }

    public Optional<StepCompletionResult> tryCompleteStep(String questId, Map<String, Object> context) {
        QuestState state = activeQuests.get(questId);
        if (state == null || state.isComplete()) return Optional.empty();

        StepSpec step = state.currentStep();
        if (step == null) return Optional.empty();

        boolean conditionsMet = step.conditions() == null || step.conditions().stream()
                .allMatch(c -> {
                    Object val = context.get(c.target());
                    if (val == null) return false;
                    if (val instanceof Number num) {
                        double v = num.doubleValue();
                        double threshold = Double.parseDouble(c.value());
                        return switch (c.operator()) {
                            case "gte" -> v >= threshold;
                            case "lte" -> v <= threshold;
                            default -> false;
                        };
                    }
                    return val.toString().equals(c.value());
                });

        if (!conditionsMet) return Optional.empty();

        QuestState advanced = state.advance();
        if (advanced.isComplete()) {
            activeQuests.remove(questId);
            completedQuests.put(questId, advanced);
        } else {
            activeQuests.put(questId, advanced);
        }

        return Optional.of(new StepCompletionResult(
                questId, step.id(), step.dialogue(), step.rewards()
        ));
    }

    public record StepCompletionResult(
            String questId, String stepId,
            List<DialogueEntry> dialogue,
            List<RewardSpec> rewards
    ) {}

    public Map<String, QuestState> getActiveQuests() { return Collections.unmodifiableMap(activeQuests); }
    public Map<String, QuestState> getCompletedQuests() { return Collections.unmodifiableMap(completedQuests); }
}
