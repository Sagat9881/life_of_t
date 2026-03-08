package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.engine.runtime.NpcUtilityBrain;
import ru.lifegame.backend.domain.engine.runtime.NpcUtilityBrain.EvaluatedAction;

import java.util.*;

public class NpcLifecycleEngine {

    private final NpcRegistry registry;
    private final NpcUtilityBrain brain;

    public NpcLifecycleEngine(NpcRegistry registry, NpcUtilityBrain brain) {
        this.registry = registry;
        this.brain = brain;
    }

    public void hourlyTick(int currentHour, Map<String, Object> gameContext) {
        for (NpcInstance npc : registry.all()) {
            Optional<EvaluatedAction> best = brain.evaluate(npc, currentHour, gameContext);
            best.ifPresent(action -> {
                npc.setCurrentActivity(action.actionId());
                npc.setCurrentAnimation(action.animation());
                npc.setCurrentLocation(action.location());
            });
        }
    }

    public void dailyTick(Map<String, Object> gameContext) {
        for (NpcInstance npc : registry.all()) {
            npc.mood().dailyDecay();
        }
    }

    public NpcRegistry registry() {
        return registry;
    }
}
