package com.sagat9881.lifeoft.domain.npc;

import com.sagat9881.lifeoft.domain.npc.spec.NpcSpec;
import com.sagat9881.lifeoft.domain.npc.spec.ScheduleSlotSpec;

import java.util.Optional;

/**
 * A live NPC instance in a game session.
 * Created from NpcSpec — the engine never knows concrete NPC identities.
 *
 * Named NPCs: full mood (6 axes) + memory + utility brain.
 * Filler NPCs: simplified mood (2 axes) + fixed schedule, no memory.
 */
public class NpcInstance {

    private final NpcSpec spec;
    private final NpcMood mood;
    private final NpcMemoryLog memory;
    private final NpcSchedule schedule;
    private NpcActivity currentActivity;

    private NpcInstance(NpcSpec spec, NpcMood mood, NpcMemoryLog memory, NpcSchedule schedule) {
        this.spec = spec;
        this.mood = mood;
        this.memory = memory;
        this.schedule = schedule;
        this.currentActivity = NpcActivity.idle("spawn");
    }

    /**
     * Factory: creates NpcInstance from spec.
     * Named → full brain. Filler → simplified brain.
     */
    public static NpcInstance fromSpec(NpcSpec spec) {
        NpcMood mood = NpcMood.fromSpec(spec);
        NpcMemoryLog memory = spec.memoryEnabled()
                ? new NpcMemoryLog(spec.shortTermMemorySize())
                : NpcMemoryLog.disabled();
        NpcSchedule schedule = NpcSchedule.fromSpec(spec);
        return new NpcInstance(spec, mood, memory, schedule);
    }

    public String id() { return spec.id(); }
    public NpcSpec spec() { return spec; }
    public NpcMood mood() { return mood; }
    public NpcMemoryLog memory() { return memory; }
    public NpcSchedule schedule() { return schedule; }
    public NpcActivity currentActivity() { return currentActivity; }

    public boolean hasMemory() {
        return memory != null && memory.isEnabled();
    }

    public boolean isNamed() {
        return spec.isNamed();
    }

    /**
     * Updates NPC activity based on current hour and mood.
     * Schedule resolves through 3 layers: base → mood → quest.
     */
    public void updateActivity(int currentHour) {
        String dominantMood = mood.dominantExtreme(70);
        Optional<ScheduleSlotSpec> slot = schedule.resolveActivity(currentHour, dominantMood);

        if (slot.isPresent()) {
            ScheduleSlotSpec s = slot.get();
            this.currentActivity = new NpcActivity(
                    s.activityId(), s.animationKey(), s.locationId(), s.durationHours()
            );
        } else {
            this.currentActivity = NpcActivity.idle("home");
        }
    }

    public boolean isAvailableAt(int hour) {
        String dominantMood = mood.dominantExtreme(70);
        return schedule.isAvailableAt(hour, dominantMood);
    }

    /**
     * Records a player action in NPC memory.
     */
    public void observeAction(String actionId, int day, int hour) {
        if (hasMemory()) {
            memory.recordAction(actionId, day, hour, false);
        }
    }

    /**
     * Records a significant event in long-term memory.
     */
    public void observeSignificantEvent(String eventId, int day, int hour) {
        if (hasMemory()) {
            memory.recordAction(eventId, day, hour, true);
        }
    }

    /**
     * Daily tick: mood decay + memory day-end.
     */
    public void dailyTick() {
        mood.dailyTick();
        if (hasMemory()) {
            memory.onDayEnd();
        }
    }
}
