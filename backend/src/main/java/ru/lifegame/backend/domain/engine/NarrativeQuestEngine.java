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
        this.questSpecs = questSpecs;
    }

    public void activateQuest(String questId) {
        questSpecs.stream()
                .filter(q -> q.id().equals(questId))
                .findFirst()
                .ifPresent(q -> activeQuests.put(questId, new QuestState(q, 0)));
    }

    public record QuestState(QuestSpec spec, int currentStepIndex) {
        public boolean isComplete() {
            return currentStepIndex >= spec.steps().size();
        }
        public StepSpec currentStep() {
            return spec.steps().get(currentStepIndex);
        }
        public QuestState advance() {
            return new QuestState(spec, currentStepIndex + 1);
        }
    }

    public Optional<StepCompletionResult> tryCompleteStep(String questId, String actionId, Map<String, Object> context) {
        QuestState state = activeQuests.get(questId);
        if (state == null || state.isComplete()) return Optional.empty();

        StepSpec step = state.currentStep();
        if (!step.actionId().equals(actionId)) return Optional.empty();

        if (step.conditions() != null) {
            for (var cond : step.conditions()) {
                Object val = context.get(cond.target());
                if (val == null) return Optional.empty();
                if (val instanceof Number num) {
                    double threshold = Double.parseDouble(cond.value());
                    if ("gte".equals(cond.operator()) && num.doubleValue() < threshold) return Optional.empty();
                    if ("lte".equals(cond.operator()) && num.doubleValue() > threshold) return Optional.empty();
                }
            }
        }

        QuestState advanced = state.advance();
        activeQuests.put(questId, advanced);

        return Optional.of(new StepCompletionResult(
                questId, step.id(), advanced.isComplete(),
                step.dialogue(), step.rewards()
        ));
    }

    public record StepCompletionResult(
            String questId, String stepId, boolean questComplete,
            List<DialogueEntry> dialogue, List<RewardSpec> rewards
    ) {}

    public Map<String, QuestState> activeQuests() {
        return Collections.unmodifiableMap(activeQuests);
    }

    public void checkAutoActivation(int day, Map<String, Object> context) {
        for (QuestSpec q : questSpecs) {
            if (activeQuests.containsKey(q.id())) continue;
            if (q.autoActivateDay() > 0 && day >= q.autoActivateDay()) {
                activateQuest(q.id());
            }
        }
    }
}
