package ru.lifegame.backend.domain.narrative;

import ru.lifegame.backend.domain.npc.spec.EventSpec;
import ru.lifegame.backend.domain.npc.spec.EventSpec.ConditionSpec;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Evaluates loaded {@link EventSpec} instances against the current game context
 * and determines which events fire on a given tick.
 *
 * Evaluation pipeline for each spec:
 *   1. Check all conditions against context (all must pass — AND logic)
 *   2. Check cooldown: event must not have fired within cooldownHours
 *   3. Probability roll: Random.nextDouble() &lt; spec.meta().probability()
 *   4. If all pass → return as fired
 *
 * Supported condition types:
 *   - time_of_day  : context key "timeSlot"       — values: morning/day/evening/night
 *   - stat_min     : context key = stat name      — int value, passes if context[stat] >= value
 *   - location     : context key "location"       — exact string match
 *   - trigger      : context key "activeTrigger"  — exact string match
 *   - weather      : context key "weather"        — exact string match
 *   - season       : context key "season"         — exact string match
 */
public class NarrativeEventEngine {

    private final List<EventSpec> specs;
    private final Map<String, Instant> lastFired = new HashMap<>();
    private final Random random = new Random();

    public NarrativeEventEngine(List<EventSpec> specs) {
        this.specs = Collections.unmodifiableList(new ArrayList<>(specs));
    }

    /**
     * Evaluates all loaded specs against the given context.
     *
     * @param context map of game-state keys to string values
     *                (e.g. {"timeSlot": "night", "anxiety": "70", "location": "home_room"})
     * @return list of EventSpec instances that fire on this tick (may be empty)
     */
    public List<EventSpec> evaluate(Map<String, String> context) {
        List<EventSpec> fired = new ArrayList<>();
        Instant now = Instant.now();

        for (EventSpec spec : specs) {
            if (!conditionsMet(spec.conditions(), context)) continue;
            if (!cooldownExpired(spec.id(), spec.meta().cooldownHours(), now)) continue;
            if (random.nextDouble() >= spec.meta().probability()) continue;

            lastFired.put(spec.id(), now);
            fired.add(spec);
        }
        return fired;
    }

    // ── condition evaluation ────────────────────────────────────────────────

    private boolean conditionsMet(List<ConditionSpec> conditions, Map<String, String> context) {
        for (ConditionSpec c : conditions) {
            if (!evaluate(c, context)) return false;
        }
        return true;
    }

    private boolean evaluate(ConditionSpec c, Map<String, String> context) {
        return switch (c.type()) {
            case "time_of_day" -> c.value().equals(context.get("timeSlot"));
            case "location"    -> c.value().equals(context.get("location"));
            case "weather"     -> c.value().equals(context.get("weather"));
            case "season"      -> c.value().equals(context.get("season"));
            case "trigger"     -> c.value().equals(context.get("activeTrigger"));
            case "stat_min"    -> evaluateStatMin(c, context);
            default -> {
                // Unknown condition type — log and skip (fail-open to avoid blocking new types)
                System.err.println("[NarrativeEventEngine] Unknown condition type: '" + c.type() + "' — skipping");
                yield true;
            }
        };
    }

    private boolean evaluateStatMin(ConditionSpec c, Map<String, String> context) {
        String statKey = c.stat();
        if (statKey == null || statKey.isBlank()) return false;
        String rawValue = context.get(statKey);
        if (rawValue == null) return false;
        try {
            return Integer.parseInt(rawValue) >= c.intValue();
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // ── cooldown ────────────────────────────────────────────────────────────

    private boolean cooldownExpired(String eventId, int cooldownHours, Instant now) {
        Instant last = lastFired.get(eventId);
        if (last == null) return true; // never fired
        return ChronoUnit.HOURS.between(last, now) >= cooldownHours;
    }

    /** Allows external code (e.g. tests, save/load) to reset or set the last-fired time. */
    public void setLastFired(String eventId, Instant when) {
        lastFired.put(eventId, when);
    }

    public Optional<Instant> getLastFired(String eventId) {
        return Optional.ofNullable(lastFired.get(eventId));
    }
}
