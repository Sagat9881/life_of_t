package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.engine.runtime.NpcUtilityBrain;
import ru.lifegame.backend.domain.engine.runtime.NpcUtilityBrain.ScoredResult;

import java.util.*;

public class NpcLifecycleEngine {

    private final NpcUtilityBrain utilityBrain;

    public NpcLifecycleEngine(NpcRegistry registry, NpcUtilityBrain utilityBrain) {
        this.utilityBrain = utilityBrain;
    }

    public record NpcActivityUpdate(String npcId, String activityId, String animation, String location) {}

    public List<NpcActivityUpdate> hourlyTick(NpcRegistry registry, int currentHour, Map<String, Object> context) {
        List<NpcActivityUpdate> updates = new ArrayList<>();
        for (NpcInstance npc : registry.all()) {
            var scheduled = npc.getScheduledActivity(currentHour);
            if (scheduled.isPresent()) {
                var slot = scheduled.get();
                npc.setCurrentActivity(slot.activity());
                npc.setCurrentLocation(slot.location());
                updates.add(new NpcActivityUpdate(npc.spec().id(), slot.activity(), slot.animation(), slot.location()));
            } else {
                var best = utilityBrain.evaluate(npc, context);
                if (best.isPresent()) {
                    ScoredResult result = best.get();
                    npc.setCurrentActivity(result.actionId());
                    updates.add(new NpcActivityUpdate(npc.spec().id(), result.actionId(), result.animation(), result.location()));
                }
            }
        }
        return updates;
    }

    public void dailyTick(NpcRegistry registry) {
        for (NpcInstance npc : registry.all()) {
            npc.mood().dailyDecay();
        }
    }
}
