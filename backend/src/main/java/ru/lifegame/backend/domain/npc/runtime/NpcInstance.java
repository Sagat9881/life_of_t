package ru.lifegame.backend.domain.npc.runtime;

import ru.lifegame.backend.domain.npc.spec.NpcSchedule;
import ru.lifegame.backend.domain.npc.spec.NpcSpec;

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

    public static NpcInstance createNamed(NpcSpec spec) {
        NpcMood mood = NpcMood.fromSpec(spec.moodInitial());
        NpcMemory memory = new NpcMemory(spec.shortTermSize());
        NpcSchedule schedule = buildSchedule(spec);
        return new NpcInstance(spec, mood, memory, schedule);
    }

    public static NpcInstance createFiller(NpcSpec spec) {
        NpcMood mood = NpcMood.fillerMood(
                spec.moodInitial().getOrDefault("happiness", 50),
                spec.moodInitial().getOrDefault("energy", 70)
        );
        NpcMemory memory = NpcMemory.disabled();
        NpcSchedule schedule = buildSchedule(spec);
        return new NpcInstance(spec, mood, memory, schedule);
    }

    public static NpcInstance fromSpec(NpcSpec spec) {
        return spec.isNamed() ? createNamed(spec) : createFiller(spec);
    }

    private static NpcSchedule buildSchedule(NpcSpec spec) {
        var slots = spec.schedule().stream()
                .map(s -> new NpcSchedule.ScheduleSlot(
                        s.start(), s.end(),
                        s.activity(), s.location(), s.animation()))
                .collect(Collectors.toList());
        return new NpcSchedule(slots);
    }

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

    public void updateScheduleActivity(int currentHour) {
        resolveActivity(currentHour);
    }

    public void setCurrentActivity(String activityId, String animationKey, String locationId) {
        this.currentActivity = new NpcActivity(activityId, animationKey, locationId);
    }

    public void checkMoodOverride(int currentHour) {
        if (!mood.hasExtremeState()) return;
        String dominant = mood.dominantAxis();
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

    public void observePlayerAction(String actionId, int day, int hour) {
        memory.observe(new NpcMemory.MemoryEntry(actionId, day, hour, ""));
    }

    public void dailyTick() {
        mood.dailyTick();
        schedule.clearOverrides();
    }

    public NpcMood getMood() { return mood; }

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
