package ru.lifegame.backend.domain.npc.runtime;

import ru.lifegame.backend.domain.event.domain.DomainEvent;
import ru.lifegame.backend.domain.event.domain.NpcActivityChangedEvent;
import ru.lifegame.backend.domain.event.domain.NpcMoodExtremeEvent;
import ru.lifegame.backend.domain.npc.NpcUtilityBrain;

import java.util.*;

public class NpcLifecycleEngine {

    private final NpcRegistry registry;
    private final NpcUtilityBrain brain;

    public NpcLifecycleEngine(NpcRegistry registry, NpcUtilityBrain brain) {
        this.registry = registry;
        this.brain = brain;
    }

    /**
     * Called every game hour (after each player action).
     * Returns domain events for activity changes and mood extremes.
     */
    public List<DomainEvent> hourlyTick(String sessionId, int currentHour, Map<String, Object> context) {
        List<DomainEvent> events = new ArrayList<>();

        for (NpcInstance npc : registry.getAll()) {
            String oldActivityId = npc.currentActivity().activityId();
            String oldLocationId = npc.currentActivity().locationId();

            // 1. Update schedule-based activity
            npc.updateScheduleActivity(currentHour);

            // 2. Check mood override (extreme mood can change activity)
            npc.checkMoodOverride(currentHour);

            // 3. Brain evaluation (utility AI can override further)
            Optional<NpcUtilityBrain.ScoredCandidate> best = brain.evaluate(npc, context);
            best.ifPresent(candidate -> {
                if (candidate.score() > 0.5) {
                    npc.setCurrentActivity(
                            candidate.actionId(),
                            candidate.animationKey(),
                            candidate.locationId()
                    );
                }
            });

            // Emit activity changed event if activity or location changed
            String newActivityId = npc.currentActivity().activityId();
            String newLocationId = npc.currentActivity().locationId();
            if (!oldActivityId.equals(newActivityId) || !oldLocationId.equals(newLocationId)) {
                events.add(new NpcActivityChangedEvent(
                        sessionId, npc.id(), oldActivityId, newActivityId, newLocationId
                ));
            }

            // Emit mood extreme event if any axis is in extreme state
            if (npc.mood().hasExtremeState()) {
                String dominant = npc.mood().dominantAxis();
                int dominantValue = resolveAxisValue(npc.mood(), dominant);
                events.add(new NpcMoodExtremeEvent(sessionId, npc.id(), dominant, dominantValue));
            }
        }

        return events;
    }

    /**
     * Called at end of day. Applies daily decay and returns mood extreme events.
     */
    public List<DomainEvent> dailyTick(String sessionId, Map<String, Object> context) {
        List<DomainEvent> events = new ArrayList<>();

        for (NpcInstance npc : registry.getAll()) {
            npc.getMood().dailyTick();

            // After decay, check if mood is still extreme
            if (npc.mood().hasExtremeState()) {
                String dominant = npc.mood().dominantAxis();
                int dominantValue = resolveAxisValue(npc.mood(), dominant);
                events.add(new NpcMoodExtremeEvent(sessionId, npc.id(), dominant, dominantValue));
            }
        }

        return events;
    }

    /**
     * Resolves the integer value of the named mood axis.
     * NpcMood exposes individual getters, not a generic getAxis(String) method.
     */
    private static int resolveAxisValue(NpcMood mood, String axis) {
        return switch (axis) {
            case "happiness" -> mood.happiness();
            case "energy"    -> mood.energy();
            case "stress"    -> mood.stress();
            case "trust"     -> mood.trust();
            case "romance"   -> mood.romance();
            case "anger"     -> mood.anger();
            default          -> 0;
        };
    }

    public NpcRegistry getRegistry() {
        return registry;
    }
}
