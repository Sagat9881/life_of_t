package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.engine.runtime.NpcUtilityBrain;
import ru.lifegame.backend.domain.engine.runtime.NpcUtilityBrain.ScoredResult;
import ru.lifegame.backend.domain.engine.spec.NpcSpec;

import java.util.Optional;

public class NpcLifecycleEngine {

    private final NpcRegistry registry;
    private final NpcUtilityBrain brain;

    public NpcLifecycleEngine(NpcRegistry registry, NpcUtilityBrain brain) {
        this.registry = registry;
        this.brain = brain;
    }

    public void hourlyTick(int currentHour, int currentDay) {
        for (NpcInstance npc : registry.all()) {
            updateScheduleActivity(npc, currentHour);
            Optional<ScoredResult> action = brain.evaluate(npc, currentHour, currentDay);
            action.ifPresent(a -> {
                // NPC decided to initiate — store as pending event
            });
        }
    }

    public void dailyTick(int currentDay) {
        for (NpcInstance npc : registry.all()) {
            npc.mood().dailyDecay();
            if (npc.spec().memoryEnabled()) {
                npc.memory().onDayEnd();
            }
        }
    }

    private void updateScheduleActivity(NpcInstance npc, int currentHour) {
        for (NpcSpec.ScheduleSlot slot : npc.spec().schedule()) {
            if (currentHour >= slot.start() && currentHour < slot.end()) {
                npc.updateActivity(slot.activity(), slot.location(), slot.animation());
                return;
            }
        }
        npc.updateActivity("idle", "home", "idle");
    }
}
