package com.sagat9881.lifeoft.domain.npc.engine;

import com.sagat9881.lifeoft.domain.npc.model.*;

import java.util.*;

/**
 * Manages NPC lifecycle: hourly activity updates and daily mood ticks.
 * 
 * Called by DayEndProcessor and ActionExecutor to keep NPCs alive.
 * All behavior is data-driven — engine knows no specific NPC names.
 */
public class NpcLifecycleEngine {

    private final NpcUtilityBrain utilityBrain;

    public NpcLifecycleEngine(ConditionEvaluator conditionEvaluator) {
        this.utilityBrain = new NpcUtilityBrain(conditionEvaluator);
    }

    /**
     * Update all NPC activities for a given hour.
     * Called when game time advances.
     */
    public void hourlyTick(NpcRegistry registry, int currentHour, Object gameContext) {
        for (NpcInstance npc : registry.all()) {
            NpcActivity newActivity = utilityBrain.selectActivity(npc, currentHour, gameContext);
            npc.setCurrentActivity(newActivity);
        }
    }

    /**
     * Daily tick: update mood decay, process memory patterns,
     * generate NPC-initiated events.
     * Called during endDay().
     */
    public List<NpcInitiatedEvent> dailyTick(NpcRegistry registry, Object gameContext) {
        List<NpcInitiatedEvent> events = new ArrayList<>();

        for (NpcInstance npc : registry.all()) {
            // 1. Apply daily mood decay/drift toward baseline
            npc.mood().dailyTick(npc.spec().moodDecayRates());

            // 2. Loneliness increases if player didn't interact
            if ("named".equals(npc.spec().type())) {
                boolean interactedToday = npc.memory().hasInteractionToday();
                if (!interactedToday) {
                    npc.mood().adjustAxis("loneliness", 8.0);
                    npc.mood().adjustAxis("affection", -3.0);
                }
            }

            // 3. Check if NPC wants to initiate an event
            Optional<ScoredAction> initiated = utilityBrain.evaluate(npc, gameContext);
            if (initiated.isPresent()) {
                ScoredAction action = initiated.get();
                if (action.isEventInitiator()) {
                    events.add(new NpcInitiatedEvent(
                            npc.spec().id(),
                            action.actionId(),
                            action.eventType(),
                            action.options()
                    ));
                }
            }
        }

        return events;
    }

    /**
     * Notify all NPCs that player performed an action.
     * Updates memory and may trigger mood changes.
     */
    public void onPlayerAction(NpcRegistry registry, String actionId, int day, int hour) {
        registry.observePlayerAction(actionId, day);

        // Check if any NPC has a reaction to this specific action
        for (NpcInstance npc : registry.named()) {
            for (var reaction : npc.spec().actionReactions()) {
                if (reaction.triggerActionId().equals(actionId)) {
                    applyReaction(npc, reaction);
                }
            }
        }
    }

    private void applyReaction(NpcInstance npc, NpcSpec.ActionReaction reaction) {
        for (var moodChange : reaction.moodChanges().entrySet()) {
            npc.mood().adjustAxis(moodChange.getKey(), moodChange.getValue());
        }
    }

    /**
     * Get current activity snapshots for all NPCs (for frontend rendering).
     */
    public List<NpcActivitySnapshot> getActivitySnapshots(NpcRegistry registry) {
        List<NpcActivitySnapshot> snapshots = new ArrayList<>();
        for (NpcInstance npc : registry.all()) {
            snapshots.add(new NpcActivitySnapshot(
                    npc.spec().id(),
                    npc.spec().displayName(),
                    npc.spec().category(),
                    npc.currentActivity().activityId(),
                    npc.currentActivity().animationKey(),
                    npc.currentActivity().locationId()
            ));
        }
        return snapshots;
    }
}
