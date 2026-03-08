package ru.lifegame.backend.application.engine.runtime;

import com.sagat.life_of_t.domain.engine.spec.NpcSpec;

/**
 * A live NPC instance = spec + runtime state.
 * Named NPCs get full mood (6 axes) + memory.
 * Filler NPCs get minimal mood (2 axes) + no memory.
 */
public class NpcInstance {
    private final NpcSpec spec;
    private final NpcMoodState mood;
    private final NpcMemoryLog memory;
    private String currentActivity;
    private String currentLocation;
    private String currentAnimation;

    private NpcInstance(NpcSpec spec, NpcMoodState mood, NpcMemoryLog memory) {
        this.spec = spec;
        this.mood = mood;
        this.memory = memory;
        this.currentActivity = "idle";
        this.currentLocation = "unknown";
        this.currentAnimation = "idle";
    }

    public static NpcInstance createNamed(NpcSpec spec) {
        return new NpcInstance(
                spec,
                NpcMoodState.fromTraits(spec.personalityTraits()),
                new NpcMemoryLog()
        );
    }

    public static NpcInstance createFiller(NpcSpec spec) {
        return new NpcInstance(
                spec,
                NpcMoodState.fillerDefaults(),
                null
        );
    }

    public String entityId() { return spec.entityId(); }
    public NpcSpec spec() { return spec; }
    public NpcMoodState mood() { return mood; }
    public boolean hasMemory() { return memory != null; }
    public NpcMemoryLog memory() { return memory; }

    public String currentActivity() { return currentActivity; }
    public String currentLocation() { return currentLocation; }
    public String currentAnimation() { return currentAnimation; }

    public void updateActivity(String activity, String location, String animation) {
        this.currentActivity = activity;
        this.currentLocation = location;
        this.currentAnimation = animation;
    }

    public void observePlayerAction(String actionId, int day, int hour) {
        if (memory != null) memory.observe(actionId, day, hour);
    }

    public void onPlayerInteraction() {
        mood.onPlayerInteraction();
    }

    public void dailyTick() {
        mood.dailyTick();
    }
}
