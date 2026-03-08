package com.sagat9881.lifeoft.domain.npc.engine;

import com.sagat9881.lifeoft.domain.npc.model.NpcInstance;
import com.sagat9881.lifeoft.domain.npc.model.NpcActivity;
import com.sagat9881.lifeoft.domain.npc.model.NpcMood;
import com.sagat9881.lifeoft.domain.model.session.GameSessionContext;
import com.sagat9881.lifeoft.domain.event.game.GameEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Manages NPC lifecycle: hourly activity updates and daily mood/memory ticks.
 * Delegates decision-making to NpcUtilityBrain.
 * Produces NPC-initiated GameEvents when NPCs decide to interact with player.
 */
public class NpcLifecycleEngine {

    private final NpcUtilityBrain utilityBrain;
    private final NpcRegistry registry;

    public NpcLifecycleEngine(NpcUtilityBrain utilityBrain, NpcRegistry registry) {
        this.utilityBrain = utilityBrain;
        this.registry = registry;
    }

    /**
     * Called every game hour when player performs an action.
     * Updates each NPC's current activity based on Utility AI evaluation.
     * Returns list of NPC-initiated events (if any NPC wants to interact with player).
     */
    public List<GameEvent> hourlyTick(GameSessionContext context) {
        List<GameEvent> npcEvents = new ArrayList<>();

        for (NpcInstance npc : registry.all()) {
            Optional<NpcActivity> bestAction = utilityBrain.selectBestAction(npc, context);

            if (bestAction.isPresent()) {
                NpcActivity activity = bestAction.get();
                npc.setCurrentActivity(activity);
                npc.setCurrentLocation(activity.location());

                // If this action has event options (NPC wants to interact with player),
                // convert to GameEvent
                var scoredAction = findScoredAction(npc, activity.activityId());
                if (scoredAction.isPresent() && scoredAction.get().hasOptions()) {
                    GameEvent event = NpcInitiatedEvent.from(npc, scoredAction.get());
                    npcEvents.add(event);
                }
            } else {
                // Fall back to schedule
                int hour = context.time().hour();
                npc.schedule().getSlotForHour(hour).ifPresent(slot -> {
                    npc.setCurrentActivity(new NpcActivity(slot.activity(), slot.animation(), slot.location()));
                    npc.setCurrentLocation(slot.location());
                });
            }
        }

        return npcEvents;
    }

    /**
     * Called at end of each day.
     * Applies mood decay, cleans up memory, updates loneliness based on interactions.
     */
    public void dailyTick(GameSessionContext context) {
        int currentDay = context.time().day();

        for (NpcInstance npc : registry.all()) {
            // Mood daily decay
            NpcMood currentMood = npc.mood();
            NpcMood decayed = applyDailyMoodDecay(currentMood, npc);
            npc.setMood(decayed);

            // Memory cleanup for named NPCs
            if (npc.memory() != null) {
                npc.memory().cleanupOlderThan(currentDay - 10);

                // Increase loneliness if player hasn't interacted
                if (npc.memory().isBeingIgnored(3)) {
                    npc.setMood(npc.mood().withLoneliness(
                            Math.min(100, npc.mood().loneliness() + 8)
                    ));
                }
            }
        }
    }

    private NpcMood applyDailyMoodDecay(NpcMood mood, NpcInstance npc) {
        // Personality traits influence decay rates
        var traits = npc.spec().personalityTraits();
        double patienceModifier = traits.getOrDefault("patience", 50.0) / 100.0;
        double warmthModifier = traits.getOrDefault("warmth", 50.0) / 100.0;

        return new NpcMood(
                clamp(mood.happiness() - 3 + warmthModifier * 2),
                clamp(mood.anxiety() - 2),
                clamp(mood.loneliness() + 5 - warmthModifier * 3),
                clamp(mood.irritability() - 3 * patienceModifier),
                clamp(mood.energy() + 15), // Sleep restores energy
                clamp(mood.affection() - 2)
        );
    }

    private double clamp(double value) {
        return Math.max(0, Math.min(100, value));
    }

    private Optional<com.sagat9881.lifeoft.domain.npc.spec.ScoredAction> findScoredAction(
            NpcInstance npc, String actionId) {
        if (npc.spec().actions() == null) return Optional.empty();
        return npc.spec().actions().stream()
                .filter(a -> a.actionId().equals(actionId))
                .findFirst();
    }
}
