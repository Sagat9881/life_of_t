package ru.lifegame.backend.domain.model.character;

import ru.lifegame.backend.domain.action.ActionType;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.common.PlayerId;
import ru.lifegame.backend.domain.model.session.GameTime;
import ru.lifegame.backend.domain.model.stats.StatChanges;
import ru.lifegame.backend.domain.model.stats.Stats;

import java.util.*;

public class PlayerCharacter {

    private final PlayerId id;
    private final String name;
    private Stats stats;
    private JobInfo job;
    private String location;
    private final Map<String, Boolean> tags;
    private Skills skills;
    private final List<String> inventory;

    // ── Daily state flags (reset at end of each day) ─────────────────────────
    // restedToday  — set by REST action; controls energy decay
    // workedToday  — set by WORK action; controls consecutiveWorkDays counter
    private boolean restedToday;
    private boolean workedToday;

    // ── Cumulative counters (used by conflict-trigger system) ─────────────────
    // consecutiveWorkDays  — increments each day workedToday=true, resets on rest day
    //                        used by: HUSBAND_CONSECUTIVE_WORK_DAYS trigger
    // daysSinceHousehold   — increments every day, reset via resetHouseholdDays()
    //                        used by: HUSBAND_DAYS_NO_HOUSEHOLD trigger
    private int consecutiveWorkDays;
    private int daysSinceHousehold;

    public PlayerCharacter(PlayerId id, String name, Stats stats, JobInfo job,
                           String location, Map<String, Boolean> tags,
                           Skills skills, List<String> inventory) {
        this.id = id;
        this.name = name;
        this.stats = stats;
        this.job = job;
        this.location = location;
        this.tags = new HashMap<>(tags);
        this.skills = skills;
        this.inventory = new ArrayList<>(inventory);
        this.restedToday = false;
        this.workedToday = false;
        this.consecutiveWorkDays = 0;
        this.daysSinceHousehold = 0;
    }

    /**
     * Whether the player has enough time and energy to perform the given action.
     *
     * The minimum energy threshold of 5 is intentionally kept low to allow
     * the player to "drag" to the end of the day.
     * TODO: promote magic number 5 to GameBalance.ENERGY_ACTION_MIN once
     *       game-balance.yml defines a player.energyActionMin entry.
     */
    public boolean canPerformAction(ActionType action, GameTime time, int timeCost) {
        if (!time.hasEnoughTime(timeCost)) return false;
        return stats.energy() >= 5;
    }

    public void applyStatChanges(StatChanges changes) {
        this.stats = stats.apply(changes);
    }

    /**
     * Applies end-of-day decay rules from game-balance.yml (section: decay).
     *
     * Order is deterministic and testable:
     *   1. Energy penalty when no rest taken today           (decay.energy.noRest)
     *   2. Stress cascade when stress exceeds high threshold (decay.stress.*)
     *      2a. Stress grows                                  (decay.stress.dailyIncrease)
     *      2b. Mood drops due to high stress                 (decay.mood.dailyDecreaseHighStress)
     *      2c. Burnout risk grows                            (decay.burnout.dailyIncrease)
     *   3. Consecutive work days counter update
     *   4. Days-since-household counter advances
     *   5. Flags reset for the next day
     */
    public void applyEndOfDayDecay() {
        // 1. Energy decay if player did not rest today
        if (!restedToday) {
            stats = stats.changeEnergy(-GameBalance.ENERGY_DECAY_NO_REST);
        }

        // 2. Stress cascade (threshold check uses the value AFTER step 1)
        if (stats.stress() > GameBalance.STRESS_HIGH_THRESHOLD) {
            stats = stats.changeStress(GameBalance.STRESS_DAILY_INCREASE);
            stats = stats.changeMood(-GameBalance.MOOD_DAILY_DECREASE_HIGH_STRESS);
            job   = job.changeBurnoutRisk(GameBalance.BURNOUT_RISK_DAILY_INCREASE);
        }

        // 3. Consecutive work days
        if (workedToday) {
            consecutiveWorkDays++;
        } else {
            consecutiveWorkDays = 0;
        }

        // 4. Household neglect counter (reset explicitly via resetHouseholdDays())
        daysSinceHousehold++;

        // 5. Reset daily flags
        restedToday = false;
        workedToday = false;
    }

    // ── Daily flag setters ────────────────────────────────────────────────────
    public void markRested()           { this.restedToday = true; }
    public void markWorked()           { this.workedToday = true; }
    public void resetHouseholdDays()   { this.daysSinceHousehold = 0; }

    // ── Crisis / state checks (thresholds from game-balance.yml) ─────────────

    /** True when burnout risk has reached the maximum threshold. */
    public boolean isBurnedOut() {
        return job.burnoutRisk() >= GameBalance.BURNOUT_THRESHOLD;
    }

    /**
     * True when Tatiana is in an internal identity crisis:
     * both self-esteem and job satisfaction are critically low.
     * Thresholds: conflict.internal.identity.selfEsteem / satisfaction
     */
    public boolean isInInternalCrisis() {
        return stats.selfEsteem()    < GameBalance.INTERNAL_IDENTITY_SELF_ESTEEM
                && job.satisfaction() < GameBalance.INTERNAL_IDENTITY_SATISFACTION;
    }

    /**
     * True when Tatiana is at risk of burnout due to extreme stress.
     * Thresholds: conflict.internal.burnout.risk / stress
     */
    public boolean isAtBurnoutRisk() {
        return job.burnoutRisk() >= GameBalance.INTERNAL_BURNOUT_RISK
                && stats.stress() >= GameBalance.INTERNAL_BURNOUT_STRESS;
    }

    /** True when money has fallen below the bankruptcy threshold. */
    public boolean isBankrupt() {
        return stats.money() < GameBalance.BANKRUPTCY_THRESHOLD;
    }

    // ── Mutation helpers ─────────────────────────────────────────────────────
    public void improveSkill(String skill, int delta) {
        this.skills = skills.improve(skill, delta);
    }

    public void changeJobSatisfaction(int delta) {
        this.job = job.changeSatisfaction(delta);
    }

    public void changeJobBurnoutRisk(int delta) {
        this.job = job.changeBurnoutRisk(delta);
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public PlayerId id()                   { return id; }
    public String name()                   { return name; }
    public Stats stats()                   { return stats; }
    public JobInfo job()                   { return job; }
    public String location()               { return location; }
    public Map<String, Boolean> tags()     { return Collections.unmodifiableMap(tags); }
    public Skills skills()                 { return skills; }
    public List<String> inventory()        { return Collections.unmodifiableList(inventory); }
    public int consecutiveWorkDays()       { return consecutiveWorkDays; }
    public int daysSinceHousehold()        { return daysSinceHousehold; }

    public static PlayerCharacter initial() {
        return new PlayerCharacter(
                PlayerId.generate(),
                "\u0422\u0430\u0442\u044c\u044f\u043d\u0430",
                Stats.initial(),
                JobInfo.initial(),
                "HOME",
                Map.of("loves_cats", true, "efficient_worker", false),
                Skills.initial(),
                List.of("laptop", "smartphone")
        );
    }
}
