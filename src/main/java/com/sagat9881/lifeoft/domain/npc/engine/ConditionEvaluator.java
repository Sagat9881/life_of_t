package com.sagat9881.lifeoft.domain.npc.engine;

import com.sagat9881.lifeoft.domain.npc.NpcInstance;
import com.sagat9881.lifeoft.domain.npc.NpcMood;
import com.sagat9881.lifeoft.domain.npc.NpcMemoryLog;
import com.sagat9881.lifeoft.domain.npc.spec.ConditionSpec;
import com.sagat9881.lifeoft.domain.npc.spec.ConditionSpec.ConditionType;
import com.sagat9881.lifeoft.domain.npc.spec.ConditionSpec.Operator;

import java.util.List;
import java.util.Map;

/**
 * Interprets ConditionSpec against NpcInstance and game context.
 * This is the core abstraction layer — the engine never knows
 * concrete NPC names, actions, or stat names. Everything is data.
 */
public class ConditionEvaluator {

    /**
     * Evaluates all conditions. Returns true only if ALL conditions pass.
     */
    public boolean evaluateAll(List<ConditionSpec> conditions, NpcInstance npc,
                                Map<String, Object> gameContext) {
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }
        for (ConditionSpec condition : conditions) {
            if (!evaluate(condition, npc, gameContext)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculates total score modifier from all met conditions.
     */
    public double calculateScoreModifier(List<ConditionSpec> conditions, NpcInstance npc,
                                          Map<String, Object> gameContext) {
        if (conditions == null) return 0.0;
        double modifier = 0.0;
        for (ConditionSpec c : conditions) {
            if (c.hasScoreModifier() && evaluate(c, npc, gameContext)) {
                modifier += c.scoreModifier();
            }
        }
        return modifier;
    }

    public boolean evaluate(ConditionSpec spec, NpcInstance npc,
                            Map<String, Object> gameContext) {
        return switch (spec.type()) {
            case MOOD -> evaluateMood(spec, npc);
            case MEMORY -> evaluateMemory(spec, npc);
            case SCHEDULE -> evaluateSchedule(spec, npc, gameContext);
            case STAT -> evaluateStat(spec, gameContext);
            case DAY -> evaluateDay(spec, gameContext);
            case RELATIONSHIP -> evaluateRelationship(spec, gameContext);
            case PERSONALITY -> evaluatePersonality(spec, npc);
        };
    }

    private boolean evaluateMood(ConditionSpec spec, NpcInstance npc) {
        int value = npc.mood().axis(spec.target());
        return compare(value, spec.operator(), spec.value());
    }

    private boolean evaluateMemory(ConditionSpec spec, NpcInstance npc) {
        if (!npc.hasMemory()) return false;
        NpcMemoryLog memory = npc.memory();
        return switch (spec.target()) {
            case "isBeingIgnored" -> spec.operator() == Operator.EXISTS && memory.isBeingIgnored();
            case "detectWorkObsession" -> spec.operator() == Operator.EXISTS && memory.detectWorkObsession();
            case "recentInteractionCount" -> compare(memory.recentInteractionCount(), spec.operator(), spec.value());
            default -> false;
        };
    }

    private boolean evaluateSchedule(ConditionSpec spec, NpcInstance npc,
                                      Map<String, Object> gameContext) {
        int currentHour = getInt(gameContext, "currentHour", 12);
        return switch (spec.target()) {
            case "available" -> npc.isAvailableAt(currentHour);
            case "unavailable" -> !npc.isAvailableAt(currentHour);
            default -> false;
        };
    }

    private boolean evaluateStat(ConditionSpec spec, Map<String, Object> gameContext) {
        double value = getDouble(gameContext, "stat." + spec.target(), 0);
        return compare(value, spec.operator(), spec.value());
    }

    private boolean evaluateDay(ConditionSpec spec, Map<String, Object> gameContext) {
        int day = getInt(gameContext, "currentDay", 1);
        return compare(day, spec.operator(), spec.value());
    }

    private boolean evaluateRelationship(ConditionSpec spec, Map<String, Object> gameContext) {
        String key = spec.isCrossNpc()
                ? "relationship." + spec.npcId() + "." + spec.target()
                : "relationship." + spec.target();
        double value = getDouble(gameContext, key, 50);
        return compare(value, spec.operator(), spec.value());
    }

    private boolean evaluatePersonality(ConditionSpec spec, NpcInstance npc) {
        int value = npc.spec().personalityTrait(spec.target());
        return compare(value, spec.operator(), spec.value());
    }

    private boolean compare(double actual, Operator op, double threshold) {
        return switch (op) {
            case GTE -> actual >= threshold;
            case LTE -> actual <= threshold;
            case GT -> actual > threshold;
            case LT -> actual < threshold;
            case EQ -> Math.abs(actual - threshold) < 0.001;
            case NEQ -> Math.abs(actual - threshold) >= 0.001;
            case BETWEEN -> false; // requires two values, handled separately
            case EXISTS -> actual != 0;
        };
    }

    private int getInt(Map<String, Object> ctx, String key, int def) {
        Object v = ctx.get(key);
        if (v instanceof Number n) return n.intValue();
        return def;
    }

    private double getDouble(Map<String, Object> ctx, String key, double def) {
        Object v = ctx.get(key);
        if (v instanceof Number n) return n.doubleValue();
        return def;
    }
}
