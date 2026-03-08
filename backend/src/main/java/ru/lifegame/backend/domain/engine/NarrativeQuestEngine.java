package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.spec.QuestSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.StepSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.RewardSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.DialogueEntry;
import ru.lifegame.backend.domain.engine.spec.ConditionSpec;

import java.util.*;

public class NarrativeQuestEngine {

    private final List<QuestSpec> allQuests;
    private final Map<String, QuestState> activeQuests = new LinkedHashMap<>();
    private final Set<String> completedQuests = new HashSet<>();

    public NarrativeQuestEngine(List<QuestSpec> allQuests) {
        this.allQuests = allQuests;
    }

    public record QuestState(String questId, QuestSpec spec, int currentStepIndex) {
        public StepSpec currentStep() {
            if (currentStepIndex >= spec.steps().size()) return null;
            return spec.steps().get(currentStepIndex);
        }

        public QuestState advance() {
            return new QuestState(questId, spec, currentStepIndex + 1);
        }

        public boolean isComplete() {
            return currentStepIndex >= spec.steps().size();
        }
    }

    public void activateEligibleQuests(Map<String, Object> gameContext) {
        for (QuestSpec quest : allQuests) {
            if (activeQuests.containsKey(quest.id())) continue;
            if (completedQuests.contains(quest.id())) continue;
            if (allConditionsMet(quest.triggerConditions(), gameContext)) {
                activeQuests.put(quest.id(), new QuestState(quest.id(), quest, 0));
            }
        }
    }

    public record StepCompletionResult(String questId, String stepId, boolean questCompleted,
                                       DialogueEntry dialogue, RewardSpec reward) {}

    public List<StepCompletionResult> checkStepCompletions(Map<String, Object> gameContext) {
        List<StepCompletionResult> results = new ArrayList<>();
        for (var entry : new ArrayList<>(activeQuests.entrySet())) {
            QuestState state = entry.getValue();
            StepSpec step = state.currentStep();
            if (step == null) continue;
            if (allConditionsMet(step.completionConditions(), gameContext)) {
                QuestState advanced = state.advance();
                if (advanced.isComplete()) {
                    activeQuests.remove(entry.getKey());
                    completedQuests.add(entry.getKey());
                    results.add(new StepCompletionResult(entry.getKey(), step.id(), true,
                            step.dialogue(), state.spec().completionReward()));
                } else {
                    activeQuests.put(entry.getKey(), advanced);
                    results.add(new StepCompletionResult(entry.getKey(), step.id(), false,
                            step.dialogue(), step.reward()));
                }
            }
        }
        return results;
    }

    public Map<String, QuestState> activeQuests() { return Collections.unmodifiableMap(activeQuests); }
    public Set<String> completedQuests() { return Collections.unmodifiableSet(completedQuests); }

    private boolean allConditionsMet(List<ConditionSpec> conditions, Map<String, Object> ctx) {
        if (conditions == null || conditions.isEmpty()) return true;
        return conditions.stream().allMatch(c -> evaluateCondition(c, ctx));
    }

    private boolean evaluateCondition(ConditionSpec c, Map<String, Object> ctx) {
        Object val = ctx.get(c.target());
        if (val == null) return false;
        if (val instanceof Number num) {
            double actual = num.doubleValue();
            double expected = Double.parseDouble(c.value());
            return switch (c.operator()) {
                case "gte" -> actual >= expected;
                case "lte" -> actual <= expected;
                case "gt" -> actual > expected;
                case "lt" -> actual < expected;
                case "eq" -> actual == expected;
                default -> false;
            };
        }
        return String.valueOf(val).equals(c.value());
    }
}
