package ru.lifegame.backend.domain.conflict;

import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.character.PlayerCharacter;
import ru.lifegame.backend.domain.model.relationship.*;
import ru.lifegame.backend.domain.model.stats.StatChanges;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Conflict {
    private final String id;
    private final ConflictType type;
    private ConflictStage stage;
    private ConflictStressPoints csp;
    private final List<ConflictRound> rounds;
    private ConflictResolution resolution;

    public Conflict(String id, ConflictType type) {
        this.id = id;
        this.type = type;
        this.stage = ConflictStage.BREWING;
        this.csp = ConflictStressPoints.initial();
        this.rounds = new ArrayList<>();
        this.resolution = null;
    }

    public void escalate() {
        if (stage == ConflictStage.BREWING) {
            stage = ConflictStage.ESCALATION;
        }
    }

    public void avoidAtBrewingStage() {
        if (stage == ConflictStage.BREWING) {
            this.resolution = ConflictResolution.avoided();
            this.stage = ConflictStage.RESOLUTION;
        }
    }

    public TacticEffects applyTactic(ConflictTactic tactic, PlayerCharacter player, Relationships relationships) {
        if (isResolved()) {
            throw new IllegalStateException("Conflict is already resolved");
        }
        if (stage == ConflictStage.BREWING) {
            escalate();
        }

        TacticEffects effects = tactic.calculateEffects(player, this, relationships);
        csp = csp.apply(effects.cspChanges());

        int roundNum = rounds.size() + 1;
        String situation = buildSituation(roundNum);

        rounds.add(new ConflictRound(
                roundNum, situation, tactic.code(),
                effects.reactionText(), effects.cspChanges(),
                effects.statChanges(), effects.relationshipChanges(),
                effects.succeeded()
        ));

        advanceStage();
        checkResolution(relationships);
        return effects;
    }

    private String buildSituation(int round) {
        return switch (stage) {
            case ESCALATION -> type.label() + ": напряжение нарастает (раунд " + round + ")";
            case CLIMAX -> type.label() + ": решающий момент!";
            default -> type.label() + ": разговор";
        };
    }

    private void advanceStage() {
        if (rounds.size() >= 2 && stage == ConflictStage.ESCALATION) {
            stage = ConflictStage.CLIMAX;
        }
    }

    private void checkResolution(Relationships relationships) {
        if (csp.isOpponentDefeated()) {
            resolve(ConflictOutcome.PLAYER_VICTORY, relationships);
        } else if (csp.isPlayerDefeated()) {
            resolvePlayerDefeat(relationships);
        } else if (rounds.size() >= GameBalance.MAX_CONFLICT_ROUNDS) {
            resolve(ConflictOutcome.COMPROMISE, relationships);
        }
    }

    private void resolvePlayerDefeat(Relationships relationships) {
        Optional<NpcCode> opp = type.opponent();
        if (opp.isPresent()) {
            Relationship rel = relationships.get(opp.get());
            if (rel != null && rel.trust() < GameBalance.TRUST_CRITICAL_FOR_BREAK) {
                resolve(ConflictOutcome.RELATIONSHIP_BREAK, relationships);
                return;
            }
        }
        resolve(ConflictOutcome.OPPONENT_VICTORY, relationships);
    }

    private void resolve(ConflictOutcome outcome, Relationships relationships) {
        boolean isBreak = outcome == ConflictOutcome.RELATIONSHIP_BREAK;
        this.resolution = new ConflictResolution(outcome, StatChanges.none(), null, isBreak);
        this.stage = ConflictStage.RESOLUTION;
    }

    public boolean isResolved() {
        return stage == ConflictStage.RESOLUTION && resolution != null;
    }

    // --- Getters ---
    public String id() { return id; }
    public ConflictType type() { return type; }
    public ConflictStage stage() { return stage; }
    public ConflictStressPoints csp() { return csp; }
    public List<ConflictRound> rounds() { return List.copyOf(rounds); }
    public ConflictResolution resolution() { return resolution; }
}
