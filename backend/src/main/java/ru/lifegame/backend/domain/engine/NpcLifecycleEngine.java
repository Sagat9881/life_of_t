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

    public List<NpcEvent> hourlyTick(int currentHour) {
        List<NpcEvent> events = new ArrayList<>();
        for (NpcInstance npc : registry.all()) {
            npc.updateScheduleActivity(currentHour);
            EvaluatedAction best = brain.evaluate(npc);
            if (best != null && best.score() > 0.5) {
                events.add(new NpcEvent(npc.spec().id(), best.actionId(), best.score()));
            }
        }
        return events;
    }

    public void dailyTick() {
        for (NpcInstance npc : registry.all()) {
            npc.mood().dailyDecay();
        }
    }

    public record NpcEvent(String npcId, String actionId, double score) {}
}
