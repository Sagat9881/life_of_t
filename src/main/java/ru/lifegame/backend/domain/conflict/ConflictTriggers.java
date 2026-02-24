package ru.lifegame.backend.domain.conflict;

import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.*;

import java.util.*;

/**
 * Domain service that evaluates conditions and triggers conflicts.
 * Part of the conflict subdomain.
 */
public class ConflictTriggers {

    public List<Conflict> checkTriggers(PlayerCharacter player, Relationships relationships, GameTime time) {
        List<Conflict> triggered = new ArrayList<>();
        checkHusbandConflicts(player, relationships, triggered);
        checkFatherConflicts(player, relationships, triggered);
        checkInternalConflicts(player, relationships, triggered);
        return triggered;
    }

    private void checkHusbandConflicts(PlayerCharacter player, Relationships relationships, List<Conflict> result) {
        Relationship husband = relationships.get(NpcCode.HUSBAND);
        if (husband == null || husband.broken()) return;

        if (husband.closeness() < GameBalance.HUSBAND_CLOSENESS_HOUSEHOLD
                && player.daysSinceHousehold() >= GameBalance.HUSBAND_DAYS_NO_HOUSEHOLD) {
            result.add(createConflict(HusbandConflicts.HOUSEHOLD_DUTIES));
        }
        if (husband.closeness() < GameBalance.HUSBAND_CLOSENESS_ATTENTION) {
            result.add(createConflict(HusbandConflicts.LACK_OF_ATTENTION));
        }
        if (husband.romance() < GameBalance.HUSBAND_ROMANCE_CRISIS
                && player.consecutiveWorkDays() >= GameBalance.HUSBAND_CONSECUTIVE_WORK_DAYS) {
            result.add(createConflict(HusbandConflicts.ROMANTIC_CRISIS));
        }
        if (player.stats().money() < GameBalance.HUSBAND_MONEY_LOW
                || player.stats().money() > GameBalance.HUSBAND_MONEY_HIGH) {
            result.add(createConflict(HusbandConflicts.FINANCIAL_DISAGREEMENT));
        }
    }

    private void checkFatherConflicts(PlayerCharacter player, Relationships relationships, List<Conflict> result) {
        Relationship father = relationships.get(NpcCode.FATHER);
        if (father == null || father.broken()) return;

        if (father.closeness() < GameBalance.FATHER_CLOSENESS_NEGLECTED) {
            result.add(createConflict(FatherConflicts.FEELING_NEGLECTED));
        }
        if (player.job().satisfaction() < GameBalance.FATHER_CRITICISM_SATISFACTION
                || player.stats().selfEsteem() < GameBalance.FATHER_CRITICISM_SELF_ESTEEM) {
            result.add(createConflict(FatherConflicts.CRITICISM_OF_CHOICES));
        }
        if (player.stats().money() < GameBalance.FATHER_CONCERN_MONEY
                || player.stats().health() < GameBalance.FATHER_CONCERN_HEALTH) {
            result.add(createConflict(FatherConflicts.CONCERN_FOR_WELLBEING));
        }
    }

    private void checkInternalConflicts(PlayerCharacter player, Relationships relationships, List<Conflict> result) {
        if (player.stats().selfEsteem() < GameBalance.INTERNAL_IDENTITY_SELF_ESTEEM
                && player.job().satisfaction() < GameBalance.INTERNAL_IDENTITY_SATISFACTION) {
            result.add(createConflict(InternalConflicts.IDENTITY_CRISIS));
        }
        if (player.job().burnoutRisk() > GameBalance.INTERNAL_BURNOUT_RISK
                || player.stats().stress() > GameBalance.INTERNAL_BURNOUT_STRESS) {
            result.add(createConflict(InternalConflicts.BURNOUT));
        }
        if (relationships.totalCloseness() < GameBalance.INTERNAL_GUILT_CLOSENESS_SUM
                && player.stats().selfEsteem() < GameBalance.INTERNAL_GUILT_SELF_ESTEEM) {
            result.add(createConflict(InternalConflicts.GUILT));
        }
    }

    private Conflict createConflict(ConflictType type) {
        return new Conflict(UUID.randomUUID().toString(), type);
    }
}
