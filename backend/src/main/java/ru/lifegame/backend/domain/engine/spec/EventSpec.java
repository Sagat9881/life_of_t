package ru.lifegame.backend.application.engine.spec;

import java.util.List;
import java.util.Map;

/**
 * Immutable specification of a game event loaded from XML.
 * Engine evaluates conditions generically — no hardcoded event IDs.
 */
public record EventSpec(
        String id,
        String type,
        EventMeta meta,
        List<ConditionSpec> conditions,
        List<EffectSpec> effects
) {
    public record EventMeta(
            String titleRu,
            String descriptionRu,
            double probability,
            int cooldownHours
    ) {}

    public record ConditionSpec(
            String type,
            String value,
            Map<String, String> attributes
    ) {}

    public record EffectSpec(
            String type,
            String stat,
            String target,
            String value,
            String speaker,
            String lineRu,
            String choice
    ) {
        public boolean isStatChange() { return "stat-change".equals(type); }
        public boolean isRelationshipChange() { return "relationship-change".equals(type); }
        public boolean isDialogue() { return "dialogue".equals(type); }

        public int numericValue() {
            if (value == null) return 0;
            String clean = value.replace("+", "");
            return Integer.parseInt(clean);
        }
    }
}
