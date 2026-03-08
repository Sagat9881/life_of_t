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

    public void activateQuest(String questId) {
        questSpecs.stream()
            .filter(q -> q.id().equals(questId))
            .findFirst()
            .ifPresent(spec -> activeQuests.putIfAbsent(questId, new QuestState(spec, 0)));
    }

    public record QuestState(QuestSpec spec, int currentStepIndex) {
        public boolean isComplete() {
            return currentStepIndex >= spec.steps().size();
        }
        public StepSpec currentStep() {
            if (isComplete()) return null;
            return spec.steps().get(currentStepIndex);
        }
    }

    public Optional<StepCompletionResult> tryCompleteStep(String questId, Map<String, Object> context) {
        QuestState state = activeQuests.get(questId);
        if (state == null || state.isComplete()) return Optional.empty();

        StepSpec step = state.currentStep();
        if (step == null) return Optional.empty();

        boolean conditionsMet = step.conditions() == null || step.conditions().isEmpty() ||
            step.conditions().stream().allMatch(c -> {
                Object val = context.get(c.target());
                if (val == null) return false;
                if (val instanceof Number num) {
                    double v = num.doubleValue();
                    double t = Double.parseDouble(c.value());
                    return switch (c.operator()) {
                        case "gte" -> v >= t;
                        case "lte" -> v <= t;
                        case "eq" -> v == t;
                        default -> false;
                    };
                }
                return val.toString().equals(c.value());
            });

        if (!conditionsMet) return Optional.empty();

        activeQuests.put(questId, new QuestState(state.spec(), state.currentStepIndex() + 1));
        return Optional.of(new StepCompletionResult(
            questId, step.id(),
            step.dialogue() != null ? step.dialogue() : List.of(),
            step.rewards() != null ? step.rewards() : List.of()
        ));
    }

    public record StepCompletionResult(
        String questId, String stepId,
        List<DialogueEntry> dialogue,
        List<RewardSpec> rewards
    ) {}

    public Map<String, QuestState> getActiveQuests() { return Collections.unmodifiableMap(activeQuests); }
}
