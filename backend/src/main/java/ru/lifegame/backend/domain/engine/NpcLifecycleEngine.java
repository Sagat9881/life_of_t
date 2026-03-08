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

    public void hourlyTick(int currentHour, Map<String, Object> context) {
        for (NpcInstance npc : registry.allInstances()) {
            npc.updateScheduledActivity(currentHour);
            if ("named".equals(npc.spec().type())) {
                Optional<EvaluatedAction> action = brain.evaluateBest(npc, context);
                action.ifPresent(a -> npc.setCurrentActivity(a.actionId(), a.animation(), a.location()));
            }
        }
    }

    public void dailyTick(Map<String, Object> context) {
        for (NpcInstance npc : registry.allInstances()) {
            npc.mood().dailyDecay();
        }
    }

    public NpcRegistry registry() { return registry; }
}
