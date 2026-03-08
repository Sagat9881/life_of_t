package ru.lifegame.backend.domain.engine.runtime;

import ru.lifegame.backend.domain.engine.spec.ConditionSpec;
import ru.lifegame.backend.domain.npc.NpcMood;

public class ConditionEvaluator {

    public boolean evaluate(ConditionSpec condition, NpcInstance npc, Object gameContext) {
        if (condition.type().equals("mood")) {
            return evaluateMoodCondition(condition, npc.mood());
        }
        if (condition.type().equals("schedule")) {
            return "available".equals(condition.value());
        }
        if (condition.type().equals("memory")) {
            return evaluateMemoryCondition(condition, npc);
        }
        if (condition.type().equals("day")) {
            return true;
        }
        return true;
    }

    private boolean evaluateMoodCondition(ConditionSpec condition, NpcMood mood) {
        int actual = getMoodValue(condition.target(), mood);
        int threshold = Integer.parseInt(condition.value());
        return switch (condition.operator()) {
            case "gte" -> actual >= threshold;
            case "lte" -> actual <= threshold;
            case "gt" -> actual > threshold;
            case "lt" -> actual < threshold;
            case "eq" -> actual == threshold;
            default -> false;
        };
    }

    private int getMoodValue(String axis, NpcMood mood) {
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

    private boolean evaluateMemoryCondition(ConditionSpec condition, NpcInstance npc) {
        return switch (condition.target()) {
            case "isBeingIgnored" -> npc.memory().isBeingIgnored(3);
            case "detectWorkObsession" -> npc.memory().detectWorkObsession();
            default -> false;
        };
    }
}
