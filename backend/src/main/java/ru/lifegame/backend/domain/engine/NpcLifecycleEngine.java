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

    public List<NpcAction> hourlyTick(int currentHour, Map<String, Object> context) {
        List<NpcAction> actions = new ArrayList<>();
        for (NpcInstance npc : registry.all()) {
            var scheduled = npc.getScheduledActivity(currentHour);
            if (scheduled.isPresent()) {
                var slot = scheduled.get();
                npc.setCurrentActivity(slot.activity());
                npc.setCurrentLocation(slot.location());
                npc.setCurrentAnimation(slot.animation());
                actions.add(new NpcAction(npc.spec().id(), slot.activity(), slot.location(), slot.animation(), "schedule"));
            } else {
                Optional<EvaluatedAction> best = brain.evaluate(npc, context);
                best.ifPresent(a -> {
                    npc.setCurrentActivity(a.actionId());
                    actions.add(new NpcAction(npc.spec().id(), a.actionId(), npc.currentLocation(), "idle", "utility_ai"));
                });
            }
        }
        return actions;
    }

    public void dailyTick(Map<String, Object> context) {
        for (NpcInstance npc : registry.all()) {
            npc.mood().dailyDecay();
        }
    }

    public record NpcAction(String npcId, String activity, String location, String animation, String source) {}
}
