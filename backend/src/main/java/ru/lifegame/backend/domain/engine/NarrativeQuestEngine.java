package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.spec.QuestSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.StepSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.RewardSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.DialogueEntry;

import java.util.*;

public class NarrativeQuestEngine {

    private final List<QuestSpec> questSpecs;
    private final Map<String, QuestState> activeQuests = new LinkedHashMap<>();

    public NarrativeQuestEngine(List<QuestSpec> questSpecs) {
        this.questSpecs = questSpecs != null ? questSpecs : List.of();
    }

    public record QuestState(String questId, QuestSpec spec, int currentStepIndex, String status) {
        public StepSpec currentStep() {
            if (currentStepIndex >= spec.steps().size()) return null;
            return spec.steps().get(currentStepIndex);
        }
    }

    public void activateQuest(String questId) {
        questSpecs.stream()
                .filter(q -> q.id().equals(questId))
                .findFirst()
                .ifPresent(spec -> activeQuests.put(questId, new QuestState(questId, spec, 0, "active")));
    }

    public void activateByDay(int day) {
        for (QuestSpec spec : questSpecs) {
            if (spec.triggerDay() == day && !activeQuests.containsKey(spec.id())) {
                activeQuests.put(spec.id(), new QuestState(spec.id(), spec, 0, "active"));
            }
        }
    }

    public record StepCompletionResult(boolean completed, String questId, String stepId,
                                       List<DialogueEntry> dialogue, List<RewardSpec> rewards) {}

    public List<StepCompletionResult> onAction(String actionId, Map<String, Object> context) {
        List<StepCompletionResult> results = new ArrayList<>();
        for (var entry : new ArrayList<>(activeQuests.entrySet())) {
            QuestState state = entry.getValue();
            if (!"active".equals(state.status())) continue;
            StepSpec step = state.currentStep();
            if (step == null) continue;
            if (step.triggerAction().equals(actionId)) {
                int nextIndex = state.currentStepIndex() + 1;
                String newStatus = nextIndex >= state.spec().steps().size() ? "completed" : "active";
                activeQuests.put(entry.getKey(),
                        new QuestState(state.questId(), state.spec(), nextIndex, newStatus));
                results.add(new StepCompletionResult(true, state.questId(), step.id(),
                        step.dialogue(), step.rewards()));
            }
        }
        return results;
    }

    public Map<String, QuestState> getActiveQuests() {
        return Collections.unmodifiableMap(activeQuests);
    }
}
