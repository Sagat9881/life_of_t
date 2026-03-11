package ru.lifegame.backend.domain.model.character;

import ru.lifegame.backend.domain.balance.GameBalance;

public record JobInfo(
        String title,
        int satisfaction,
        int burnoutRisk
) {
    public JobInfo {
        // satisfaction is bounded [STAT_MIN, STAT_MAX] — same 0..100 scale as player stats
        satisfaction = clampSatisfaction(satisfaction);
        // burnoutRisk is bounded [0, BURNOUT_THRESHOLD] per game-balance.yml decay.burnout.threshold
        burnoutRisk  = Math.max(0, Math.min(GameBalance.BURNOUT_THRESHOLD, burnoutRisk));
    }

    public JobInfo changeSatisfaction(int delta) {
        return new JobInfo(title, satisfaction + delta, burnoutRisk);
    }

    public JobInfo changeBurnoutRisk(int delta) {
        return new JobInfo(title, satisfaction, burnoutRisk + delta);
    }

    public static JobInfo initial() {
        return new JobInfo(
                "Web-\u0434\u0438\u0437\u0430\u0439\u043d\u0435\u0440 (Tilda)",
                GameBalance.INITIAL_JOB_SATISFACTION,
                GameBalance.INITIAL_BURNOUT_RISK
        );
    }

    private static int clampSatisfaction(int value) {
        return Math.max(GameBalance.STAT_MIN, Math.min(GameBalance.STAT_MAX, value));
    }
}
