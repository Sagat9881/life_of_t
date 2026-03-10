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
 *   1. NarrativeBootstrap.onApplicationReady() calls reloadSpecs() with all parsed QuestSpecs.
 *   2. EndDayService checks triggerDay on each endDay and calls activateQuest() for new quests.
 *   3. On every player action, ExecutePlayerActionService calls tryCompleteStep() for each
 *      active quest to advance it when all objectives are met.
 *
 * Thread safety: not thread-safe by design (one engine per Spring context,
 * sessions are single-threaded per request).
 */
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

    /** Returns all loaded quest specs (used by EndDayService for triggerDay checks). */
    public List<QuestSpec> getQuestSpecs() {
        return Collections.unmodifiableList(questSpecs);
    }

    // ── quest state ──────────────────────────────────────────────────────────

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

    /**
     * Activate a quest by id. No-op if the quest is already active or id is unknown.
     * Called by EndDayService when triggerDay <= currentDay.
     */
    public void activateQuest(String questId) {
        questSpecs.stream()
                .filter(q -> q.id().equals(questId))
                .findFirst()
                .ifPresent(q -> activeQuests.put(questId, new QuestState(q, 0, false)));
    }

    // ── step completion ──────────────────────────────────────────────────────

    /**
     * Try to complete the current step of a quest.
     * Returns a result if the step's objectives are all met; empty otherwise.
     * Advances the step index (or marks the quest complete) on success.
     */
    public Optional<StepCompletionResult> tryCompleteStep(String questId, Map<String, Object> context) {
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
