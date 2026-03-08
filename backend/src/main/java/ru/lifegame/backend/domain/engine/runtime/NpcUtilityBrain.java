package ru.lifegame.backend.domain.engine.runtime;

import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.engine.spec.NpcSpec.ActionSpec;
import ru.lifegame.backend.domain.engine.spec.NpcSpec.ScheduleSlot;
import ru.lifegame.backend.domain.npc.engine.NpcMood;

import java.util.*;

public class NpcUtilityBrain {

    public record ScoredResult(
        String actionId,
        String animation,
        String location,
        double score
    ) {}

    public Optional<ScoredResult> evaluate(NpcInstance npc, int currentHour, Map<String, Object> gameContext) {
        NpcSpec spec = npc.spec();
        NpcMood mood = npc.getMood();

        // 1. Check schedule first
        Optional<ScheduleSlot> scheduledSlot = spec.schedule().stream()
            .filter(s -> currentHour >= s.start() && currentHour < s.end())
            .findFirst();

        // 2. Check mood overrides
        if (mood.getIrritability() > 70 || mood.getLoneliness() > 70) {
            Optional<ScoredResult> moodAction = evaluateMoodDrivenActions(npc, mood, gameContext);
            if (moodAction.isPresent()) return moodAction;
        }

        // 3. Use scheduled activity
        if (scheduledSlot.isPresent()) {
            ScheduleSlot slot = scheduledSlot.get();
            return Optional.of(new ScoredResult(
                slot.activity(), slot.animation(), slot.location(), 1.0
            ));
        }

        return Optional.of(new ScoredResult("idle", "idle", "default", 0.1));
    }

    private Optional<ScoredResult> evaluateMoodDrivenActions(NpcInstance npc, NpcMood mood, Map<String, Object> gameContext) {
        List<ActionSpec> actions = npc.spec().actions();
        if (actions == null || actions.isEmpty()) return Optional.empty();

        ScoredResult best = null;
        double bestScore = 0;

        for (ActionSpec action : actions) {
            double score = action.baseScore();
            // Mood-based scoring
            if (mood.getLoneliness() > 50) score += 0.3;
            if (mood.getIrritability() > 60) score -= 0.2;
            if (mood.getEnergy() < 30) score -= 0.3;

            if (score > bestScore) {
                bestScore = score;
                best = new ScoredResult(
                    action.id(), action.animation(), action.location(), score
                );
            }
        }
        return Optional.ofNullable(best);
    }
}
