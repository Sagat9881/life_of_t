package com.life_of_t.domain.npc;

import com.life_of_t.domain.npc.spec.NpcSpec;
import com.life_of_t.domain.npc.spec.NpcSpec.ScheduleSlotSpec;

import java.util.stream.Collectors;

/**
 * A live NPC instance in the game session.
 * Created from NpcSpec (XML) by NpcRegistry.
 * Named NPCs get full brain (mood 6 axes + memory + utility AI).
 * Filler NPCs get simplified brain (mood 2 axes + no memory).
 */
public class NpcInstance {

    private final NpcSpec spec;
    private final NpcMood mood;
    private final NpcMemory memory;
    private final NpcSchedule schedule;
    private NpcActivity currentActivity;

    private NpcInstance(NpcSpec spec, NpcMood mood, NpcMemory memory, NpcSchedule schedule) {
        this.spec = spec;
        this.mood = mood;
        this.memory = memory;
        this.schedule = schedule;
        this.currentActivity = NpcActivity.idle("default");
    }

    /**
     * Create a fully-featured named NPC from spec.
     */
    public static NpcInstance createNamed(NpcSpec spec) {
        NpcMood mood = NpcMood.fromSpec(spec.moodInitial());
        NpcMemory memory = new NpcMemory(spec.memoryShortTermSize());
        NpcSchedule schedule = buildSchedule(spec);
        return new NpcInstance(spec, mood, memory, schedule);
    }

    /**
     * Create a simplified filler NPC from spec.
     */
    public static NpcInstance createFiller(NpcSpec spec) {
        NpcMood mood = NpcMood.fillerMood(
                spec.moodInitial().getOrDefault("happiness", 50),
                spec.moodInitial().getOrDefault("energy", 70)
        );
        NpcMemory memory = NpcMemory.disabled();
        NpcSchedule schedule = buildSchedule(spec);
        return new NpcInstance(spec, mood, memory, schedule);
    }

    private static NpcSchedule buildSchedule(NpcSpec spec) {
        var slots = spec.scheduleSlots().stream()
                .map(s -> new NpcSchedule.ScheduleSlot(
                        s.startHour(), s.endHour(),
                        s.activityId(), s.locationId(), s.animationKey()))
                .collect(Collectors.toList());
        return new NpcSchedule(slots);
    }

    /**
     * Update current activity based on schedule resolution for given hour.
     * Returns the resolved activity (for rendering).
     */
    public NpcActivity resolveActivity(int currentHour) {
        var slot = schedule.resolveSlot(currentHour);
        if (slot.isPresent()) {
            var s = slot.get();
            this.currentActivity = new NpcActivity(s.activityId(), s.animationKey(), s.locationId());
        } else {
            this.currentActivity = NpcActivity.idle("default");
        }
        return this.currentActivity;
    }

    /**
     * Apply mood override to schedule if mood is extreme.
     * Called by NpcLifecycleEngine during hourly tick.
     */
    public void checkMoodOverride(int currentHour) {
        if (!mood.hasExtremeState()) return;

        String dominant = mood.dominantAxis();
        // Find mood-override action from spec
        spec.moodOverrideActions().stream()
                .filter(a -> a.triggerAxis().equals(dominant))
                .findFirst()
                .ifPresent(override -> {
                    var overrideSlot = new NpcSchedule.ScheduleSlot(
                            currentHour, currentHour + override.durationHours(),
                            override.activityId(), override.locationId(), override.animationKey());
                    schedule.setMoodOverride(overrideSlot);
                });
    }

    /**
     * Record that the player performed an action observed by this NPC.
     */
    public void observePlayerAction(String actionId, int day, int hour) {
        memory.observe(new NpcMemory.MemoryEntry(actionId, day, hour, ""));
    }

    /**
     * Daily tick: mood decay, clear schedule overrides, prepare for new day.
     */
    public void dailyTick() {
        mood.dailyTick();
        schedule.clearOverrides();
    }

    // Accessors
    public String id() { return spec.id(); }
    public String type() { return spec.type(); }
    public String category() { return spec.category(); }
    public String displayName() { return spec.displayName(); }
    public NpcSpec spec() { return spec; }
    public NpcMood mood() { return mood; }
    public NpcMemory memory() { return memory; }
    public NpcSchedule schedule() { return schedule; }
    public NpcActivity currentActivity() { return currentActivity; }
}
