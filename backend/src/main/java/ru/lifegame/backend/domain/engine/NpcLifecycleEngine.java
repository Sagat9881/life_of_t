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

    public List<NpcAction> hourlyTick(int currentHour, Map<String, Object> gameContext) {
        List<NpcAction> actions = new ArrayList<>();
        for (NpcInstance npc : registry.all()) {
            var scheduleSlot = npc.spec().schedule().stream()
                    .filter(s -> currentHour >= s.startHour() && currentHour < s.endHour())
                    .findFirst();

            if (scheduleSlot.isPresent()) {
                var slot = scheduleSlot.get();
                npc.setCurrentActivity(slot.activity());
                npc.setCurrentLocation(slot.location());
                npc.setCurrentAnimation(slot.animation());
                actions.add(new NpcAction(npc.spec().id(), slot.activity(), slot.location(), slot.animation(), false));
            }
        }
        return actions;
    }

    public List<NpcAction> dailyTick(Map<String, Object> gameContext) {
        List<NpcAction> initiatedActions = new ArrayList<>();
        for (NpcInstance npc : registry.all()) {
            npc.mood().dailyDecay();
            if ("named".equals(npc.spec().type())) {
                Optional<EvaluatedAction> best = brain.evaluateBest(npc, gameContext);
                best.ifPresent(a -> initiatedActions.add(
                        new NpcAction(npc.spec().id(), a.actionId(), "", "", true)
                ));
            }
        }
        return initiatedActions;
    }

    public record NpcAction(String npcId, String activity, String location, String animation, boolean initiated) {}
}
