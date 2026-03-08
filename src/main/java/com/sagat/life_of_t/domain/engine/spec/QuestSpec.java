package com.sagat.life_of_t.domain.engine.spec;

import java.util.List;
import java.util.Map;

/**
 * Immutable specification of a quest loaded from XML.
 * Engine processes steps/objectives generically.
 */
public record QuestSpec(
        String id,
        String type,
        QuestMeta meta,
        List<ObjectiveSpec> objectives,
        List<StepSpec> steps,
        List<RewardSpec> rewards
) {
    public record QuestMeta(
            String title,
            String titleRu,
            String description,
            String descriptionRu,
            int chapter,
            String prerequisites
    ) {}

    public record ObjectiveSpec(
            String id,
            String type,
            String descriptionRu,
            String target,
            String location,
            int count
    ) {}

    public record StepSpec(
            int order,
            String objectiveRef,
            List<DialogueEntry> dialogues,
            List<RewardSpec> onComplete
    ) {}

    public record DialogueEntry(
            String speaker,
            String lineRu,
            String choice
    ) {}

    public record RewardSpec(
            String type,
            String stat,
            String target,
            String value,
            String condition,
            String unlockType,
            String unlockValue
    ) {
        public boolean isStatChange() { return "stat-change".equals(type); }
        public boolean isRelationshipChange() { return "relationship-change".equals(type); }
        public boolean isUnlock() { return "unlock".equals(type); }

        public int numericValue() {
            if (value == null) return 0;
            return Integer.parseInt(value.replace("+", ""));
        }
    }
}
