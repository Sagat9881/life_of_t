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
        for (NpcInstance npc : registry.allNpcs()) {
            npc.updateScheduledActivity(currentHour);
            Optional<EvaluatedAction> action = brain.evaluate(npc, context);
            action.ifPresent(a -> {
                npc.setCurrentActivity(a.actionId(), a.animation(), a.location());
                results.add(new NpcActionResult(npc.spec().id(), a.actionId(), a.score()));
            });
        }
        return results;
    }

    public void dailyTick() {
        for (NpcInstance npc : registry.allNpcs()) {
            npc.mood().dailyDecay();
            npc.memory().trimOldEntries();
        }
    }

    public record NpcActionResult(String npcId, String actionId, double score) {}
}
