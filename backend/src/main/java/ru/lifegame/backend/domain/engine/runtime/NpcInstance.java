package ru.lifegame.backend.domain.engine.runtime;

import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.npc.engine.NpcMood;
import ru.lifegame.backend.domain.npc.engine.NpcMemory;

import java.util.Optional;

public class NpcInstance {

    private final NpcSpec spec;
    private final NpcMood mood;
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

    public Optional<NpcSpec.ScheduleSlot> getScheduledActivity(int hour) {
        return spec.schedule().stream()
                .filter(s -> hour >= s.start() && hour < s.end())
                .findFirst();
    }

    public NpcSpec spec() { return spec; }
    public NpcMood mood() { return mood; }
    public NpcMemory memory() { return memory; }
    public String currentActivity() { return currentActivity; }
    public String currentLocation() { return currentLocation; }
    public String currentAnimation() { return currentAnimation; }

    public void setCurrentActivity(String activity) { this.currentActivity = activity; }
    public void setCurrentLocation(String location) { this.currentLocation = location; }
    public void setCurrentAnimation(String animation) { this.currentAnimation = animation; }
}
