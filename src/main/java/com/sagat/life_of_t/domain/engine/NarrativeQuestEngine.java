package com.sagat.life_of_t.domain.engine;

import com.sagat.life_of_t.domain.engine.spec.QuestSpec;
import com.sagat.life_of_t.domain.engine.spec.QuestSpec.*;

import java.util.*;

/**
 * Manages quest lifecycle: activation, step progression, completion.
 * All quest content comes from XML — engine only processes abstract steps.
 */
public class NarrativeQuestEngine {

    private final List<QuestSpec> allQuests;
    private final Map<String, QuestState> activeQuests = new LinkedHashMap<>();
    private final Set<String> completedQuestIds = new LinkedHashSet<>();

    public NarrativeQuestEngine(List<QuestSpec> allQuests) {
        this.allQuests = allQuests;
    }

    public record QuestState(
            QuestSpec spec,
            int currentStepIndex,
            Map<String, Integer> objectiveProgress
    ) {
        public boolean isComplete() {
            return currentStepIndex >= spec.steps().size();
        }

        public StepSpec currentStep() {
            if (isComplete()) return null;
            return spec.steps().get(currentStepIndex);
        }
    }

    public List<QuestSpec> checkActivatable(int currentDay, Set<String> completedPrereqs) {
        List<QuestSpec> activatable = new ArrayList<>();
        for (QuestSpec quest : allQuests) {
            if (activeQuests.containsKey(quest.id())) continue;
            if (completedQuestIds.contains(quest.id())) continue;

            String prereqs = quest.meta().prerequisites();
            if (prereqs != null && !prereqs.isBlank()) {
                boolean allMet = Arrays.stream(prereqs.split(","))
                        .map(String::trim)
                        .allMatch(completedPrereqs::contains);
                if (!allMet) continue;
            }
            activatable.add(quest);
        }
        return activatable;
    }

    public void activateQuest(String questId) {
        allQuests.stream()
                .filter(q -> q.id().equals(questId))
                .findFirst()
                .ifPresent(spec -> {
                    Map<String, Integer> progress = new LinkedHashMap<>();
                    for (ObjectiveSpec obj : spec.objectives()) {
                        progress.put(obj.id(), 0);
                    }
                    activeQuests.put(questId, new QuestState(spec, 0, progress));
                });
    }

    public record StepCompletionResult(
            String questId,
            int stepOrder,
            List<DialogueEntry> dialogues,
            List<RewardSpec> rewards,
            boolean questCompleted
    ) {}

    public Optional<StepCompletionResult> advanceObjective(String objectiveId, String playerChoice) {
        for (var entry : activeQuests.entrySet()) {
            QuestState state = entry.getValue();
            StepSpec currentStep = state.currentStep();
            if (currentStep == null) continue;
            if (!currentStep.objectiveRef().equals(objectiveId)) continue;

            String objId = currentStep.objectiveRef();
            int newProgress = state.objectiveProgress().getOrDefault(objId, 0) + 1;

            ObjectiveSpec objective = state.spec().objectives().stream()
                    .filter(o -> o.id().equals(objId))
                    .findFirst().orElse(null);

            if (objective != null && newProgress >= objective.count()) {
                int nextStep = state.currentStepIndex() + 1;
                boolean questDone = nextStep >= state.spec().steps().size();

                List<DialogueEntry> dialogues = currentStep.dialogues();
                if (playerChoice != null) {
                    dialogues = dialogues.stream()
                            .filter(d -> d.choice() == null || d.choice().equals(playerChoice))
                            .toList();
                }

                List<RewardSpec> rewards = new ArrayList<>(currentStep.onComplete());
                if (playerChoice != null) {
                    rewards = rewards.stream()
                            .filter(r -> r.condition() == null || r.condition().contains(playerChoice))
                            .toList();
                }

                if (questDone) {
                    rewards = new ArrayList<>(rewards);
                    rewards.addAll(state.spec().rewards());
                    completedQuestIds.add(entry.getKey());
                    activeQuests.remove(entry.getKey());
                } else {
                    Map<String, Integer> updatedProgress = new LinkedHashMap<>(state.objectiveProgress());
                    updatedProgress.put(objId, newProgress);
                    activeQuests.put(entry.getKey(), new QuestState(state.spec(), nextStep, updatedProgress));
                }

                return Optional.of(new StepCompletionResult(
                        entry.getKey(), currentStep.order(), dialogues, rewards, questDone
                ));
            } else {
                Map<String, Integer> updatedProgress = new LinkedHashMap<>(state.objectiveProgress());
                updatedProgress.put(objId, newProgress);
                activeQuests.put(entry.getKey(), new QuestState(state.spec(), state.currentStepIndex(), updatedProgress));
            }
        }
        return Optional.empty();
    }

    public Map<String, QuestState> activeQuests() { return Collections.unmodifiableMap(activeQuests); }
    public Set<String> completedQuestIds() { return Collections.unmodifiableSet(completedQuestIds); }
}
