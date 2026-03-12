package ru.lifegame.backend.domain.ending.spec;

import ru.lifegame.backend.domain.ending.spec.EndingSpec.EndingCondition;
import ru.lifegame.backend.domain.model.character.PlayerCharacter;
import ru.lifegame.backend.domain.model.relationship.Relationship;
import ru.lifegame.backend.domain.model.relationship.Relationships;
import ru.lifegame.backend.domain.quest.QuestLog;

public class EndingConditionEvaluator {

    public boolean evaluate(
        EndingCondition condition,
        PlayerCharacter player,
        Relationships relationships,
        QuestLog questLog
    ) {
        return switch (condition.type()) {
            case "stat" -> evaluateStat(condition, player, relationships);
            case "relationship" -> evaluateRelationship(condition, relationships);
            case "quest" -> evaluateQuest(condition, questLog);
            default -> throw new IllegalArgumentException("Unknown condition type: " + condition.type());
        };
    }

    private boolean evaluateStat(EndingCondition condition, PlayerCharacter player, Relationships relationships) {
        double actualValue = switch (condition.field()) {
            case "job_satisfaction" -> player.job().satisfaction();
            case "burnout_risk" -> player.job().burnoutRisk();
            case "mood" -> player.stats().mood();
            case "self_esteem" -> player.stats().selfEsteem();
            case "stress" -> player.stats().stress();
            case "energy" -> player.stats().energy();
            case "health" -> player.stats().health();
            case "money" -> player.stats().money();
            case "total_closeness" -> relationships.totalCloseness();
            default -> throw new IllegalArgumentException("Unknown stat field: " + condition.field());
        };

        return compare(actualValue, condition.operator(), condition.value());
    }

    private boolean evaluateRelationship(EndingCondition condition, Relationships relationships) {
        if (condition.target() == null) return false;

        Relationship rel = relationships.get(condition.target());
        if (rel == null) return false;

        double actualValue = switch (condition.field()) {
            case "closeness" -> rel.closeness();
            case "trust" -> rel.trust();
            case "romance" -> rel.romance();
            case "stability" -> rel.stability();
            case "broken" -> rel.broken() ? 1.0 : 0.0;
            default -> throw new IllegalArgumentException("Unknown relationship field: " + condition.field());
        };

        return compare(actualValue, condition.operator(), condition.value());
    }

    private boolean evaluateQuest(EndingCondition condition, QuestLog questLog) {
        if (condition.questId() == null) return false;

        boolean completed =  questLog.getCompletedQuestIds().contains(condition.questId());

        return switch (condition.state()) {
            case "COMPLETED" -> completed;
            case "ACTIVE" -> questLog.hasActiveQuest(condition.questId());
            default -> false;
        };
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
