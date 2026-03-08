package ru.lifegame.backend.domain.npc;

import ru.lifegame.backend.domain.event.game.*;
import ru.lifegame.backend.domain.model.stats.StatChanges;

import java.util.*;

/**
 * Generic Utility AI brain for any NPC.
 * Evaluates all ScoredActions from the NPC's spec, picks the highest-scoring one.
 * Zero hardcoded NPC names or action IDs — purely data-driven.
 */
public class NpcUtilityBrain {
    private static final double INITIATION_THRESHOLD = 0.5;

    private final ConditionEvaluator conditionEvaluator;
    private final Set<String> firedToday = new HashSet<>();

    public NpcUtilityBrain() {
        this.conditionEvaluator = new ConditionEvaluator();
    }

    /**
     * Evaluate all candidate actions for a given NPC and return the best one (if any).
     */
    public Optional<GameEvent> evaluate(NpcInstance npc, int currentDay, int currentHour,
                                         ConditionEvaluator.StatAccessor stats) {
        if (npc.spec().actions().isEmpty()) return Optional.empty();

        return npc.spec().actions().stream()
            .filter(action -> !firedToday.contains(npc.id() + ":" + action.actionId()))
            .filter(action -> conditionEvaluator.allMet(
                action.conditions(), npc, currentDay, currentHour, stats))
            .map(action -> Map.entry(action, score(action, npc)))
            .filter(e -> e.getValue() > INITIATION_THRESHOLD)
            .max(Comparator.comparingDouble(Map.Entry::getValue))
            .map(e -> {
                ScoredAction action = e.getKey();
                firedToday.add(npc.id() + ":" + action.actionId());
                return toGameEvent(action, npc, currentDay);
            });
    }

    /**
     * Score an action based on base score + mood urgency + personality modifiers.
     */
    private double score(ScoredAction action, NpcInstance npc) {
        double moodFactor = npc.mood().urgencyScore() / 50.0;
        double personalityFactor = 0.0;

        // Personality traits can boost or reduce action scores
        Map<String, Integer> traits = npc.spec().personalityTraits();
        if (traits.containsKey("patience")) {
            // High patience reduces urgency-driven actions
            personalityFactor -= (traits.get("patience") - 50) / 100.0;
        }
        if (traits.containsKey("warmth")) {
            // High warmth boosts social actions
            personalityFactor += (traits.get("warmth") - 50) / 200.0;
        }

        return action.baseScore() + moodFactor + personalityFactor;
    }

    /**
     * Convert a ScoredAction into a GameEvent that the existing event system can handle.
     */
    private GameEvent toGameEvent(ScoredAction action, NpcInstance npc, int currentDay) {
        List<EventOption> eventOptions = action.options().stream()
            .map(opt -> new EventOption(
                opt.id(),
                opt.text(),
                opt.resultText(),
                new StatChanges(
                    opt.energy(), opt.jobSatisfaction(),
                    opt.stress(), opt.mood(),
                    opt.money(), opt.selfEsteem()
                ),
                opt.relationshipDeltas()
            ))
            .toList();

        GameEventType eventType;
        try {
            eventType = GameEventType.valueOf(action.eventType());
        } catch (IllegalArgumentException e) {
            eventType = GameEventType.RANDOM_ENCOUNTER;
        }

        return new GameEvent(
            "npc_" + npc.id() + "_" + action.actionId(),
            eventType,
            action.title(),
            action.description(),
            eventOptions,
            Map.of("npcId", npc.id(), "npcName", npc.spec().displayName()),
            5,
            currentDay
        );
    }

    public void resetDaily() {
        firedToday.clear();
    }
}
