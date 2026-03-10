package ru.lifegame.backend.domain.narrative;

import ru.lifegame.backend.domain.narrative.spec.QuestSpec;
import ru.lifegame.backend.domain.narrative.spec.QuestSpec.StepSpec;
import ru.lifegame.backend.domain.narrative.spec.QuestSpec.RewardSpec;
import ru.lifegame.backend.domain.narrative.spec.QuestSpec.DialogueEntry;
import ru.lifegame.backend.domain.narrative.spec.QuestSpec.ObjectiveSpec;

import java.util.*;

/**
 * Runtime engine for data-driven quests loaded from narrative/quests/*.xml.
 *
 * Lifecycle:
 *   1. NarrativeBootstrap.onApplicationReady() → reloadSpecs()
 *   2. EndDayService checks triggerDay and calls activateQuest()
 *   3. ExecutePlayerActionService calls tryCompleteStep() on every action
 */
public class NarrativeQuestEngine {

    private List<QuestSpec> questSpecs;
    private final Map<String, QuestState> activeQuests = new LinkedHashMap<>();

    public NarrativeQuestEngine(List<QuestSpec> questSpecs) {
        this.questSpecs = questSpecs != null ? new ArrayList<>(questSpecs) : new ArrayList<>();
    }

    public void reloadSpecs(List<QuestSpec> newSpecs) {
        this.questSpecs = newSpecs != null ? new ArrayList<>(newSpecs) : new ArrayList<>();
    }

    public List<QuestSpec> getQuestSpecs() {
        return Collections.unmodifiableList(questSpecs);
    }

    // ── quest state ─────────────────────────────────────────────────────────

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

    // ── step completion ──────────────────────────────────────────────────────

    /**
     * Try to complete the current step of an active quest.
     * All objectives must be met (AND logic).
     * On success: advances step index (or marks quest complete) and
     * returns a result carrying the step’s rewards for the caller to apply.
     */
    public Optional<StepCompletionResult> tryCompleteStep(String questId,
                                                          Map<String, Object> context) {
        QuestState state = activeQuests.get(questId);
        if (state == null || state.completed()) return Optional.empty();
        StepSpec step = state.currentStep();
        if (step == null) return Optional.empty();

        boolean met = step.objectives() == null || step.objectives().stream()
                .allMatch(c -> checkObjective(c, context));
        if (!met) return Optional.empty();

        int     nextIndex = state.currentStepIndex() + 1;
        boolean done      = nextIndex >= state.spec().steps().size();
        activeQuests.put(questId, new QuestState(state.spec(), nextIndex, done));

        // Pass real rewards from the XML spec to the caller
        List<RewardSpec> rewards = step.rewards() != null ? step.rewards() : List.of();

        return Optional.of(new StepCompletionResult(
                questId,
                step.stepId(),
                done,
                List.of(),   // dialogue — not shown in current flow
                rewards
        ));
    }

    public record StepCompletionResult(
            String questId,
            String stepId,
            boolean questCompleted,
            List<DialogueEntry> dialogue,
            List<RewardSpec> rewards
    ) {}

    // ── accessors ─────────────────────────────────────────────────────────────

    public Map<String, QuestState> getActiveQuests() {
        return Collections.unmodifiableMap(activeQuests);
    }

    // ── objective evaluation ────────────────────────────────────────────────────

    private boolean checkObjective(ObjectiveSpec c, Map<String, Object> context) {
        Object val = context.get(c.target());
        if (val == null) return false;
        if (val instanceof Number num) {
            double actual   = num.doubleValue();
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
