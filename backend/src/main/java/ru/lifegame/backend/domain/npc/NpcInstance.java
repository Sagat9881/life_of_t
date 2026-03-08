package ru.lifegame.backend.domain.npc;

import com.life_of_t.domain.npc.NpcActivity;
import com.life_of_t.domain.npc.NpcSchedule;
import com.life_of_t.domain.npc.spec.NpcSpec;

import java.util.stream.Collectors;

/**
 * A live NPC instance in the game session.
 * Created from NpcSpec (XML) by NpcRegistry.
 * Named NPCs get full brain (mood 6 axes + memory + utility AI).
 * Filler NPCs get simplified brain (mood 2 axes + no memory).
 */
public class NpcInstance {

    private final NpcSpec spec;
    private final ru.lifegame.backend.domain.npc.NpcMood mood;
    private final ru.lifegame.backend.domain.npc.NpcMemory memory;
    private final ru.lifegame.backend.domain.npc.NpcSchedule schedule;
    private ru.lifegame.backend.domain.npc.NpcActivity currentActivity;

    private NpcInstance(NpcSpec spec, ru.lifegame.backend.domain.npc.NpcMood mood, ru.lifegame.backend.domain.npc.NpcMemory memory, ru.lifegame.backend.domain.npc.NpcSchedule schedule) {
        this.spec = spec;
        this.mood = mood;
        this.memory = memory;
        this.schedule = schedule;
        this.currentActivity = ru.lifegame.backend.domain.npc.NpcActivity.idle("default");
    }

    /**
     * Create a fully-featured named NPC from spec.
     */
    public static NpcInstance createNamed(NpcSpec spec) {
        ru.lifegame.backend.domain.npc.NpcMood mood = ru.lifegame.backend.domain.npc.NpcMood.fromSpec(spec.moodInitial());
        ru.lifegame.backend.domain.npc.NpcMemory memory = new ru.lifegame.backend.domain.npc.NpcMemory(spec.memoryShortTermSize());
        ru.lifegame.backend.domain.npc.NpcSchedule schedule = buildSchedule(spec);
        return new NpcInstance(spec, mood, memory, schedule);
    }

    /**
     * Create a simplified filler NPC from spec.
     */
    public static NpcInstance createFiller(NpcSpec spec) {
        ru.lifegame.backend.domain.npc.NpcMood mood = ru.lifegame.backend.domain.npc.NpcMood.fillerMood(
                spec.moodInitial().getOrDefault("happiness", 50),
                spec.moodInitial().getOrDefault("energy", 70)
        );
        ru.lifegame.backend.domain.npc.NpcMemory memory = ru.lifegame.backend.domain.npc.NpcMemory.disabled();
        ru.lifegame.backend.domain.npc.NpcSchedule schedule = buildSchedule(spec);
        return new NpcInstance(spec, mood, memory, schedule);
    }

    private static ru.lifegame.backend.domain.npc.NpcSchedule buildSchedule(NpcSpec spec) {
        var slots = spec.scheduleSlots().stream()
                .map(s -> new ru.lifegame.backend.domain.npc.NpcSchedule.ScheduleSlot(
                        s.startHour(), s.endHour(),
                        s.activityId(), s.locationId(), s.animationKey()))
                .collect(Collectors.toList());
        return new ru.lifegame.backend.domain.npc.NpcSchedule(slots);
    }

    /**
     * Update current activity based on schedule resolution for given hour.
     * Returns the resolved activity (for rendering).
     */
    public ru.lifegame.backend.domain.npc.NpcActivity resolveActivity(int currentHour) {
        var slot = schedule.resolveSlot(currentHour);
        if (slot.isPresent()) {
            var s = slot.get();
            this.currentActivity = new ru.lifegame.backend.domain.npc.NpcActivity(s.activityId(), s.animationKey(), s.locationId());
        } else {
            this.currentActivity = ru.lifegame.backend.domain.npc.NpcActivity.idle("default");
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
                    var overrideSlot = new ru.lifegame.backend.domain.npc.NpcSchedule.ScheduleSlot(
                            currentHour, currentHour + override.durationHours(),
                            override.activityId(), override.locationId(), override.animationKey());
                    schedule.setMoodOverride(overrideSlot);
                });
    }

    /**
     * Record that the player performed an action observed by this NPC.
     */
    public void observePlayerAction(String actionId, int day, int hour) {
        memory.observe(new ru.lifegame.backend.domain.npc.NpcMemory.MemoryEntry(actionId, day, hour, ""));
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
    public ru.lifegame.backend.domain.npc.NpcMemory memory() { return memory; }
    public NpcSchedule schedule() { return schedule; }
    public NpcActivity currentActivity() { return currentActivity; }
}
