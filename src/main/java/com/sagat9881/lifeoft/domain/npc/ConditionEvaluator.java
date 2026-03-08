package com.sagat9881.lifeoft.domain.npc;

/**
 * Evaluates XML-defined conditions against live NPC and game state.
 * This is the bridge between data-driven specs and runtime logic.
 * The evaluator is generic — it doesn't know about specific NPCs or actions.
 */
public class ConditionEvaluator {

    /**
     * Evaluate a single condition against an NPC instance and game context.
     *
     * @param condition  the condition spec from XML
     * @param npc        the NPC being evaluated
     * @param gameContext  the game session context (passed as Object for decoupling)
     * @return true if condition is met
     */
    public boolean evaluate(ConditionSpec condition, NpcInstance npc, Object gameContext) {
        return switch (condition.type()) {
            case "mood" -> evaluateMood(condition, npc);
            case "memory" -> evaluateMemory(condition, npc);
            case "schedule" -> evaluateSchedule(condition, npc);
            case "energy" -> evaluateEnergy(condition, npc);
            default -> true; // Unknown condition types pass by default
        };
    }

    private boolean evaluateMood(ConditionSpec cond, NpcInstance npc) {
        NpcMood mood = npc.mood();
        int actual = switch (cond.axis()) {
            case "happiness" -> mood.happiness();
            case "anxiety" -> mood.anxiety();
            case "loneliness" -> mood.loneliness();
            case "irritability" -> mood.irritability();
            case "energy" -> mood.energy();
            case "affection" -> mood.affection();
            default -> 0;
        };
        return compareInt(actual, cond.operator(), cond.intValue(0));
    }

    private boolean evaluateMemory(ConditionSpec cond, NpcInstance npc) {
        if (!npc.memory().isEnabled()) return false;
        if ("work_obsession".equals(cond.pattern())) {
            return npc.memory().detectWorkObsession();
        }
        if ("being_ignored".equals(cond.pattern())) {
            return npc.memory().isBeingIgnored();
        }
        if (cond.pattern() != null) {
            return npc.memory().hasPattern(cond.pattern());
        }
        return false;
    }

    private boolean evaluateSchedule(ConditionSpec cond, NpcInstance npc) {
        if ("available".equals(cond.check())) {
            return npc.isAvailable();
        }
        if ("away".equals(cond.check())) {
            return !npc.isAvailable();
        }
        return true;
    }

    private boolean evaluateEnergy(ConditionSpec cond, NpcInstance npc) {
        return compareInt(npc.mood().energy(), cond.operator(), cond.intValue(0));
    }

    private boolean compareInt(int actual, String operator, int threshold) {
        if (operator == null) return true;
        return switch (operator) {
            case "gte", ">=" -> actual >= threshold;
            case "lte", "<=" -> actual <= threshold;
            case "gt", ">" -> actual > threshold;
            case "lt", "<" -> actual < threshold;
            case "eq", "==" -> actual == threshold;
            default -> true;
        };
    }
}
