package ru.lifegame.backend.domain.model.character;

import ru.lifegame.backend.domain.action.ActionType;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.conflict.tactics.BaseConflictTactics;
import ru.lifegame.backend.domain.conflict.tactics.ConflictTactic;
import ru.lifegame.backend.domain.conflict.tactics.SkillBasedConflictTactics;
import ru.lifegame.backend.domain.model.common.Location;
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
    private Location location;
    private final Map<String, Boolean> tags;
    private Skills skills;
    private final List<String> inventory;
    private boolean restedToday;
    private boolean workedToday;
    private int consecutiveWorkDays;
    private int daysSinceHousehold;

    public PlayerCharacter(PlayerId id, String name, Stats stats, JobInfo job,
                           Location location, Map<String, Boolean> tags,
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

    public boolean canPerformAction(ActionType action, GameTime time, int timeCost) {
        if (!time.hasEnoughTime(timeCost)) return false;
        if (stats.energy() < 5) return false;
        return true;
    }

    public boolean canUseTactic(ConflictTactic tactic) {
        if (tactic.isBaseAvailable()) return true;
        return tactic.requiredSkill()
                .map(skill -> skills.hasLevel(skill, tactic.requiredSkillLevel()))
                .orElse(true);
    }

    public List<ConflictTactic> availableConflictTactics() {
        List<ConflictTactic> result = new ArrayList<>(Arrays.asList(BaseConflictTactics.values()));
        for (SkillBasedConflictTactics t : SkillBasedConflictTactics.values()) {
            if (canUseTactic(t)) result.add(t);
        }
        return result;
    }

    public void applyStatChanges(StatChanges changes) {
        this.stats = stats.apply(changes);
    }

    public void applyEndOfDayDecay() {
        if (!restedToday) {
            stats = stats.changeEnergy(-GameBalance.ENERGY_DECAY_NO_REST);
        }
        if (stats.stress() > GameBalance.STRESS_HIGH_THRESHOLD) {
            stats = stats.changeStress(GameBalance.STRESS_DAILY_INCREASE);
            stats = stats.changeMood(-GameBalance.MOOD_DAILY_DECREASE_HIGH_STRESS);
            job = job.changeBurnoutRisk(GameBalance.BURNOUT_RISK_DAILY_INCREASE);
        }
        if (workedToday) {
            consecutiveWorkDays++;
        } else {
            consecutiveWorkDays = 0;
        }
        daysSinceHousehold++;
        restedToday = false;
        workedToday = false;
    }

    public void markRested() { this.restedToday = true; }
    public void markWorked() { this.workedToday = true; }
    public void resetHouseholdDays() { this.daysSinceHousehold = 0; }

    public boolean isBurnedOut() {
        return job.burnoutRisk() >= GameBalance.BURNOUT_THRESHOLD;
    }

    public boolean isInInternalCrisis() {
        return stats.selfEsteem() < GameBalance.INTERNAL_IDENTITY_SELF_ESTEEM
                && job.satisfaction() < GameBalance.INTERNAL_IDENTITY_SATISFACTION;
    }

    public boolean isBankrupt() {
        return stats.money() < GameBalance.BANKRUPTCY_THRESHOLD;
    }

    public void improveSkill(String skill, int delta) {
        this.skills = skills.improve(skill, delta);
    }

    public void setLocation(Location location) { this.location = location; }

    // --- Getters ---
    public PlayerId id() { return id; }
    public String name() { return name; }
    public Stats stats() { return stats; }
    public JobInfo job() { return job; }
    public Location location() { return location; }
    public Map<String, Boolean> tags() { return Collections.unmodifiableMap(tags); }
    public Skills skills() { return skills; }
    public List<String> inventory() { return Collections.unmodifiableList(inventory); }
    public int consecutiveWorkDays() { return consecutiveWorkDays; }
    public int daysSinceHousehold() { return daysSinceHousehold; }

    public static PlayerCharacter initial() {
        return new PlayerCharacter(
                PlayerId.generate(),
                "Татьяна",
                Stats.initial(),
                JobInfo.initial(),
                Location.HOME,
                Map.of("loves_cats", true, "efficient_worker", false),
                Skills.initial(),
                List.of("laptop", "smartphone")
        );
    }
}
