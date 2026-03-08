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
            if (scheduled != null) {
                npc.setCurrentActivity(scheduled.activity());
                npc.setCurrentLocation(scheduled.location());
                npc.setCurrentAnimation(scheduled.animation());
                actions.add(new NpcAction(npc.spec().id(), scheduled.activity(), scheduled.location(), scheduled.animation(), "schedule"));
            } else {
                Optional<EvaluatedAction> best = brain.evaluate(npc, context);
                best.ifPresent(action -> {
                    npc.setCurrentActivity(action.actionId());
                    actions.add(new NpcAction(npc.spec().id(), action.actionId(), npc.currentLocation(), null, "utility_ai"));
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
