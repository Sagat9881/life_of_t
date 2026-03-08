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

    public List<NpcActionResult> hourlyTick(int currentHour, Map<String, Object> gameContext) {
        List<NpcActionResult> results = new ArrayList<>();
        for (NpcInstance npc : registry.all()) {
            var scheduled = npc.getScheduledActivity(currentHour);
            if (scheduled.isPresent()) {
                npc.setCurrentActivity(scheduled.get().activity());
                npc.setCurrentLocation(scheduled.get().location());
                npc.setCurrentAnimation(scheduled.get().animation());
            } else {
                Optional<EvaluatedAction> best = brain.evaluate(npc, gameContext);
                best.ifPresent(action -> {
                    npc.setCurrentActivity(action.actionId());
                    results.add(new NpcActionResult(npc.spec().id(), action.actionId(), action.score()));
                });
            }
        }
        return results;
    }

    public void dailyTick() {
        for (NpcInstance npc : registry.all()) {
            npc.mood().dailyDecay();
        }
    }

    public record NpcActionResult(String npcId, String actionId, double score) {}
}
