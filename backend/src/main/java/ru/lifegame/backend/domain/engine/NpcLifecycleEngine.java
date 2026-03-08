package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.engine.runtime.NpcUtilityBrain;
import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.npc.engine.NpcMood;

public class NpcLifecycleEngine {

    private final NpcRegistry registry;
    private final NpcUtilityBrain utilityBrain;

    public NpcLifecycleEngine(NpcRegistry registry, NpcUtilityBrain utilityBrain) {
        this.registry = registry;
        this.utilityBrain = utilityBrain;
    }

    public void hourlyTick(int currentHour, Object gameContext) {
        for (NpcInstance npc : registry.all()) {
            var scheduled = npc.spec().schedule().stream()
                .filter(s -> currentHour >= s.start() && currentHour < s.end())
                .findFirst();

            if (scheduled.isPresent()) {
                var slot = scheduled.get();
                npc.updateActivity(slot.activity(), slot.location(), slot.animation());
            } else {
                var candidate = utilityBrain.evaluate(npc, gameContext);
                candidate.ifPresent(c -> npc.updateActivity(c.action().id(), "dynamic", "idle"));
            }
        }
    }

    public void dailyTick() {
        for (NpcInstance npc : registry.all()) {
            NpcMood mood = npc.mood();
            npc.setMood(mood.dailyDecay());
        }
    }
}
