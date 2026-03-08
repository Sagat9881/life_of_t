package com.sagat9881.lifeoft.domain.npc;

/**
 * A live NPC instance within a game session.
 * Combines the immutable spec (from XML) with mutable runtime state:
 * mood, memory, schedule, and current physical activity.
 *
 * Supports both named NPCs (full brain: 6-axis mood, memory, utility AI)
 * and filler NPCs (simplified: 2-axis mood, fixed schedule, no memory).
 */
public class NpcInstance {

    private final NpcSpec spec;
    private NpcMood mood;
    private final NpcMemory memory;
    private final NpcSchedule schedule;
    private NpcActivity currentActivity;

    public NpcInstance(NpcSpec spec, NpcMood mood, NpcMemory memory,
                       NpcSchedule schedule, NpcActivity currentActivity) {
        this.spec = spec;
        this.mood = mood;
        this.memory = memory;
        this.schedule = schedule;
        this.currentActivity = currentActivity;
    }

    public NpcSpec spec() {
        return spec;
    }

    public NpcMood mood() {
        return mood;
    }

    public NpcMemory memory() {
        return memory;
    }

    public NpcSchedule schedule() {
        return schedule;
    }

    public NpcActivity currentActivity() {
        return currentActivity;
    }

    /**
     * Create a copy with updated mood.
     */
    public NpcInstance withMood(NpcMood newMood) {
        return new NpcInstance(spec, newMood, memory, schedule, currentActivity);
    }

    /**
     * Create a copy with updated activity.
     */
    public NpcInstance withActivity(NpcActivity newActivity) {
        return new NpcInstance(spec, mood, memory, schedule, newActivity);
    }

    /**
     * Check if NPC is currently available for interaction
     * (not away, not sleeping).
     */
    public boolean isAvailable() {
        if (currentActivity == null) return true;
        String loc = currentActivity.locationId();
        String act = currentActivity.activityId();
        return !"away".equals(loc)
                && !"sleeping".equals(act)
                && !"sleep".equals(act);
    }

    /**
     * Check if NPC is at a specific location.
     */
    public boolean isAtLocation(String locationId) {
        return currentActivity != null
                && locationId.equals(currentActivity.locationId());
    }

    @Override
    public String toString() {
        return "NpcInstance{" + spec.id() + ", activity=" + currentActivity + ", mood=" + mood + "}";
    }
}
