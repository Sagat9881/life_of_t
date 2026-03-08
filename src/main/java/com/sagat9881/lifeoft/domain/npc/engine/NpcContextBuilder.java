package com.sagat9881.lifeoft.domain.npc.engine;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds the game context Map<String, Object> that ConditionEvaluator uses.
 * This is the bridge between GameSessionContext and the NPC engine.
 * 
 * The engine operates on abstract keys — it never imports domain model classes.
 * Context keys follow a naming convention:
 *   - "currentDay" -> int
 *   - "currentHour" -> int
 *   - "stat.{name}" -> double (e.g. "stat.stress", "stat.money", "stat.mood")
 *   - "relationship.{npcId}.{metric}" -> double (e.g. "relationship.alexander.closeness")
 *   - "skill.{name}" -> int (e.g. "skill.empathy")
 */
public class NpcContextBuilder {

    private final Map<String, Object> context = new HashMap<>();

    public NpcContextBuilder day(int day) {
        context.put("currentDay", day);
        return this;
    }

    public NpcContextBuilder hour(int hour) {
        context.put("currentHour", hour);
        return this;
    }

    public NpcContextBuilder stat(String name, double value) {
        context.put("stat." + name, value);
        return this;
    }

    public NpcContextBuilder relationship(String npcId, String metric, double value) {
        context.put("relationship." + npcId + "." + metric, value);
        return this;
    }

    public NpcContextBuilder skill(String name, int value) {
        context.put("skill." + name, value);
        return this;
    }

    public NpcContextBuilder put(String key, Object value) {
        context.put(key, value);
        return this;
    }

    public Map<String, Object> build() {
        return Map.copyOf(context);
    }

    /**
     * Merges another context map into this builder.
     */
    public NpcContextBuilder merge(Map<String, Object> other) {
        context.putAll(other);
        return this;
    }
}
