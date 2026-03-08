package ru.lifegame.backend.domain.engine.runtime;

import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.npc.engine.NpcMood;
import ru.lifegame.backend.domain.npc.engine.NpcMemory;

public class NpcInstance {

    private final NpcSpec spec;
    private final NpcMood mood;
    private final NpcMemory memory;
    private String currentActivity;
    private String currentAnimation;
    private String currentLocation;

    public NpcInstance(NpcSpec spec, NpcMood mood, NpcMemory memory) {
        this.spec = spec;
        this.mood = mood;
        this.memory = memory;
        this.currentActivity = "idle";
        this.currentAnimation = "idle";
        this.currentLocation = "unknown";
    }

    public static NpcInstance fromSpec(NpcSpec spec) {
        NpcMood mood = NpcMood.fromInitialValues(
            spec.moodInitial().getOrDefault("happiness", 50.0),
            spec.moodInitial().getOrDefault("anxiety", 20.0),
            spec.moodInitial().getOrDefault("loneliness", 20.0),
            spec.moodInitial().getOrDefault("irritability", 10.0),
            spec.moodInitial().getOrDefault("energy", 70.0),
            spec.moodInitial().getOrDefault("affection", 50.0)
        );
        boolean memoryEnabled = "named".equals(spec.type());
        NpcMemory memory = memoryEnabled ? new NpcMemory(10) : new NpcMemory(0);
        return new NpcInstance(spec, mood, memory);
    }

    public void updateScheduledActivity(int hour) {
        if (spec.scheduleSlots() == null) return;
        spec.scheduleSlots().stream()
            .filter(slot -> hour >= slot.start() && hour < slot.end())
            .findFirst()
            .ifPresent(slot -> {
                this.currentActivity = slot.activity();
                this.currentAnimation = slot.animation();
                this.currentLocation = slot.location();
            });
    }

    public void setCurrentActivity(String activity, String animation, String location) {
        this.currentActivity = activity;
        this.currentAnimation = animation;
        this.currentLocation = location;
    }

    public NpcSpec spec() { return spec; }
    public NpcMood mood() { return mood; }
    public NpcMemory memory() { return memory; }
    public String currentActivity() { return currentActivity; }
    public String currentAnimation() { return currentAnimation; }
    public String currentLocation() { return currentLocation; }
}
