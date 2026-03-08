package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.engine.runtime.NpcUtilityBrain;
import ru.lifegame.backend.domain.engine.runtime.NpcUtilityBrain.EvaluatedAction;

import java.util.ArrayList;
import java.util.List;

public class NpcLifecycleEngine {

    private final NpcRegistry registry;
    private final NpcUtilityBrain brain;

    public NpcLifecycleEngine(NpcRegistry registry, NpcUtilityBrain brain) {
        this.registry = registry;
        this.brain = brain;
    }

    public void hourlyTick(int currentHour) {
        for (NpcInstance npc : registry.getAll()) {
            var scheduled = npc.getScheduledActivity(currentHour);
            if (scheduled.isPresent()) {
                npc.setCurrentActivity(scheduled.get().activity());
                npc.setCurrentLocation(scheduled.get().location());
                npc.setCurrentAnimation(scheduled.get().animation());
            }
        }
    }

    public List<EvaluatedAction> dailyTick() {
        List<EvaluatedAction> initiatedActions = new ArrayList<>();
        for (NpcInstance npc : registry.getNamed()) {
            npc.mood().dailyDecay();
            var best = brain.evaluate(npc);
            if (best.isPresent() && best.get().score() > 0.5) {
                initiatedActions.add(best.get());
            }
        }
        return initiatedActions;
    }
}
