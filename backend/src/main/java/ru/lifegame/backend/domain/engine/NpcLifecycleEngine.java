package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.engine.runtime.NpcUtilityBrain;
import ru.lifegame.backend.domain.engine.runtime.NpcUtilityBrain.ScoredResult;

import java.util.ArrayList;
import java.util.List;

public class NpcLifecycleEngine {

    private final NpcRegistry registry;
    private final NpcUtilityBrain brain;

    public NpcLifecycleEngine(NpcRegistry registry, NpcUtilityBrain brain) {
        this.registry = registry;
        this.brain = brain;
    }

    public void hourlyTick(int currentHour) {
        for (NpcInstance npc : registry.allInstances()) {
            var scheduled = npc.getScheduledActivity(currentHour);
            if (scheduled != null) {
                npc.setCurrentActivity(scheduled.activity());
                npc.setCurrentLocation(scheduled.location());
                npc.setCurrentAnimation(scheduled.animation());
            }
        }
    }

    public List<NpcInitiatedEvent> dailyTick(int currentDay) {
        List<NpcInitiatedEvent> events = new ArrayList<>();
        for (NpcInstance npc : registry.namedInstances()) {
            npc.mood().dailyDecay();
            ScoredResult best = brain.evaluate(npc);
            if (best != null && best.score() > 0.5) {
                events.add(new NpcInitiatedEvent(npc.spec().id(), best.actionId(), best.score()));
            }
        }
        return events;
    }

    public NpcRegistry registry() {
        return registry;
    }
}
