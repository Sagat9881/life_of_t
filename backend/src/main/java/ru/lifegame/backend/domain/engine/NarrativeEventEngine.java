package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.spec.EventSpec;
import ru.lifegame.backend.domain.engine.spec.ConditionSpec;

import java.util.*;

public class NarrativeEventEngine {

    private final List<EventSpec> eventSpecs;
    private final Map<String, Integer> lastFiredDay = new HashMap<>();

    public NarrativeEventEngine(List<EventSpec> eventSpecs) {
        this.eventSpecs = eventSpecs;
    }

    public record FiredEvent(EventSpec spec, EventSpec.EffectSpec chosenEffect) {}

    public List<EventSpec> getEligibleEvents(int currentDay, Object gameContext) {
        return eventSpecs.stream()
            .filter(e -> isEligible(e, currentDay))
            .sorted(Comparator.comparingInt(e -> -e.meta().priority()))
            .toList();
    }

    private boolean isEligible(EventSpec event, int currentDay) {
        if (!event.meta().repeatable() && lastFiredDay.containsKey(event.id())) return false;
        if (event.meta().cooldownDays() > 0) {
            Integer lastDay = lastFiredDay.get(event.id());
            if (lastDay != null && (currentDay - lastDay) < event.meta().cooldownDays()) return false;
        }
        return true;
    }

    public void markFired(String eventId, int day) {
        lastFiredDay.put(eventId, day);
    }

    private boolean evaluateCondition(ConditionSpec condition, Object gameContext) {
        return true;
    }
}
