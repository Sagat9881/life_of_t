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
                npc.setCurrentActivity(scheduled.get().activity());
                npc.setCurrentLocation(scheduled.get().location());
                actions.add(new NpcAction(npc.spec().id(), scheduled.get().activity(),
                        scheduled.get().location(), scheduled.get().animation(), "schedule"));
            } else {
                var best = brain.evaluateBest(npc, context);
                if (best.isPresent()) {
                    EvaluatedAction action = best.get();
                    npc.setCurrentActivity(action.actionId());
                    actions.add(new NpcAction(npc.spec().id(), action.actionId(),
                            npc.currentLocation(), null, "utility_ai"));
                }
            }
        }
        return actions;
    }

    public void dailyTick() {
        for (NpcInstance npc : registry.all()) {
            npc.mood().dailyDecay();
        }
    }

    public record NpcAction(String npcId, String activity, String location,
                            String animation, String source) {}
}
