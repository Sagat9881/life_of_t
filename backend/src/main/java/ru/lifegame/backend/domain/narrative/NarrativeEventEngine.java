package ru.lifegame.backend.domain.narrative;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lifegame.backend.domain.narrative.spec.EventSpec;
import ru.lifegame.backend.domain.narrative.spec.EventSpec.ConditionSpec;

import java.util.*;

/**
 * Evaluates {@link EventSpec} instances against the current game context
 * and determines which events fire on a given tick.
 *
 * Specs are NOT stored here — callers pass them on each {@link #evaluate} call.
 * This keeps GameContentService as the single source of truth for event data.
 *
 * Evaluation pipeline for each spec:
 *   1. Check all conditions against context (all must pass — AND logic)
 *   2. Check cooldown: event must not have fired within cooldownDays (game days)
 *   3. Probability roll: Random.nextDouble() &lt; spec.meta().probability()
 *   4. If all pass → return as fired
 *
 * Cooldown note: cooldownHours from XML is treated as cooldownDays in game time
 * (one game tick = one game day). This is an intentional simplification.
 *
 * Supported condition types:
 *   - time_of_day  : context key "timeSlot"       — values: morning/day/evening/night
 *   - stat_min     : context key = stat name      — int value, passes if context[stat] >= value
 *   - location     : context key "location"       — exact string match
 *   - trigger      : context key "activeTrigger"  — exact string match
 *   - weather      : context key "weather"        — exact string match
 *   - season       : context key "season"         — exact string match
 *
 * Context values are always Strings. Callers must convert numeric stats
 * via String.valueOf() before passing the context map.
 */
public class NarrativeEventEngine {

    private static final Logger log = LoggerFactory.getLogger(NarrativeEventEngine.class);

    private final Map<String, Integer> lastFired = new HashMap<>();
    private final Random random = new Random();

    public NarrativeEventEngine() {}

    /**
     * Evaluates all provided specs against the given context.
     *
     * @param specs          list of event specs to evaluate (from GameContentService)
     * @param context        map of game-state keys to String values.
     *                       Numeric stats must be pre-converted: String.valueOf(energy).
     *                       e.g. {"timeSlot": "night", "anxiety": "70", "location": "home_room"}
     * @param currentGameDay current in-game day number, used for cooldown tracking
     * @return list of EventSpec instances that fire on this tick (may be empty)
     */
    public List<EventSpec> evaluate(List<EventSpec> specs, Map<String, String> context, int currentGameDay) {
        List<EventSpec> fired = new ArrayList<>();

        for (EventSpec spec : specs) {
            if (!conditionsMet(spec.conditions(), context)) continue;
            if (!cooldownExpired(spec.id(), spec.meta().cooldownHours(), currentGameDay)) continue;
            if (random.nextDouble() >= spec.meta().probability()) continue;

            lastFired.put(spec.id(), currentGameDay);
            fired.add(spec);
        }
        return fired;
    }

    // ── condition evaluation ─────────────────────────────────────────────────────

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
                log.warn("[NarrativeEventEngine] Unknown condition type: '{}' — blocking event (fail-closed)", c.type());
                yield false;
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

    // ── cooldown ─────────────────────────────────────────────────────────────────

    private boolean cooldownExpired(String eventId, int cooldownDays, int currentGameDay) {
        Integer lastFiredDay = lastFired.get(eventId);
        if (lastFiredDay == null) return true;
        return (currentGameDay - lastFiredDay) >= cooldownDays;
    }

    /** Allows external code (e.g. tests, save/load) to reset or set the last-fired game day. */
    public void setLastFired(String eventId, int gameDay) {
        lastFired.put(eventId, gameDay);
    }

    public Optional<Integer> getLastFired(String eventId) {
        return Optional.ofNullable(lastFired.get(eventId));
    }
}
