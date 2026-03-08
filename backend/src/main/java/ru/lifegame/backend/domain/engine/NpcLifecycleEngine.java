package ru.lifegame.backend.application.engine;

import com.sagat.life_of_t.domain.engine.runtime.NpcInstance;
import com.sagat.life_of_t.domain.engine.runtime.NpcUtilityBrain;
import com.sagat.life_of_t.domain.engine.runtime.NpcUtilityBrain.ScoredAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Drives NPC activity each game hour via Utility AI.
 * No knowledge of specific NPCs — works purely through specs and runtime state.
 */
public class NpcLifecycleEngine {
    private final NpcRegistry registry;
    private final NpcUtilityBrain brain = new NpcUtilityBrain();

    public NpcLifecycleEngine(NpcRegistry registry) {
        this.registry = registry;
    }

    public record NpcActivityUpdate(String entityId, String activity, String animation, String location) {}

    public List<NpcActivityUpdate> hourlyTick(int hour) {
        String timeOfDay = resolveTimeOfDay(hour);
        List<NpcActivityUpdate> updates = new ArrayList<>();

        for (NpcInstance npc : registry.allNpcs()) {
            ScoredAction chosen = brain.evaluate(npc, timeOfDay);
            npc.updateActivity(chosen.actionId(), chosen.location(), chosen.animation());
            updates.add(new NpcActivityUpdate(
                    npc.entityId(),
                    chosen.actionId(),
                    chosen.animation(),
                    chosen.location()
            ));
        }
        return updates;
    }

    public void dailyTick() {
        for (NpcInstance npc : registry.allNpcs()) {
            npc.dailyTick();
        }
    }

    public void onPlayerAction(String actionId, int day, int hour) {
        for (NpcInstance npc : registry.namedNpcs()) {
            npc.observePlayerAction(actionId, day, hour);
        }
    }

    public void onPlayerInteractsWithNpc(String npcId) {
        registry.get(npcId).ifPresent(NpcInstance::onPlayerInteraction);
    }

    private String resolveTimeOfDay(int hour) {
        if (hour >= 6 && hour < 12) return "morning";
        if (hour >= 12 && hour < 18) return "day";
        if (hour >= 18 && hour < 23) return "evening";
        return "night";
    }
}
