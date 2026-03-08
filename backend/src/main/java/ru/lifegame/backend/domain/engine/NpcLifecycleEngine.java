package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.engine.runtime.NpcUtilityBrain;
import ru.lifegame.backend.domain.engine.runtime.NpcUtilityBrain.ScoredResult;

import java.util.*;

public class NpcLifecycleEngine {

    private final NpcRegistry registry;
    private final NpcUtilityBrain brain;

    public NpcLifecycleEngine(NpcRegistry registry, NpcUtilityBrain brain) {
        this.registry = registry;
        this.brain = brain;
    }

    public void hourlyTick(int currentHour, Map<String, Object> gameContext) {
        for (NpcInstance npc : registry.getAll()) {
            Optional<ScoredResult> best = brain.evaluate(npc, currentHour, gameContext);
            best.ifPresent(result -> npc.setCurrentActivity(
                result.actionId(), result.animation(), result.location()
            ));
        }
    }

    public void dailyTick(Map<String, Object> gameContext) {
        for (NpcInstance npc : registry.getAll()) {
            npc.getMood().dailyTick();
            npc.getMemory().onDayEnd();
        }
    }

    public NpcRegistry getRegistry() {
        return registry;
    }
}
