package com.sagat9881.lifeoft.domain.npc.model;

import com.sagat9881.lifeoft.domain.npc.spec.NpcSpec;

/**
 * Live NPC instance in a game session.
 * Combines static spec (from XML) with dynamic state (mood, memory, activity).
 * Named NPCs: full state (6-axis mood, memory, utility brain decisions).
 * Filler NPCs: light state (2-axis mood, no memory, schedule-only activities).
 */
public class NpcInstance {

    private final NpcSpec spec;
    private NpcMood mood;
    private final NpcMemory memory; // null for filler NPCs
    private final NpcSchedule schedule;
    private NpcActivity currentActivity;
    private String currentLocation;

    public NpcInstance(NpcSpec spec, NpcMood mood, NpcMemory memory,
                       NpcSchedule schedule, NpcActivity currentActivity, String currentLocation) {
        this.spec = spec;
        this.mood = mood;
        this.memory = memory;
        this.schedule = schedule;
        this.currentActivity = currentActivity;
        this.currentLocation = currentLocation;
    }

    public NpcSpec spec() { return spec; }
    public NpcMood mood() { return mood; }
    public NpcMemory memory() { return memory; }
    public NpcSchedule schedule() { return schedule; }
    public NpcActivity currentActivity() { return currentActivity; }
    public String currentLocation() { return currentLocation; }

    public void setMood(NpcMood mood) { this.mood = mood; }
    public void setCurrentActivity(NpcActivity activity) { this.currentActivity = activity; }
    public void setCurrentLocation(String location) { this.currentLocation = location; }

    /**
     * Player interacted with this NPC. Update mood and record in memory.
     */
    public void onPlayerInteraction(String actionId, int day, double qualityMultiplier) {
        this.mood = mood.onPlayerInteraction(qualityMultiplier);
        if (memory != null) {
            memory.record(actionId, day);
        }
    }

    /**
     * Is this a named (full brain) NPC?
     */
    public boolean isNamed() {
        return "named".equals(spec.type());
    }

    @Override
    public String toString() {
        return String.format("NpcInstance[%s, %s, mood=%s, at=%s, doing=%s]",
                spec.id(), spec.type(), mood,
                currentLocation, currentActivity != null ? currentActivity.activityId() : "none");
    }
}
