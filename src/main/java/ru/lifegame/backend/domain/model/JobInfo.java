package ru.lifegame.backend.domain.model;

import ru.lifegame.backend.domain.balance.GameBalance;

public record JobInfo(
        String title,
        int satisfaction,
        int burnoutRisk
) {
    public JobInfo {
        satisfaction = clamp(satisfaction);
        burnoutRisk = Math.max(0, Math.min(burnoutRisk, GameBalance.BURNOUT_THRESHOLD));
    }

    public JobInfo changeSatisfaction(int delta) {
        return new JobInfo(title, satisfaction + delta, burnoutRisk);
    }

    public JobInfo changeBurnoutRisk(int delta) {
        return new JobInfo(title, satisfaction, burnoutRisk + delta);
    }

    public static JobInfo initial() {
        return new JobInfo(
                "Web-дизайнер (Tilda)",
                GameBalance.INITIAL_JOB_SATISFACTION,
                GameBalance.INITIAL_BURNOUT_RISK
        );
    }

    private static int clamp(int value) {
        return Math.max(GameBalance.STAT_MIN, Math.min(GameBalance.STAT_MAX, value));
    }
}
