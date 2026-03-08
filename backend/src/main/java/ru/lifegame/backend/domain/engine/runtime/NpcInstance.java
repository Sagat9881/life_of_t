package ru.lifegame.backend.domain.engine.runtime;

import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.npc.engine.NpcMood;
import ru.lifegame.backend.domain.npc.engine.NpcMemory;

public class NpcInstance {

    private final NpcSpec spec;
    private final NpcMood mood;
    private final NpcMemory memory;
    private String currentActivityId;
    private String currentAnimation;
    private String currentLocation;

    public NpcInstance(NpcSpec spec, NpcMood mood, NpcMemory memory) {
        this.spec = spec;
        this.mood = mood;
        this.memory = memory;
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
        NpcMemory memory = new NpcMemory(memoryEnabled ? 10 : 0);
        return new NpcInstance(spec, mood, memory);
    }

    public void setCurrentActivity(String activityId, String animation, String location) {
        this.currentActivityId = activityId;
        this.currentAnimation = animation;
        this.currentLocation = location;
    }

    public NpcSpec spec() { return spec; }
    public NpcMood getMood() { return mood; }
    public NpcMemory getMemory() { return memory; }
    public String getCurrentActivityId() { return currentActivityId; }
    public String getCurrentAnimation() { return currentAnimation; }
    public String getCurrentLocation() { return currentLocation; }
}
