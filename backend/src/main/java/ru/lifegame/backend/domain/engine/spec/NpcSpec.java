package ru.lifegame.backend.application.engine.spec;

import java.util.List;
import java.util.Map;

/**
 * Immutable specification of an NPC loaded from XML.
 * The engine knows nothing about specific NPCs — all behavior is data-driven.
 */
public record NpcSpec(
        String entityId,
        NpcType type,
        Map<String, Integer> personalityTraits,
        List<ScheduleSlot> schedules,
        List<ReactionSpec> reactions
) {
    public enum NpcType { NAMED, FILLER }

    public record ScheduleSlot(
            String timeOfDay,
            List<ActionSpec> actions
    ) {}

    public record ActionSpec(
            String type,
            String target,
            String value,
            double probability,
            String lineRu
    ) {
        public ActionSpec(String type, String target, String value) {
            this(type, target, value, 1.0, null);
        }
    }

    public record ReactionSpec(
            String trigger,
            Map<String, String> attributes,
            List<ActionSpec> actions
    ) {}

    public int trait(String name) {
        return personalityTraits.getOrDefault(name, 0);
    }

    public boolean isNamed() {
        return type == NpcType.NAMED;
    }
}
