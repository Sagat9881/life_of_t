package ru.lifegame.backend.domain.model.relationship;

import ru.lifegame.backend.domain.balance.GameBalance;

public record Relationship(
        NpcCode npcCode,
        int closeness,
        int trust,
        int stability,
        int romance,
        int lastInteractionDay,
        boolean broken
) {
    public Relationship {
        closeness = clamp(closeness);
        trust = clamp(trust);
        stability = clamp(stability);
        romance = clamp(romance);
    }

    public Relationship applyChanges(RelationshipChanges c) {
        return new Relationship(
                npcCode,
                closeness + c.closeness(),
                trust + c.trust(),
                stability + c.stability(),
                romance + c.romance(),
                lastInteractionDay,
                broken
        );
    }

    public Relationship markInteraction(int currentDay) {
        return new Relationship(npcCode, closeness, trust, stability, romance, currentDay, broken);
    }

    public Relationship applyDecay(int currentDay) {
        int gap = currentDay - lastInteractionDay;
        if (gap < GameBalance.NO_INTERACTION_DAYS_MILD) {
            return this;
        }
        if (gap < GameBalance.NO_INTERACTION_DAYS_SEVERE) {
            return new Relationship(npcCode,
                    closeness - GameBalance.DECAY_MILD_CLOSENESS,
                    trust - GameBalance.DECAY_MILD_TRUST,
                    stability, romance, lastInteractionDay, broken);
        }
        return new Relationship(npcCode,
                closeness - GameBalance.DECAY_SEVERE_CLOSENESS,
                trust - GameBalance.DECAY_SEVERE_TRUST,
                stability, romance, lastInteractionDay, broken);
    }

    public Relationship breakRelationship() {
        return new Relationship(npcCode, closeness, trust, stability, romance, lastInteractionDay, true);
    }

    private static int clamp(int value) {
        return Math.max(GameBalance.STAT_MIN, Math.min(GameBalance.STAT_MAX, value));
    }
}
