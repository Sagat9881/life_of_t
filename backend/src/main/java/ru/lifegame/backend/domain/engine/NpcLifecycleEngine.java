package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.engine.runtime.NpcUtilityBrain;
import ru.lifegame.backend.domain.engine.runtime.NpcUtilityBrain.EvaluatedAction;

import java.util.*;

public class NpcLifecycleEngine {

    private final NpcUtilityBrain brain;

    public NpcLifecycleEngine(NpcUtilityBrain brain) {
        this.brain = brain;
    }

    public void hourlyTick(NpcRegistry registry, int currentHour) {
        for (NpcInstance npc : registry.all()) {
            Optional<EvaluatedAction> best = brain.evaluate(npc, currentHour);
            best.ifPresent(action -> {
                npc.setCurrentActivity(action.actionId());
                npc.setCurrentAnimation(action.animation());
                npc.setCurrentLocation(action.location());
            });
        }
    }

    public void dailyTick(NpcRegistry registry) {
        for (NpcInstance npc : registry.all()) {
            npc.mood().dailyDecay();
        }
    }
}
