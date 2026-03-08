package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.engine.runtime.NpcUtilityBrain;
import ru.lifegame.backend.domain.engine.runtime.NpcUtilityBrain.EvaluatedAction;

import java.util.*;

public class NpcLifecycleEngine {

    private final NpcUtilityBrain brain;

    public NpcLifecycleEngine(NpcUtilityBrain brain) {
        this.brain = brain;
    }

    public List<NpcAction> hourlyTick(NpcRegistry registry, int currentHour) {
        List<NpcAction> actions = new ArrayList<>();
        for (NpcInstance npc : registry.all()) {
            var scheduled = npc.getScheduledActivity(currentHour);
            if (scheduled.isPresent()) {
                npc.setCurrentActivity(scheduled.get().activity());
                npc.setCurrentLocation(scheduled.get().location());
                actions.add(new NpcAction(npc.spec().id(), scheduled.get().activity(),
                        scheduled.get().location(), scheduled.get().animation(), "schedule"));
            } else {
                Optional<EvaluatedAction> best = brain.evaluate(npc);
                if (best.isPresent()) {
                    npc.setCurrentActivity(best.get().actionId());
                    actions.add(new NpcAction(npc.spec().id(), best.get().actionId(),
                            npc.currentLocation(), null, "utility_ai"));
                }
            }
        }
        return actions;
    }

    public void dailyTick(NpcRegistry registry) {
        for (NpcInstance npc : registry.all()) {
            npc.mood().dailyDecay();
            npc.memory().onDayEnd();
        }
    }

    public record NpcAction(String npcId, String activity, String location,
                            String animation, String source) {}
}
