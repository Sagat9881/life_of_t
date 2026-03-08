package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipGraph;
import ru.lifegame.backend.domain.npc.graph.NpcRelationshipEdge;
import ru.lifegame.backend.domain.npc.spec.ConditionSpec;
import ru.lifegame.backend.domain.npc.engine.ConditionEvaluator;

import java.util.*;

public class CrossNpcTriggerEngine {

    public record CrossNpcTrigger(
        String id, String npcA, String npcB,
        List<ConditionSpec> conditions,
        String eventId
    ) {}

    private final List<CrossNpcTrigger> triggers;

    public CrossNpcTriggerEngine(List<CrossNpcTrigger> triggers) {
        this.triggers = new ArrayList<>(triggers);
    }

    public List<String> evaluate(
        NpcRegistry registry,
        NpcRelationshipGraph graph,
        ConditionEvaluator evaluator,
        Map<String, Object> gameContext
    ) {
        List<String> firedEvents = new ArrayList<>();
        for (CrossNpcTrigger trigger : triggers) {
            boolean allMet = true;
            for (ConditionSpec cond : trigger.conditions()) {
                Optional<NpcInstance> npc = registry.get(trigger.npcA());
                if (npc.isEmpty() || !evaluator.evaluate(cond, npc.get(), gameContext)) {
                    allMet = false;
                    break;
                }
            }
            if (allMet) {
                firedEvents.add(trigger.eventId());
            }
        }
        return firedEvents;
    }
}
