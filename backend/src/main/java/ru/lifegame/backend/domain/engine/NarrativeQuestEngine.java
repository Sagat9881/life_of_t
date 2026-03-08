package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.spec.QuestSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.*;
import ru.lifegame.backend.domain.engine.spec.ConditionSpec;

import java.util.*;

public class NarrativeQuestEngine {

    private final List<QuestSpec> questSpecs;
    private final Map<String, QuestState> activeQuests = new LinkedHashMap<>();
    private final Set<String> completedQuests = new HashSet<>();

    public NarrativeQuestEngine(List<QuestSpec> questSpecs) {
        this.questSpecs = questSpecs;
    }

    public record QuestState(QuestSpec spec, int currentStepIndex, Map<String, Integer> objectiveProgress) {
        public StepSpec currentStep() {
            if (currentStepIndex >= spec.steps().size()) return null;
            return spec.steps().get(currentStepIndex);
        }
    }

    public void checkTriggers(int currentDay, Map<String, Object> context) {
        for (var spec : questSpecs) {
            if (activeQuests.containsKey(spec.id()) || completedQuests.contains(spec.id())) continue;
            if (spec.meta().triggerDay() > 0 && currentDay >= spec.meta().triggerDay()) {
                activeQuests.put(spec.id(), new QuestState(spec, 0, new HashMap<>()));
            }
        }
    }

    public record StepCompletionResult(String questId, String stepId, boolean questCompleted, DialogueEntry dialogue, List<RewardSpec> rewards) {}

    public List<StepCompletionResult> updateProgress(String actionType, Map<String, Object> context) {
        List<StepCompletionResult> results = new ArrayList<>();
        var it = activeQuests.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            var state = entry.getValue();
            var step = state.currentStep();
            if (step == null) continue;
            boolean stepComplete = checkStepObjectives(step, actionType, state.objectiveProgress());
            if (stepComplete) {
                int nextIndex = state.currentStepIndex() + 1;
                boolean questDone = nextIndex >= state.spec().steps().size();
                results.add(new StepCompletionResult(entry.getKey(), step.id(), questDone, step.dialogue(), step.rewards()));
                if (questDone) {
                    completedQuests.add(entry.getKey());
                    it.remove();
                } else {
                    activeQuests.put(entry.getKey(), new QuestState(state.spec(), nextIndex, new HashMap<>()));
                }
            }
        }
        return results;
    }

    private boolean checkStepObjectives(StepSpec step, String actionType, Map<String, Integer> progress) {
        if (step.objectives() == null || step.objectives().isEmpty()) return false;
        for (var obj : step.objectives()) {
            if (obj.type().equals("action") && obj.target().equals(actionType)) {
                int current = progress.getOrDefault(obj.target(), 0) + 1;
                progress.put(obj.target(), current);
                if (current < obj.count()) return false;
            }
        }
        return true;
    }

    public Map<String, QuestState> activeQuests() { return Collections.unmodifiableMap(activeQuests); }
    public Set<String> completedQuests() { return Collections.unmodifiableSet(completedQuests); }
}
