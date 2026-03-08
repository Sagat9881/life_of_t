package com.sagat9881.lifeoft.domain.npc.engine;

import com.sagat9881.lifeoft.domain.npc.model.NpcInstance;
import com.sagat9881.lifeoft.domain.npc.model.NpcMood;
import com.sagat9881.lifeoft.domain.npc.spec.ConditionSpec;
import com.sagat9881.lifeoft.domain.model.session.GameSessionContext;

/**
 * Evaluates XML-defined conditions against NPC state and game context.
 * Supports: mood, memory, schedule, stat, day, relationship condition types.
 * No hardcoded NPC or action references — purely data-driven.
 */
public class ConditionEvaluator {

    public boolean evaluate(ConditionSpec condition, NpcInstance npc, GameSessionContext context) {
        return switch (condition.type()) {
            case "mood" -> evaluateMood(condition, npc);
            case "memory" -> evaluateMemory(condition, npc);
            case "schedule" -> evaluateSchedule(condition, npc, context);
            case "stat" -> evaluateStat(condition, context);
            case "day" -> evaluateDay(condition, context);
            case "relationship" -> evaluateRelationship(condition, npc, context);
            case "time" -> evaluateTime(condition, context);
            default -> {
                // Unknown condition type — log warning, treat as met
                System.err.println("Unknown condition type: " + condition.type());
                yield true;
            }
        };
    }

    private boolean evaluateMood(ConditionSpec c, NpcInstance npc) {
        double actual = getMoodAxis(npc.mood(), c.target());
        return compareValues(actual, c.operator(), c.value());
    }

    private boolean evaluateMemory(ConditionSpec c, NpcInstance npc) {
        if (npc.memory() == null) return false;
        return switch (c.target()) {
            case "is_being_ignored" -> npc.memory().isBeingIgnored(c.value().intValue());
            case "detect_pattern" -> npc.memory().detectPattern(c.operator());
            case "recent_interaction" -> npc.memory().hasRecentInteraction(c.operator(), c.value().intValue());
            default -> false;
        };
    }

    private boolean evaluateSchedule(ConditionSpec c, NpcInstance npc, GameSessionContext context) {
        int hour = context.time().hour();
        return switch (c.target()) {
            case "available" -> npc.schedule().isAvailable(hour);
            case "at_home" -> npc.schedule().isAtHome(hour);
            case "away" -> !npc.schedule().isAtHome(hour);
            default -> true;
        };
    }

    private boolean evaluateStat(ConditionSpec c, GameSessionContext context) {
        double actual = switch (c.target()) {
            case "stress" -> context.playerCharacter().stats().stress();
            case "energy" -> context.playerCharacter().stats().energy();
            case "mood" -> context.playerCharacter().stats().mood();
            case "money" -> context.playerCharacter().stats().money();
            case "self_esteem" -> context.playerCharacter().selfEsteem();
            default -> 0;
        };
        return compareValues(actual, c.operator(), c.value());
    }

    private boolean evaluateDay(ConditionSpec c, GameSessionContext context) {
        double actual = context.time().day();
        return compareValues(actual, c.operator(), c.value());
    }

    private boolean evaluateRelationship(ConditionSpec c, NpcInstance npc, GameSessionContext context) {
        // target = "closeness", "trust", "romance", etc.
        // operator = npc id to check relationship with, or comparison operator
        // For simplicity: check player's relationship with this NPC
        var relationships = context.playerCharacter().relationships();
        var rel = relationships.findByNpcId(npc.spec().id());
        if (rel.isEmpty()) return false;
        double actual = switch (c.target()) {
            case "closeness" -> rel.get().closeness();
            case "trust" -> rel.get().trust();
            case "romance" -> rel.get().romance();
            case "stability" -> rel.get().stability();
            default -> 0;
        };
        return compareValues(actual, c.operator(), c.value());
    }

    private boolean evaluateTime(ConditionSpec c, GameSessionContext context) {
        double actual = context.time().hour();
        return compareValues(actual, c.operator(), c.value());
    }

    private double getMoodAxis(NpcMood mood, String axis) {
        return switch (axis) {
            case "happiness" -> mood.happiness();
            case "anxiety" -> mood.anxiety();
            case "loneliness" -> mood.loneliness();
            case "irritability" -> mood.irritability();
            case "energy" -> mood.energy();
            case "affection" -> mood.affection();
            default -> 0;
        };
    }

    private boolean compareValues(double actual, String operator, Double expected) {
        if (expected == null) return true;
        return switch (operator) {
            case "gte", ">=" -> actual >= expected;
            case "lte", "<=" -> actual <= expected;
            case "gt", ">" -> actual > expected;
            case "lt", "<" -> actual < expected;
            case "eq", "==" -> Math.abs(actual - expected) < 0.001;
            case "neq", "!=" -> Math.abs(actual - expected) >= 0.001;
            default -> true;
        };
    }
}
