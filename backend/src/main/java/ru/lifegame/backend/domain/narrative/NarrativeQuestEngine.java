package ru.lifegame.backend.domain.narrative;

import ru.lifegame.backend.domain.narrative.spec.QuestSpec;
import ru.lifegame.backend.domain.narrative.spec.QuestSpec.StepSpec;
import ru.lifegame.backend.domain.narrative.spec.QuestSpec.RewardSpec;
import ru.lifegame.backend.domain.narrative.spec.QuestSpec.DialogueEntry;
import ru.lifegame.backend.domain.narrative.spec.QuestSpec.ObjectiveSpec;

import java.util.*;

public class NarrativeQuestEngine {

    private List<QuestSpec> questSpecs;
    private final Map<String, QuestState> activeQuests = new LinkedHashMap<>();

    public NarrativeQuestEngine(List<QuestSpec> questSpecs) {
        this.questSpecs = questSpecs != null ? new ArrayList<>(questSpecs) : new ArrayList<>();
    }

    /**
     * Replaces the current quest spec list with a new set.
     * Called by NarrativeBootstrap after XML loading completes.
     */
    public void reloadSpecs(List<QuestSpec> newSpecs) {
        this.questSpecs = newSpecs != null ? new ArrayList<>(newSpecs) : new ArrayList<>();
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
                .ifPresent(q -> activeQuests.put(questId, new QuestState(q, 0, false)));
    }

    public Optional<StepCompletionResult> tryCompleteStep(String questId, Map<String, Object> context) {
        QuestState state = activeQuests.get(questId);
        if (state == null || state.completed()) return Optional.empty();
        StepSpec step = state.currentStep();
        if (step == null) return Optional.empty();

        boolean met = step.objectives() == null || step.objectives().stream()
                .allMatch(c -> checkObjective(c, context));
        if (!met) return Optional.empty();

        int nextIndex = state.currentStepIndex() + 1;
        boolean done = nextIndex >= state.spec().steps().size();
        activeQuests.put(questId, new QuestState(state.spec(), nextIndex, done));

        return Optional.of(new StepCompletionResult(
                questId, step.stepId(), done,
                List.of(),
                done ? List.of() : List.of()
        ));
    }

    public record StepCompletionResult(
            String questId,
            String stepId,
            boolean questCompleted,
            List<DialogueEntry> dialogue,
            List<RewardSpec> rewards
    ) {}

    public Map<String, QuestState> getActiveQuests() {
        return Collections.unmodifiableMap(activeQuests);
    }

    private boolean checkObjective(ObjectiveSpec c, Map<String, Object> context) {
        Object val = context.get(c.target());
        if (val == null) return false;
        if (val instanceof Number num) {
            double actual = num.doubleValue();
            double expected = Double.parseDouble(c.value());
            return switch (c.operator()) {
                case "gte" -> actual >= expected;
                case "lte" -> actual <= expected;
                case "eq"  -> actual == expected;
                default    -> false;
            };
        }
        return val.toString().equals(c.value());
    }
}
