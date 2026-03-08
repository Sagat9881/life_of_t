package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.engine.runtime.NpcUtilityBrain;
import ru.lifegame.backend.domain.engine.runtime.NpcUtilityBrain.EvaluatedAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NpcLifecycleEngine {

    private final NpcRegistry registry;
    private final NpcUtilityBrain brain;

    public NpcLifecycleEngine(NpcRegistry registry, NpcUtilityBrain brain) {
        this.registry = registry;
        this.brain = brain;
    }

    public List<NpcEvent> hourlyTick(int currentHour, int currentDay) {
        List<NpcEvent> events = new ArrayList<>();
        for (NpcInstance npc : registry.allNamed()) {
            Optional<EvaluatedAction> best = brain.evaluate(npc, currentHour, currentDay);
            best.ifPresent(action -> {
                npc.setCurrentActivity(action.actionId());
                if (action.generatesEvent()) {
                    events.add(new NpcEvent(npc.spec().id(), action.actionId(), action.score()));
                }
            });
        }
        return events;
    }

    public void dailyTick() {
        registry.dailyTick();
    }

    public record NpcEvent(String npcId, String actionId, double score) {}
}
