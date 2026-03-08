package ru.lifegame.backend.application.engine;

import com.sagat.life_of_t.domain.engine.spec.EventSpec;
import com.sagat.life_of_t.domain.engine.spec.EventSpec.*;

import java.util.*;

/**
 * Evaluates event conditions against game state and fires matching events.
 * Completely data-driven — conditions are evaluated generically from XML specs.
 */
public class NarrativeEventEngine {

    private final List<EventSpec> eventSpecs;
    private final Map<String, Integer> cooldowns = new HashMap<>();

    public NarrativeEventEngine(List<EventSpec> eventSpecs) {
        this.eventSpecs = eventSpecs;
    }

    public record GameStateSnapshot(
            String timeOfDay,
            String playerLocation,
            int gameDay,
            int gameHour,
            Map<String, Integer> stats,
            Map<String, Integer> relationships
    ) {}

    public record FiredEvent(
            EventSpec spec,
            List<EffectSpec> applicableEffects
    ) {}

    public List<FiredEvent> evaluate(GameStateSnapshot state) {
        List<FiredEvent> fired = new ArrayList<>();

        for (EventSpec event : eventSpecs) {
            if (isOnCooldown(event.id(), state.gameHour())) continue;
            if (!passesAllConditions(event.conditions(), state)) continue;
            if (!passesProbability(event.meta().probability())) continue;

            List<EffectSpec> effects = event.effects().stream()
                    .filter(e -> e.choice() == null)
                    .toList();

            fired.add(new FiredEvent(event, effects));
            cooldowns.put(event.id(), state.gameHour() + event.meta().cooldownHours());
        }

        return fired;
    }

    private boolean passesAllConditions(List<ConditionSpec> conditions, GameStateSnapshot state) {
        for (ConditionSpec cond : conditions) {
            if (!evaluateCondition(cond, state)) return false;
        }
        return true;
    }

    private boolean evaluateCondition(ConditionSpec cond, GameStateSnapshot state) {
        return switch (cond.type()) {
            case "time_of_day" -> cond.value().equals(state.timeOfDay());
            case "location" -> cond.value().equals(state.playerLocation());
            case "stat_min" -> {
                String stat = cond.attributes().get("stat");
                int min = Integer.parseInt(cond.value());
                yield state.stats().getOrDefault(stat, 0) >= min;
            }
            case "stat_max" -> {
                String stat = cond.attributes().get("stat");
                int max = Integer.parseInt(cond.value());
                yield state.stats().getOrDefault(stat, 0) <= max;
            }
            case "relationship_min" -> {
                String target = cond.attributes().get("target");
                int min = Integer.parseInt(cond.value());
                yield state.relationships().getOrDefault(target, 0) >= min;
            }
            case "day_min" -> state.gameDay() >= Integer.parseInt(cond.value());
            case "day_max" -> state.gameDay() <= Integer.parseInt(cond.value());
            default -> true;
        };
    }

    private boolean isOnCooldown(String eventId, int currentHour) {
        return cooldowns.getOrDefault(eventId, 0) > currentHour;
    }

    private boolean passesProbability(double probability) {
        return Math.random() < probability;
    }
}
