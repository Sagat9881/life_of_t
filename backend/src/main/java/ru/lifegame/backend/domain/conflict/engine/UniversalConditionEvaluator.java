package ru.lifegame.backend.domain.conflict.engine;

import ru.lifegame.backend.domain.conflict.spec.TriggerCondition;
import ru.lifegame.backend.domain.model.player.Player;
import ru.lifegame.backend.domain.model.relationship.Relationships;
import ru.lifegame.backend.domain.model.time.GameTime;

import java.util.Map;

/**
 * Universal condition evaluator for data-driven conflict triggers.
 * Supports: stat, relationship, time-spent, quest, action-count, etc.
 */
public class UniversalConditionEvaluator {

    public boolean evaluate(TriggerCondition condition, Map<String, Object> context) {
        return switch (condition.type()) {
            case "stat" -> evaluateStat(condition, context);
            case "relationship" -> evaluateRelationship(condition, context);
            case "time-spent" -> evaluateTimeSpent(condition, context);
            case "quest" -> evaluateQuest(condition, context);
            case "action-count" -> evaluateActionCount(condition, context);
            case "days-played" -> evaluateDaysPlayed(condition, context);
            default -> throw new IllegalArgumentException("Unknown condition type: " + condition.type());
        };
    }

    private boolean evaluateStat(TriggerCondition condition, Map<String, Object> context) {
        Player player = (Player) context.get("player");
        if (player == null) return false;

        double actualValue = switch (condition.field()) {
            case "stress" -> player.stats().stress();
            case "mood" -> player.stats().mood();
            case "energy" -> player.stats().energy();
            case "health" -> player.stats().health();
            case "self_esteem" -> player.stats().selfEsteem();
            case "money" -> player.stats().money();
            case "burnout_risk" -> player.job().burnoutRisk();
            case "job_satisfaction" -> player.job().satisfaction();
            default -> throw new IllegalArgumentException("Unknown stat field: " + condition.field());
        };

        return compare(actualValue, condition.operator(), condition.value());
    }

    private boolean evaluateRelationship(TriggerCondition condition, Map<String, Object> context) {
        Relationships relationships = (Relationships) context.get("relationships");
        if (relationships == null || condition.target() == null) return false;

        var rel = relationships.get(condition.target());
        if (rel == null) return false;

        double actualValue = switch (condition.field()) {
            case "closeness" -> rel.closeness();
            case "trust" -> rel.trust();
            case "romance" -> rel.romance();
            case "stability" -> rel.stability();
            default -> throw new IllegalArgumentException("Unknown relationship field: " + condition.field());
        };

        return compare(actualValue, condition.operator(), condition.value());
    }

    private boolean evaluateTimeSpent(TriggerCondition condition, Map<String, Object> context) {
        Map<String, Integer> timeSpent = (Map<String, Integer>) context.get("timeSpent");
        if (timeSpent == null) return false;

        int hours = timeSpent.getOrDefault(condition.field(), 0);
        return compare(hours, condition.operator(), condition.value());
    }

    private boolean evaluateQuest(TriggerCondition condition, Map<String, Object> context) {
        Map<String, String> questStates = (Map<String, String>) context.get("questStates");
        if (questStates == null || condition.questId() == null) return false;

        String actualState = questStates.get(condition.questId());
        return condition.questState().equals(actualState);
    }

    private boolean evaluateActionCount(TriggerCondition condition, Map<String, Object> context) {
        Map<String, Integer> actionCounts = (Map<String, Integer>) context.get("actionCounts");
        if (actionCounts == null) return false;

        int count = actionCounts.getOrDefault(condition.field(), 0);
        return compare(count, condition.operator(), condition.value());
    }

    private boolean evaluateDaysPlayed(TriggerCondition condition, Map<String, Object> context) {
        GameTime time = (GameTime) context.get("time");
        if (time == null) return false;

        return compare(time.day(), condition.operator(), condition.value());
    }

    private boolean compare(double actual, String operator, Double threshold) {
        if (threshold == null) return false;

        return switch (operator) {
            case "gte" -> actual >= threshold;
            case "lte" -> actual <= threshold;
            case "gt" -> actual > threshold;
            case "lt" -> actual < threshold;
            case "eq" -> Math.abs(actual - threshold) < 0.01;
            default -> throw new IllegalArgumentException("Unknown operator: " + operator);
        };
    }
}
