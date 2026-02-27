package ru.lifegame.backend.domain.balance;

public final class GameBalance {
    private GameBalance() {}

    // --- Stats ---
    public static final int STAT_MIN = 0;
    public static final int STAT_MAX = 100;
    public static final int INITIAL_ENERGY = 100;
    public static final int INITIAL_HEALTH = 100;
    public static final int INITIAL_STRESS = 0;
    public static final int INITIAL_MOOD = 80;
    public static final int INITIAL_MONEY = 1000;
    public static final int INITIAL_SELF_ESTEEM = 70;
    public static final int INITIAL_JOB_SATISFACTION = 50;
    public static final int INITIAL_BURNOUT_RISK = 20;

    // --- Player decay ---
    public static final int ENERGY_DECAY_NO_REST = 20;
    public static final int STRESS_HIGH_THRESHOLD = 50;
    public static final int STRESS_DAILY_INCREASE = 5;
    public static final int MOOD_DAILY_DECREASE_HIGH_STRESS = 10;
    public static final int BURNOUT_RISK_DAILY_INCREASE = 5;
    public static final int BURNOUT_THRESHOLD = 100;
    public static final int BANKRUPTCY_THRESHOLD = -500;

    // --- Relationships initial ---
    public static final int HUSBAND_INITIAL_CLOSENESS = 80;
    public static final int HUSBAND_INITIAL_TRUST = 75;
    public static final int HUSBAND_INITIAL_STABILITY = 70;
    public static final int HUSBAND_INITIAL_ROMANCE = 85;
    public static final int FATHER_INITIAL_CLOSENESS = 60;
    public static final int FATHER_INITIAL_TRUST = 90;
    public static final int FATHER_INITIAL_STABILITY = 80;

    // --- Relationships decay ---
    public static final int NO_INTERACTION_DAYS_MILD = 3;
    public static final int NO_INTERACTION_DAYS_SEVERE = 7;
    public static final int DECAY_MILD_CLOSENESS = 5;
    public static final int DECAY_MILD_TRUST = 3;
    public static final int DECAY_SEVERE_CLOSENESS = 15;
    public static final int DECAY_SEVERE_TRUST = 10;
    public static final int ISOLATION_CLOSENESS_SUM = 20;
    public static final int TRUST_CRITICAL_FOR_BREAK = 10;

    // --- Pets initial ---
    public static final int PET_INITIAL_SATIETY = 70;
    public static final int PET_INITIAL_ATTENTION = 50;
    public static final int PET_INITIAL_HEALTH = 90;
    public static final int PET_INITIAL_MOOD = 80;
    public static final int PET_SAM_INITIAL_SATIETY = 80;
    public static final int PET_SAM_INITIAL_ATTENTION = 60;
    public static final int PET_SAM_INITIAL_HEALTH = 95;
    public static final int PET_SAM_INITIAL_MOOD = 85;

    // --- Pets decay ---
    public static final int PET_DAILY_SATIETY_DECAY = 10;
    public static final int PET_DAILY_ATTENTION_DECAY = 15;
    public static final int PET_LOW_SATIETY_THRESHOLD = 20;
    public static final int PET_HEALTH_DECAY_LOW_SATIETY = 10;
    public static final int PET_MOOD_DECAY_LOW_SATIETY = 20;

    // --- Conflict ---
    public static final int CSP_BASE = 50;
    public static final int INITIAL_CSP = 50;
    public static final int MAX_CONFLICT_ROUNDS = 3;

    // --- Conflict triggers ---
    public static final int HUSBAND_CLOSENESS_HOUSEHOLD = 50;
    public static final int HUSBAND_DAYS_NO_HOUSEHOLD = 5;
    public static final int HUSBAND_CLOSENESS_ATTENTION = 40;
    public static final int HUSBAND_DAYS_NO_ATTENTION = 3;
    public static final int HUSBAND_ROMANCE_CRISIS = 30;
    public static final int HUSBAND_CONSECUTIVE_WORK_DAYS = 5;
    public static final int HUSBAND_MONEY_LOW = 500;
    public static final int HUSBAND_MONEY_HIGH = 5000;
    public static final int FATHER_CLOSENESS_NEGLECTED = 30;
    public static final int FATHER_DAYS_NO_VISIT = 7;
    public static final int FATHER_CRITICISM_SATISFACTION = 30;
    public static final int FATHER_CRITICISM_SELF_ESTEEM = 30;
    public static final int FATHER_CONCERN_MONEY = 200;
    public static final int FATHER_CONCERN_HEALTH = 40;
    public static final int INTERNAL_IDENTITY_SELF_ESTEEM = 20;
    public static final int INTERNAL_IDENTITY_SATISFACTION = 30;
    public static final int INTERNAL_BURNOUT_RISK = 70;
    public static final int INTERNAL_BURNOUT_STRESS = 80;
    public static final int INTERNAL_GUILT_CLOSENESS_SUM = 100;
    public static final int INTERNAL_GUILT_SELF_ESTEEM = 40;

