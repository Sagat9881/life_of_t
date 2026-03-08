package ru.lifegame.backend.domain.engine.runtime;

import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.npc.NpcMood;
import ru.lifegame.backend.domain.npc.NpcMemory;

import java.util.Map;

public class NpcInstance {
    private final NpcSpec spec;
    private NpcMood mood;
    private final NpcMemory memory;
    private String currentActivity;
    private String currentLocation;
    private String currentAnimation;

    public NpcInstance(NpcSpec spec) {
        this.spec = spec;
        Map<String, Integer> mi = spec.moodInitial();
        this.mood = new NpcMood(
            mi.getOrDefault("happiness", 50),
            mi.getOrDefault("anxiety", 20),
            mi.getOrDefault("loneliness", 20),
            mi.getOrDefault("irritability", 10),
            mi.getOrDefault("energy", 70),
            mi.getOrDefault("affection", 50)
        );
        this.memory = spec.memoryEnabled() ? new NpcMemory(spec.shortTermSize()) : new NpcMemory(0);
        this.currentActivity = "idle";
        this.currentLocation = "unknown";
        this.currentAnimation = "idle";
    }

    public static NpcInstance createNamed(NpcSpec spec) { return new NpcInstance(spec); }
    public static NpcInstance createFiller(NpcSpec spec) { return new NpcInstance(spec); }
    public static NpcInstance fromSpec(NpcSpec spec) { return new NpcInstance(spec); }

    public NpcSpec spec() { return spec; }
    public NpcMood mood() { return mood; }
    public NpcMood getMood() { return mood; }
    public void setMood(NpcMood mood) { this.mood = mood; }
    public NpcMemory memory() { return memory; }
    public String currentActivity() { return currentActivity; }
    public String currentLocation() { return currentLocation; }
    public String currentAnimation() { return currentAnimation; }

    public void updateScheduleActivity(int currentHour) {
        // Schedule-based activity update
    }

    public void setCurrentActivity(String activity, String location, String animation) {
        this.currentActivity = activity;
        this.currentLocation = location;
        this.currentAnimation = animation;
    }

    public void updateActivity(String activity, String location, String animation) {
        setCurrentActivity(activity, location, animation);
    }
}
