package ru.lifegame.backend.domain.npc;

import ru.lifegame.backend.domain.event.game.GameEvent;

import java.util.*;
import java.util.function.Function;

/**
 * Drives the lifecycle of all NPCs in a game session.
 * Called by DayEndProcessor and ActionExecutor.
 *
 * Responsibilities:
 * - hourlyTick: update NPC activities based on schedule + mood overrides
 * - dailyTick: update NPC moods, reset utility brain
 * - evaluateInitiatives: run Utility AI for all named NPCs, return events
 */
public class NpcLifecycleEngine {
    private final NpcRegistry registry;
    private final NpcUtilityBrain brain;

    public NpcLifecycleEngine(NpcRegistry registry) {
        this.registry = registry;
        this.brain = new NpcUtilityBrain();
    }

    /**
     * Called every game hour (after player action advances time).
     * Updates NPC physical activities based on schedule.
     * If NPC mood is extreme, overrides scheduled activity.
     */
    public void hourlyTick(int currentHour) {
        for (NpcInstance npc : registry.all()) {
            npc.hourlyTick(currentHour);
            applyMoodOverrides(npc, currentHour);
        }
    }

    /**
     * Called at end of day by DayEndProcessor.
     * Drifts NPC moods based on relationship state.
     *
     * @param closenessProvider maps npcId -> closeness value from Relationships
     * @param daysSinceProvider maps npcId -> days since last interaction
     */
    public void dailyTick(Function<String, Integer> closenessProvider,
                          Function<String, Integer> daysSinceProvider) {
        for (NpcInstance npc : registry.all()) {
            int closeness = closenessProvider.apply(npc.id());
            int daysSince = daysSinceProvider.apply(npc.id());
            npc.dailyTick(closeness, daysSince);
        }
        brain.resetDaily();
    }

    /**
     * Evaluate all named NPCs for initiative actions (Utility AI).
     * Returns list of GameEvents that NPC want to initiate.
     * Usually called once per endDay after dailyTick.
     */
    public List<GameEvent> evaluateInitiatives(int currentDay, int currentHour,
                                                ConditionEvaluator.StatAccessor stats) {
        List<GameEvent> events = new ArrayList<>();
        for (NpcInstance npc : registry.named()) {
            brain.evaluate(npc, currentDay, currentHour, stats)
                .ifPresent(events::add);
        }
        return events;
    }

    /**
     * Broadcast a player action to all present NPCs.
     */
    public void onPlayerAction(int day, String actionCode, int currentHour) {
        registry.broadcastPlayerAction(day, actionCode, currentHour);
    }

    /**
     * Notify a specific NPC of direct player interaction.
     */
    public void onDirectInteraction(String npcId, int day, String actionCode) {
        registry.get(npcId).ifPresent(npc -> npc.onPlayerInteraction(day, actionCode));
    }

    /**
     * Get all NPCs currently visible at a location.
     */
    public List<NpcActivitySnapshot> getActivitySnapshots(int currentHour) {
        List<NpcActivitySnapshot> snapshots = new ArrayList<>();
        for (NpcInstance npc : registry.all()) {
            NpcActivity activity = npc.currentActivity();
            if (activity.isPresent()) {
                snapshots.add(new NpcActivitySnapshot(
                    npc.id(),
                    npc.spec().displayName(),
                    npc.spec().category(),
                    activity.activityId(),
                    activity.animationKey(),
                    activity.locationId(),
                    npc.mood().dominantEmotion()
                ));
            }
        }
        return snapshots;
    }

    /**
     * Mood extreme overrides scheduled activity.
     * E.g., irritated husband goes for a walk instead of dinner.
     */
    private void applyMoodOverrides(NpcInstance npc, int currentHour) {
        if (!npc.isNamed()) return;

        String emotion = npc.mood().dominantEmotion();
        switch (emotion) {
            case "irritated" -> npc.overrideActivity(
                new NpcActivity("sulking", "arms_crossed", npc.currentActivity().locationId()));
            case "anxious" -> npc.overrideActivity(
                new NpcActivity("pacing", "walking_slow", npc.currentActivity().locationId()));
            case "lonely" -> {
                if (npc.currentActivity().isPresent()) {
                    npc.overrideActivity(
                        new NpcActivity("waiting", "looking_door", npc.currentActivity().locationId()));
                }
            }
            default -> {} // keep scheduled activity
        }
    }

    /**
     * Snapshot of NPC activity for frontend rendering.
     */
    public record NpcActivitySnapshot(
        String npcId,
        String displayName,
        String category,
        String activityId,
        String animationKey,
        String locationId,
        String dominantEmotion
    ) {}

    public NpcRegistry registry() { return registry; }
}
