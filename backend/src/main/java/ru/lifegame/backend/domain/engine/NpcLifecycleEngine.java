package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.engine.runtime.NpcUtilityBrain;
import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.npc.engine.NpcMood;

import java.util.List;

public class NpcLifecycleEngine {

    private final NpcUtilityBrain brain;

    public NpcLifecycleEngine(NpcUtilityBrain brain) {
        this.brain = brain;
    }

    public void hourlyTick(NpcRegistry registry, int currentHour) {
        for (var npc : registry.all()) {
            updateActivityFromSchedule(npc, currentHour);
            var candidate = brain.evaluate(npc);
            candidate.ifPresent(c -> {
                // NPC-initiated event candidate — will be processed by event engine
            });
        }
    }

    public void dailyTick(NpcRegistry registry) {
        for (var npc : registry.all()) {
            NpcMood current = npc.mood();
            NpcMood decayed = current.dailyTick();
            npc.setMood(decayed);
            if (npc.spec().memoryEnabled()) {
                npc.memory().onDayEnd();
            }
        }
    }

    private void updateActivityFromSchedule(NpcInstance npc, int currentHour) {
        for (var slot : npc.spec().schedule()) {
            if (currentHour >= slot.start() && currentHour < slot.end()) {
                npc.updateActivity(slot.activity(), slot.location(), slot.animation());
                return;
            }
        }
        npc.updateActivity("idle", "unknown", "idle");
    }
}
