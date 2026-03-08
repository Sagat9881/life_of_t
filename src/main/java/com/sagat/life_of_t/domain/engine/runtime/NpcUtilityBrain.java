package com.sagat.life_of_t.domain.engine.runtime;

import com.sagat.life_of_t.domain.engine.spec.NpcSpec;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToDoubleFunction;

/**
 * Utility AI: scores all available actions for an NPC and picks the best.
 * No hardcoded actions — scoring functions are built from NPC spec traits.
 */
public class NpcUtilityBrain {

    public record ScoredAction(String actionId, String animation, String location, double score) {}

    public ScoredAction evaluate(NpcInstance npc, String timeOfDay) {
        List<ScoredAction> candidates = new ArrayList<>();

        NpcSpec spec = npc.spec();
        NpcMoodState mood = npc.mood();

        for (NpcSpec.ScheduleSlot slot : spec.schedules()) {
            if (!slot.timeOfDay().equals(timeOfDay)) continue;

            for (NpcSpec.ActionSpec action : slot.actions()) {
                double score = scoreAction(action, mood, spec);
                candidates.add(new ScoredAction(
                        action.type(),
                        action.value(),
                        action.target(),
                        score
                ));
            }
        }

        if (candidates.isEmpty()) {
            return new ScoredAction("idle", "idle", "unknown", 0);
        }

        addMoodOverrides(candidates, mood);

        return candidates.stream()
                .max(Comparator.comparingDouble(ScoredAction::score))
                .orElse(new ScoredAction("idle", "idle", "unknown", 0));
    }

    private double scoreAction(NpcSpec.ActionSpec action, NpcMoodState mood, NpcSpec spec) {
        double base = action.probability();

        if ("move".equals(action.type())) {
            base *= 1.2;
        } else if ("idle_animation".equals(action.type())) {
            base *= 1.0;
            if (mood.energy() < 30) base *= 1.5;
        } else if ("dialogue-chance".equals(action.type())) {
            base *= action.probability();
            if (mood.loneliness() > 50) base *= 1.4;
            if (mood.irritability() > 60) base *= 0.5;
            if (mood.happiness() > 70) base *= 1.3;
        }

        int awareness = spec.trait("awareness");
        int friendliness = spec.trait("friendliness");
        base *= (1.0 + friendliness / 200.0);
        base *= (1.0 + awareness / 300.0);

        return base;
    }

    private void addMoodOverrides(List<ScoredAction> candidates, NpcMoodState mood) {
        if (mood.irritability() > 70) {
            candidates.add(new ScoredAction("mood_override", "leave_room", "hallway", 2.5));
        }
        if (mood.loneliness() > 75) {
            candidates.add(new ScoredAction("mood_override", "seek_player", "tanya_location", 2.0));
        }
        if (mood.energy() < 20) {
            candidates.add(new ScoredAction("mood_override", "sleep", "bed", 3.0));
        }
        if (mood.anxiety() > 80) {
            candidates.add(new ScoredAction("mood_override", "hide", "safe_spot", 2.8));
        }
    }
}
