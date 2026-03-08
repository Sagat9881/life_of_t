package com.sagat9881.lifeoft.domain.npc.engine;

import com.sagat9881.lifeoft.domain.npc.model.ConditionSpec;
import com.sagat9881.lifeoft.domain.npc.model.NpcInstance;

/**
 * Interprets ConditionSpec against NpcInstance + game context.
 * This is the core abstraction that keeps the engine data-driven:
 * conditions are declared in XML, evaluated here generically.
 * 
 * Supports: mood, memory, schedule, stat, day, relationship conditions.
 */
public class ConditionEvaluator {

    /**
     * Evaluate a single condition against NPC state and game context.
     * 
     * @param spec condition from XML
     * @param npc the NPC being evaluated
     * @param gameContext opaque game session context (cast internally)
     * @return true if condition is met
     */
    public boolean evaluate(ConditionSpec spec, NpcInstance npc, Object gameContext) {
        return switch (spec.type()) {
            case "mood" -> evaluateMood(spec, npc);
            case "memory" -> evaluateMemory(spec, npc);
            case "schedule" -> evaluateSchedule(spec, npc, gameContext);
            case "stat" -> evaluateStat(spec, gameContext);
            case "day" -> evaluateDay(spec, gameContext);
            case "relationship" -> evaluateRelationship(spec, gameContext);
            case "always" -> true;
            default -> false;
        };
    }

    private boolean evaluateMood(ConditionSpec spec, NpcInstance npc) {
        double actual = npc.mood().getAxis(spec.axis());
        double threshold = parseDouble(spec.value());
        return compare(actual, threshold, spec.operator());
    }

    private boolean evaluateMemory(ConditionSpec spec, NpcInstance npc) {
        if (!npc.memory().isEnabled()) return false;

        return switch (spec.check()) {
            case "isBeingIgnored" -> npc.memory().isBeingIgnored(
                    extractDay(spec), (int) parseDouble(spec.value()));
            case "detectObsession" -> npc.memory().detectObsession(
                    spec.target(), (int) parseDouble(spec.value()));
            case "hasInteractionToday" -> npc.memory().hasInteractionToday();
            case "noInteractionToday" -> !npc.memory().hasInteractionToday();
            default -> false;
        };
    }

    private boolean evaluateSchedule(ConditionSpec spec, NpcInstance npc, Object gameContext) {
        int currentHour = extractHour(gameContext);
        return switch (spec.check()) {
            case "available" -> npc.isAvailable(currentHour);
            case "away" -> !npc.isAvailable(currentHour);
            default -> true;
        };
    }

    private boolean evaluateStat(ConditionSpec spec, Object gameContext) {
        double actual = extractPlayerStat(spec.target(), gameContext);
        double threshold = parseDouble(spec.value());
        return compare(actual, threshold, spec.operator());
    }

    private boolean evaluateDay(ConditionSpec spec, Object gameContext) {
        int currentDay = extractDay(gameContext);
        int threshold = (int) parseDouble(spec.value());
        return compare(currentDay, threshold, spec.operator());
    }

    private boolean evaluateRelationship(ConditionSpec spec, Object gameContext) {
        double actual = extractRelationshipValue(spec.target(), spec.axis(), gameContext);
        double threshold = parseDouble(spec.value());
        return compare(actual, threshold, spec.operator());
    }

    private boolean compare(double actual, double threshold, String operator) {
        return switch (operator) {
            case "gte", ">=" -> actual >= threshold;
            case "lte", "<=" -> actual <= threshold;
            case "gt", ">" -> actual > threshold;
            case "lt", "<" -> actual < threshold;
            case "eq", "==" -> Math.abs(actual - threshold) < 0.001;
            default -> actual >= threshold;
        };
    }

    // --- Context extraction methods ---
    // These use reflection-free approach: gameContext is expected to be
    // a Map<String, Object> or GameSessionContext with known accessors.
    // In production, replace with typed interface.

    private int extractHour(Object gameContext) {
        if (gameContext instanceof java.util.Map) {
            Object h = ((java.util.Map<?, ?>) gameContext).get("hour");
            return h != null ? ((Number) h).intValue() : 12;
        }
        return 12;
    }

    private int extractDay(Object gameContext) {
        if (gameContext instanceof java.util.Map) {
            Object d = ((java.util.Map<?, ?>) gameContext).get("day");
            return d != null ? ((Number) d).intValue() : 1;
        }
        return 1;
    }

    private double extractPlayerStat(String statName, Object gameContext) {
        if (gameContext instanceof java.util.Map) {
            Object stats = ((java.util.Map<?, ?>) gameContext).get("stats");
            if (stats instanceof java.util.Map) {
                Object val = ((java.util.Map<?, ?>) stats).get(statName);
                return val != null ? ((Number) val).doubleValue() : 0;
            }
        }
        return 0;
    }

    private double extractRelationshipValue(String npcId, String axis, Object gameContext) {
        if (gameContext instanceof java.util.Map) {
            Object rels = ((java.util.Map<?, ?>) gameContext).get("relationships");
            if (rels instanceof java.util.Map) {
                Object rel = ((java.util.Map<?, ?>) rels).get(npcId);
                if (rel instanceof java.util.Map) {
                    Object val = ((java.util.Map<?, ?>) rel).get(axis);
                    return val != null ? ((Number) val).doubleValue() : 0;
                }
            }
        }
        return 0;
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int extractDay(ConditionSpec spec) {
        try {
            return Integer.parseInt(spec.value());
        } catch (NumberFormatException e) {
            return 3;
        }
    }
}
