package com.sagat9881.lifeoft.domain.npc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Manages NPC lifecycle ticks — hourly activity updates and daily mood/memory processing.
 * The engine is data-driven: it doesn't know about specific NPCs,
 * it processes whatever the NpcRegistry contains.
 */
public class NpcLifecycleEngine {

    private final NpcUtilityBrain utilityBrain;

    public NpcLifecycleEngine(NpcUtilityBrain utilityBrain) {
        this.utilityBrain = utilityBrain;
    }

    /**
     * Called every game hour. Updates each NPC's current activity
     * based on schedule + utility AI evaluation.
     * Returns list of NPC-initiated events (if any NPC decided to act).
     */
    public List<NpcInitiatedEvent> hourlyTick(NpcRegistry registry, int currentHour, Object gameContext) {
        List<NpcInitiatedEvent> events = new ArrayList<>();

        for (NpcInstance npc : registry.all()) {
            // Step 1: Update activity from schedule
            NpcActivity scheduledActivity = npc.schedule().activityAt(currentHour);
            NpcInstance updated = npc.withActivity(scheduledActivity);

            // Step 2: For named NPCs, check if mood overrides schedule
            if ("named".equals(npc.spec().type())) {
                Optional<NpcActivity> moodOverride = checkMoodOverride(updated, currentHour);
                if (moodOverride.isPresent()) {
                    updated = updated.withActivity(moodOverride.get());
                }

                // Step 3: Utility AI — should NPC initiate an event?
                Optional<ScoredAction> bestAction = utilityBrain.evaluate(updated, gameContext);
                if (bestAction.isPresent()) {
                    ScoredAction action = bestAction.get();
                    // Only trigger if score exceeds threshold (avoid spam)
                    if (action.baseScore() > 0.4) {
                        events.add(NpcInitiatedEvent.fromScoredAction(npc.spec().id(), action));
                        // Record in memory that NPC initiated
                        updated.memory().recordEvent("initiated:" + action.actionId());
                    }
                }
            }

            registry.update(updated);
        }

        return events;
    }

    /**
     * Called at end of day. Processes mood decay, memory consolidation,
     * and resets daily counters.
     */
    public void dailyTick(NpcRegistry registry, Object gameContext) {
        for (NpcInstance npc : registry.all()) {
            // Mood daily decay toward baseline
            NpcMood decayedMood = npc.mood().dailyTick();
            NpcInstance updated = npc.withMood(decayedMood);

            // Memory: consolidate short-term → long-term for named NPCs
            if ("named".equals(npc.spec().type()) && npc.memory().isEnabled()) {
                updated.memory().consolidate();
            }

            registry.update(updated);
        }
    }

    /**
     * Notify all NPCs that the player performed an action.
     * Used for memory tracking and mood reactions.
     */
    public void onPlayerAction(NpcRegistry registry, String actionId, String targetNpcId) {
        for (NpcInstance npc : registry.all()) {
            if (npc.spec().id().equals(targetNpcId)) {
                // Direct interaction — boost mood
                NpcMood boosted = npc.mood()
                        .withLoneliness(Math.max(0, npc.mood().loneliness() - 15))
                        .withHappiness(Math.min(100, npc.mood().happiness() + 10))
                        .withAffection(Math.min(100, npc.mood().affection() + 5));
                NpcInstance updated = npc.withMood(boosted);
                updated.memory().recordEvent("player:" + actionId);
                registry.update(updated);
            } else if ("named".equals(npc.spec().type())) {
                // Indirect observation — other NPCs notice
                npc.memory().recordEvent("observed:" + actionId + ":" + targetNpcId);
                registry.update(npc);
            }
        }
    }

    /**
     * Check if extreme mood should override scheduled activity.
     * E.g., highly irritated NPC leaves dinner to be alone.
     */
    private Optional<NpcActivity> checkMoodOverride(NpcInstance npc, int hour) {
        NpcMood mood = npc.mood();

        // High irritability → isolate
        if (mood.irritability() > 75) {
            return Optional.of(new NpcActivity("isolating", "walking_away", "balcony"));
        }
        // Very low energy → rest regardless of schedule
        if (mood.energy() < 15 && hour < 22) {
            return Optional.of(new NpcActivity("resting", "sitting_tired", "sofa"));
        }
        // Very high loneliness → seek player
        if (mood.loneliness() > 80) {
            return Optional.of(new NpcActivity("seeking_player", "walking", "living_room"));
        }
        // High anxiety → pacing
        if (mood.anxiety() > 70) {
            return Optional.of(new NpcActivity("pacing", "pacing", "hallway"));
        }

        return Optional.empty();
    }
}
