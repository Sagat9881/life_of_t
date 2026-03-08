package com.sagat9881.lifeoft.domain.npc.engine;

import com.sagat9881.lifeoft.domain.npc.model.NpcInstance;
import com.sagat9881.lifeoft.domain.npc.model.NpcInitiatedEvent;

import java.util.*;

/**
 * Checks cross-NPC conditions and generates events.
 * 
 * Examples of cross-NPC triggers:
 * - Player spends too much time with one NPC → another gets jealous
 * - Tension between two NPCs exceeds threshold → they argue, player picks side
 * - Two NPCs both have high loneliness → they interact with each other
 * 
 * All trigger definitions come from XML — engine operates on abstractions.
 */
public class CrossNpcTriggerEngine {

    private final NpcRelationshipGraph graph;
    private final List<CrossNpcTriggerSpec> triggerSpecs;

    public CrossNpcTriggerEngine(NpcRelationshipGraph graph, List<CrossNpcTriggerSpec> triggerSpecs) {
        this.graph = graph;
        this.triggerSpecs = triggerSpecs != null ? triggerSpecs : Collections.emptyList();
    }

    /**
     * Check all cross-NPC triggers and return generated events.
     * Called during dailyTick.
     */
    public List<NpcInitiatedEvent> check(NpcRegistry registry, Object gameContext) {
        List<NpcInitiatedEvent> events = new ArrayList<>();

        for (CrossNpcTriggerSpec trigger : triggerSpecs) {
            if (evaluateTrigger(trigger, registry)) {
                events.add(createEvent(trigger));
                // Apply relationship changes from trigger
                applyRelationshipEffects(trigger);
            }
        }

        return events;
    }

    /**
     * Update relationship graph when player interacts with an NPC.
     * Other NPCs may react (jealousy, relief, etc.).
     */
    public void onPlayerInteraction(String targetNpcId, NpcRegistry registry) {
        for (NpcInstance npc : registry.named()) {
            if (!npc.spec().id().equals(targetNpcId)) {
                // Check if this NPC has jealousy/attention triggers toward targetNpc
                graph.getRelation(npc.spec().id(), targetNpcId).ifPresent(rel -> {
                    if (rel.tension() > 30) {
                        // Slight tension increase when player pays attention to rival
                        graph.adjustTension(npc.spec().id(), targetNpcId, 2.0);
                        npc.mood().adjustAxis("irritability", 3.0);
                    }
                });
            }
        }
    }

    private boolean evaluateTrigger(CrossNpcTriggerSpec trigger, NpcRegistry registry) {
        Optional<NpcInstance> npcA = registry.get(trigger.npcA());
        Optional<NpcInstance> npcB = registry.get(trigger.npcB());

        if (npcA.isEmpty() || npcB.isEmpty()) return false;

        return switch (trigger.conditionType()) {
            case "tension_threshold" ->
                    graph.getRelation(trigger.npcA(), trigger.npcB())
                            .map(rel -> rel.tension() >= trigger.threshold())
                            .orElse(false);
            case "both_lonely" ->
                    npcA.get().mood().getAxis("loneliness") >= trigger.threshold()
                            && npcB.get().mood().getAxis("loneliness") >= trigger.threshold();
            case "mood_contrast" -> {
                double diff = Math.abs(
                        npcA.get().mood().getAxis(trigger.moodAxis())
                                - npcB.get().mood().getAxis(trigger.moodAxis())
                );
                yield diff >= trigger.threshold();
            }
            default -> false;
        };
    }

    private NpcInitiatedEvent createEvent(CrossNpcTriggerSpec trigger) {
        return new NpcInitiatedEvent(
                trigger.npcA() + "+" + trigger.npcB(),
                trigger.eventId(),
                trigger.eventType(),
                trigger.options()
        );
    }

    private void applyRelationshipEffects(CrossNpcTriggerSpec trigger) {
        if (trigger.tensionDelta() != 0) {
            graph.adjustTension(trigger.npcA(), trigger.npcB(), trigger.tensionDelta());
        }
        if (trigger.respectDelta() != 0) {
            graph.adjustRespect(trigger.npcA(), trigger.npcB(), trigger.respectDelta());
        }
    }

    /**
     * Spec for a cross-NPC trigger — loaded from XML.
     */
    public record CrossNpcTriggerSpec(
            String npcA,
            String npcB,
            String conditionType,
            String moodAxis,
            double threshold,
            String eventId,
            String eventType,
            List<Map<String, String>> options,
            double tensionDelta,
            double respectDelta
    ) {
    }
}
