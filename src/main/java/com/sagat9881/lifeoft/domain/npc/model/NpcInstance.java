package com.sagat9881.lifeoft.domain.npc.model;

import java.util.Optional;

/**
 * A living NPC instance in a game session.
 * Created from NpcSpec (XML) + runtime state (mood, memory, activity).
 * 
 * Mutable: mood changes, memory grows, activity updates each hour.
 * The engine operates on NpcInstance without knowing specific NPC identities.
 */
public class NpcInstance {

    private final NpcSpec spec;
    private final NpcMood mood;
    private final NpcMemoryLog memory;
    private final NpcSchedule schedule;
    private NpcActivity currentActivity;

    public NpcInstance(NpcSpec spec, NpcMood mood, NpcMemoryLog memory,
                       NpcSchedule schedule, NpcActivity initialActivity) {
        this.spec = spec;
        this.mood = mood;
        this.memory = memory;
        this.schedule = schedule;
        this.currentActivity = initialActivity;
    }

    public NpcSpec spec() {
        return spec;
    }

    public NpcMood mood() {
        return mood;
    }

    public NpcMemoryLog memory() {
        return memory;
    }

    public NpcSchedule schedule() {
        return schedule;
    }

    public NpcActivity currentActivity() {
        return currentActivity;
    }

    public void setCurrentActivity(NpcActivity activity) {
        this.currentActivity = activity;
    }

    /**
     * Get scheduled activity for a given hour.
     * Returns Optional.empty() if no schedule slot covers this hour.
     */
    public Optional<NpcActivity> getScheduledActivity(int hour) {
        return schedule.getActivityAt(hour);
    }

    /**
     * Check if NPC is available for interaction at given hour.
     */
    public boolean isAvailable(int hour) {
        return schedule.isAvailableAt(hour);
    }

    @Override
    public String toString() {
        return "NpcInstance[" + spec.id() + ", activity=" + currentActivity.activityId() + "]";
    }
}
