package com.sagat9881.lifeoft.domain.npc.spec;

/**
 * Universal predicate loaded from XML. The engine interprets this
 * against NpcInstance + GameSessionContext without knowing concrete NPC names.
 *
 * Supported types:
 * - MOOD: checks NPC mood axis (e.g. loneliness >= 40)
 * - MEMORY: checks NPC memory patterns (e.g. isBeingIgnored, detectWorkObsession)
 * - SCHEDULE: checks NPC availability (e.g. isAvailable at current hour)
 * - STAT: checks player stat (e.g. stress > 70, money < 200)
 * - DAY: checks game day (e.g. day >= 10)
 * - RELATIONSHIP: checks relationship metric (e.g. closeness < 30)
 * - PERSONALITY: checks NPC personality trait
 *
 * @param type condition category
 * @param target what to check (axis name, stat name, pattern name)
 * @param operator comparison operator
 * @param value threshold value
 * @param npcId optional: target NPC for cross-NPC conditions
 * @param scoreModifier how much to add/subtract from action score when condition is met (default 0)
 */
public record ConditionSpec(
        ConditionType type,
        String target,
        Operator operator,
        double value,
        String npcId,
        double scoreModifier
) {

    public enum ConditionType {
        MOOD, MEMORY, SCHEDULE, STAT, DAY, RELATIONSHIP, PERSONALITY
    }

    public enum Operator {
        GTE, LTE, GT, LT, EQ, NEQ, BETWEEN, EXISTS
    }

    /**
     * Convenience constructor without npcId and scoreModifier.
     */
    public ConditionSpec(ConditionType type, String target, Operator operator, double value) {
        this(type, target, operator, value, null, 0.0);
    }

    public boolean hasScoreModifier() {
        return scoreModifier != 0.0;
    }

    public boolean isCrossNpc() {
        return npcId != null && !npcId.isBlank();
    }
}
