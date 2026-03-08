package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.engine.runtime.NpcUtilityBrain;
import ru.lifegame.backend.domain.engine.runtime.NpcUtilityBrain.ScoredResult;

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
                actions.add(new NpcAction(npc.id(), scheduled.get().activity(),
                        scheduled.get().location(), scheduled.get().animation(), "schedule"));
            } else {
                Optional<ScoredResult> best = brain.evaluate(npc, context);
                best.ifPresent(r -> actions.add(new NpcAction(
                        npc.id(), r.actionId(), null, null, "utility")));
            }
        }
        return actions;
    }

    public void dailyTick(Map<String, Object> context) {
        for (NpcInstance npc : registry.all()) {
            NpcInstance updated = npc.dailyMoodDecay();
            registry.update(npc.id(), updated);
        }
    }

    public record NpcAction(String npcId, String activity, String location, String animation, String source) {}
}
