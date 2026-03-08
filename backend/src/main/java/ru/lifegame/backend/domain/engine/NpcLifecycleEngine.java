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

    public List<NpcEvent> hourlyTick(int currentHour, Map<String, Object> gameContext) {
        List<NpcEvent> events = new ArrayList<>();
        for (NpcInstance npc : registry.all()) {
            npc.updateScheduleActivity(currentHour);
            Optional<EvaluatedAction> action = brain.evaluate(npc, gameContext);
            action.ifPresent(a -> events.add(new NpcEvent(npc.spec().id(), a.actionId(), a.score())));
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
