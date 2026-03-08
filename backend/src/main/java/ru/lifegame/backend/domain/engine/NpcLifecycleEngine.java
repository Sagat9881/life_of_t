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
        for (NpcInstance npc : registry.all()) {
            // Update schedule-based activity
            npc.spec().schedule().stream()
                    .filter(slot -> currentHour >= slot.startHour() && currentHour < slot.endHour())
                    .findFirst()
                    .ifPresent(slot -> npc.setCurrentActivity(slot.activity(), slot.location(), slot.animation()));

            // For named NPCs, run utility brain to check for mood-override actions
            if ("named".equals(npc.spec().type())) {
                Optional<ScoredResult> best = brain.evaluate(npc, gameContext);
                best.ifPresent(result -> {
                    if (result.score() > 0.7) {
                        npc.setCurrentActivity(result.actionId(), "dynamic", "dynamic");
                    }
                });
            }
        }
    }

    public void dailyTick(Map<String, Object> gameContext) {
        for (NpcInstance npc : registry.all()) {
            npc.mood().dailyDecay();
        }
    }

    public NpcRegistry getRegistry() {
        return registry;
    }
}
