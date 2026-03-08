package ru.lifegame.backend.domain.npc.runtime;

import ru.lifegame.backend.domain.npc.NpcUtilityBrain;

import java.util.*;

public class NpcLifecycleEngine {

    private final NpcRegistry registry;
    private final NpcUtilityBrain brain;

    public NpcLifecycleEngine(NpcRegistry registry, NpcUtilityBrain brain) {
        this.registry = registry;
        this.brain = brain;
    }

    public void hourlyTick(int currentHour, Map<String, Object> context) {
        for (NpcInstance npc : registry.getAll()) {
            npc.updateScheduleActivity(currentHour);
            Optional<NpcUtilityBrain.ScoredCandidate> best = brain.evaluate(npc, context);
            best.ifPresent(candidate -> {
                if (candidate.score() > 0.5) {
                    npc.setCurrentActivity(candidate.actionId(), candidate.actionId(), "default");
                }
            });
        }
    }

    public void dailyTick(Map<String, Object> context) {
        for (NpcInstance npc : registry.getAll()) {
            npc.getMood().dailyTick();
        }
    }

    public NpcRegistry getRegistry() {
        return registry;
    }
}
