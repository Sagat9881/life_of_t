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

    public List<EvaluatedAction> hourlyTick(int currentHour) {
        List<EvaluatedAction> initiatedActions = new ArrayList<>();
        for (NpcInstance npc : registry.allInstances()) {
            npc.updateScheduledActivity(currentHour);
            var best = brain.evaluate(npc);
            if (best != null && best.score() > 0.5) {
                initiatedActions.add(best);
            }
        }
        return initiatedActions;
    }

    public void dailyTick() {
        for (NpcInstance npc : registry.allInstances()) {
            npc.mood().dailyDecay();
        }
    }

    public NpcRegistry registry() {
        return registry;
    }
}
