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

    public List<NpcActionResult> hourlyTick(int currentHour, Map<String, Object> context) {
        List<NpcActionResult> results = new ArrayList<>();
        for (NpcInstance npc : registry.all()) {
            var scheduled = npc.getScheduledActivity(currentHour);
            if (scheduled.isPresent()) {
                npc.setCurrentActivity(scheduled.get().activity());
                npc.setCurrentLocation(scheduled.get().location());
                results.add(new NpcActionResult(npc.spec().id(), scheduled.get().activity(), scheduled.get().location(), "schedule"));
            } else {
                var best = brain.evaluate(npc, context);
                if (best.isPresent()) {
                    EvaluatedAction action = best.get();
                    results.add(new NpcActionResult(npc.spec().id(), action.actionId(), null, "utility"));
                }
            }
        }
        return results;
    }

    public void dailyTick() {
        for (NpcInstance npc : registry.all()) {
            npc.mood().dailyDecay();
        }
    }

    public record NpcActionResult(String npcId, String activity, String location, String source) {}
}
