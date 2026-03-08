package ru.lifegame.backend.domain.npc;

import java.util.Set;

/**
 * A live NPC instance in a game session.
 * Created from NpcSpec at session start.
 * Named NPCs get full brain (6-axis mood, memory, utility AI).
 * Filler NPCs get light brain (2-axis mood, fixed schedule, no memory).
 */
public class NpcInstance {
    private final NpcSpec spec;
    private NpcMood mood;
    private final NpcMemory memory;
    private final NpcSchedule schedule;
    private NpcActivity currentActivity;

    public NpcInstance(NpcSpec spec) {
        this.spec = spec;
        this.mood = NpcMood.fromInitial(spec.moodInitial(), spec.isNamed());
        this.memory = spec.memoryEnabled()
            ? new NpcMemory(spec.shortTermMemorySize())
            : NpcMemory.disabled();
        this.schedule = NpcSchedule.fromSpec(spec.scheduleSlots());
        this.currentActivity = NpcActivity.away();
    }

    /**
     * Called every game hour by NpcLifecycleEngine.
     * Updates current activity based on schedule.
     */
    public void hourlyTick(int currentHour) {
        NpcSpec.ScheduleSlot slot = schedule.slotAt(currentHour);
        this.currentActivity = (slot != null)
            ? NpcActivity.fromScheduleSlot(slot)
            : NpcActivity.away();
    }

    /**
     * Called at end of day by NpcLifecycleEngine.
     * Mood drifts based on relationship closeness and days since interaction.
     */
    public void dailyTick(int closeness, int daysSinceInteraction) {
        this.mood = mood.dailyTick(closeness, daysSinceInteraction, spec.personalityTraits());
    }

    /**
     * Player directly interacted with this NPC.
     */
    public void onPlayerInteraction(int day, String actionCode) {
        if (memory.isEnabled()) {
            memory.record(day, actionCode, "direct");
        }
        this.mood = mood.onInteraction(spec.personalityTrait("warmth"));
    }

    /**
     * Player performed an action that this NPC can observe.
     */
    public void observePlayerAction(int day, String actionCode) {
        if (memory.isEnabled()) {
            memory.record(day, actionCode, "observed");
        }
    }

    /**
     * Override current activity due to mood extreme or event.
     */
    public void overrideActivity(NpcActivity override) {
        this.currentActivity = override;
    }

    // Accessors
    public String id() { return spec.id(); }
    public NpcSpec spec() { return spec; }
    public NpcMood mood() { return mood; }
    public NpcMemory memory() { return memory; }
    public NpcSchedule schedule() { return schedule; }
    public NpcActivity currentActivity() { return currentActivity; }
    public boolean hasMemory() { return memory.isEnabled(); }
    public boolean isNamed() { return spec.isNamed(); }
    public boolean isPresent(int hour) { return schedule.isAvailable(hour); }
}
