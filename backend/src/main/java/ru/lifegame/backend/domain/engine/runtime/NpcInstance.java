package ru.lifegame.backend.domain.engine.runtime;

import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.npc.engine.NpcMood;
import ru.lifegame.backend.domain.npc.engine.NpcMemory;

public class NpcInstance {
    private final NpcSpec spec;
    private NpcMood mood;
    private final NpcMemory memory;
    private String currentActivity;
    private String currentLocation;
    private String currentAnimation;

    public NpcInstance(NpcSpec spec, NpcMood mood, NpcMemory memory) {
        this.spec = spec;
        this.mood = mood;
        this.memory = memory;
        this.currentActivity = "idle";
        this.currentLocation = "home";
        this.currentAnimation = "idle";
    }

    public static NpcInstance createNamed(NpcSpec spec) {
        NpcMood mood = NpcMood.fromInitialValues(spec.moodInitial());
        NpcMemory memory = new NpcMemory(spec.shortTermSize());
        return new NpcInstance(spec, mood, memory);
    }

    public static NpcInstance createFiller(NpcSpec spec) {
        NpcMood mood = NpcMood.fromInitialValues(spec.moodInitial());
        NpcMemory memory = new NpcMemory(0);
        return new NpcInstance(spec, mood, memory);
    }

    public void updateActivity(String activity, String location, String animation) {
        this.currentActivity = activity;
        this.currentLocation = location;
        this.currentAnimation = animation;
    }

    public NpcSpec spec() { return spec; }
    public NpcMood mood() { return mood; }
    public void setMood(NpcMood mood) { this.mood = mood; }
    public NpcMemory memory() { return memory; }
    public String currentActivity() { return currentActivity; }
    public String currentLocation() { return currentLocation; }
    public String currentAnimation() { return currentAnimation; }
}