    // --- Tactic CSP deltas ---
    public static final int SURRENDER_PLAYER_CSP = 10;
    public static final int SURRENDER_OPPONENT_CSP = -20;
    public static final int SURRENDER_SELF_ESTEEM = -5;
    public static final int SURRENDER_CLOSENESS = 5;
    public static final int SURRENDER_TRUST = -5;

    public static final int ASSERT_PLAYER_CSP = -15;
    public static final int ASSERT_OPPONENT_CSP = 15;
    public static final int ASSERT_SELF_ESTEEM = 10;
    public static final int ASSERT_CLOSENESS = -10;
    public static final int ASSERT_TRUST = 5;

    public static final int COMPROMISE_PLAYER_CSP = -10;
    public static final int COMPROMISE_OPPONENT_CSP = -10;

    public static final int AVOID_PLAYER_CSP = -5;
    public static final int AVOID_OPPONENT_CSP = 20;
    public static final int AVOID_CLOSENESS = -5;
    public static final int AVOID_TRUST = -10;

    public static final int LISTEN_PLAYER_CSP = -5;
    public static final int LISTEN_OPPONENT_CSP = -25;
    public static final int LISTEN_CLOSENESS = 15;
    public static final int LISTEN_TRUST = 10;

    public static final int HUMOR_PLAYER_CSP = -10;
    public static final int HUMOR_OPPONENT_CSP = -15;
    public static final int HUMOR_SUCCESS_CHANCE = 80;

    public static final int LOGICAL_PLAYER_CSP = -10;
    public static final int LOGICAL_OPPONENT_CSP = -20;
    public static final int LOGICAL_TRUST = 20;

    public static final int EMOTIONAL_PLAYER_CSP = -5;
    public static final int EMOTIONAL_OPPONENT_CSP = -30;
    public static final int EMOTIONAL_CLOSENESS = 20;
    public static final int EMOTIONAL_ROMANCE = 15;

    public static final int BOUNDARIES_PLAYER_CSP = -20;
    public static final int BOUNDARIES_OPPONENT_CSP = -10;
    public static final int BOUNDARIES_SELF_ESTEEM = 15;
    public static final int BOUNDARIES_TRUST = 15;

    // --- Action time costs ---
    public static final int WORK_TIME_COST = 8;
    public static final int VISIT_FATHER_TIME_COST = 4;
    public static final int DATE_HUSBAND_TIME_COST = 3;
    public static final int PLAY_CAT_TIME_COST = 1;
    public static final int WALK_DOG_TIME_COST = 2;
    public static final int SELF_CARE_TIME_COST = 2;
    public static final int REST_TIME_COST = 3;
    public static final int HOUSEHOLD_TIME_COST = 2;

    // --- Action stat effects ---
    public static final int WORK_ENERGY = -30;
    public static final int WORK_STRESS = 20;
    public static final int WORK_MOOD = -5;
    public static final int WORK_MONEY = 55;

    public static final int VISIT_FATHER_ENERGY = -15;
    public static final int VISIT_FATHER_MOOD = 10;
    public static final int VISIT_FATHER_CLOSENESS = 15;
    public static final int VISIT_FATHER_TRUST = 5;

    public static final int DATE_ENERGY = -10;
    public static final int DATE_MOOD = 20;
    public static final int DATE_STRESS = -10;
    public static final int DATE_CLOSENESS = 10;
    public static final int DATE_ROMANCE = 15;

    public static final int PLAY_CAT_ENERGY = -5;
    public static final int PLAY_CAT_MOOD = 10;
    public static final int PLAY_CAT_STRESS = -5;
    public static final int PLAY_CAT_ATTENTION = 30;
    public static final int PLAY_CAT_PET_MOOD = 20;

    public static final int WALK_DOG_ENERGY = -10;
    public static final int WALK_DOG_HEALTH = 5;
    public static final int WALK_DOG_MOOD = 10;
    public static final int WALK_DOG_STRESS = -10;
    public static final int WALK_DOG_ATTENTION = 25;
    public static final int WALK_DOG_PET_MOOD = 25;

    public static final int SELF_CARE_ENERGY = -10;
    public static final int SELF_CARE_MOOD = 15;
    public static final int SELF_CARE_STRESS = -15;
    public static final int SELF_CARE_SELF_ESTEEM = 10;

    public static final int REST_ENERGY = 40;
    public static final int REST_STRESS = -20;
    public static final int REST_MOOD = 10;

    public static final int HOUSEHOLD_ENERGY = -15;
    public static final int HOUSEHOLD_MOOD = -5;

    // --- Time ---
    public static final int DAY_START_HOUR = 8;
    public static final int DAY_END_HOUR = 24;
    public static final int HOURS_PER_DAY = 24;

    // --- Game limits ---
    public static final int MAX_GAME_DAYS = 30;
}
