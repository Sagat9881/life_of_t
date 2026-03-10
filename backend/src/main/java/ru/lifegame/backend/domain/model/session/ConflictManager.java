package ru.lifegame.backend.domain.model.session;

import ru.lifegame.backend.domain.conflict.core.Conflict;
import ru.lifegame.backend.domain.conflict.core.ConflictOutcome;
import ru.lifegame.backend.domain.conflict.core.ConflictResolution;
import ru.lifegame.backend.domain.conflict.core.ConflictStage;
import ru.lifegame.backend.domain.conflict.core.ConflictStressPoints;
import ru.lifegame.backend.domain.conflict.core.CspChanges;
import ru.lifegame.backend.domain.conflict.spec.ConflictSpec;
import ru.lifegame.backend.domain.conflict.spec.ConflictTacticSpec;
import ru.lifegame.backend.domain.conflict.engine.ConflictEngine;
import ru.lifegame.backend.domain.event.domain.ConflictResolvedEvent;
import ru.lifegame.backend.domain.event.domain.ConflictTacticAppliedEvent;
import ru.lifegame.backend.domain.event.domain.ConflictTriggeredEvent;
import ru.lifegame.backend.domain.event.domain.RelationshipBrokenEvent;
import ru.lifegame.backend.domain.exception.InvalidGameStateException;
import ru.lifegame.backend.domain.model.relationship.RelationshipChanges;
import ru.lifegame.backend.domain.model.stats.StatChanges;

import java.util.List;
import java.util.Map;

/**
 * Manages active conflicts within a game session.
 * Requires ConflictEngine to resolve specs for startConflict() and applyTactic().
 */
public class ConflictManager {

    private final ConflictEngine conflictEngine;

    public ConflictManager(ConflictEngine conflictEngine) {
        this.conflictEngine = conflictEngine;
    }

    /**
     * Add a new conflict instance already created by ConflictEngine.
     */
    public void addNewConflict(
            Conflict conflict,
            GameSessionContext context,
            DomainEventPublisher eventPublisher
    ) {
        List<Conflict> activeConflicts = context.activeConflicts();
        if (hasActiveConflictOfType(conflict.conflictId(), activeConflicts)) {
            throw new InvalidGameStateException(
                    "Conflict of type '" + conflict.conflictId() + "' is already active"
            );
        }
        activeConflicts.add(conflict);
        eventPublisher.publish(new ConflictTriggeredEvent(context.sessionId(), conflict.id()));
    }

    /**
     * Create a conflict from conflictId (resolved via ConflictEngine) and add to session.
     * Used when manually triggering a conflict from an action or event.
     */
    public Conflict startConflict(
            String conflictId,
            GameSessionContext context,
            DomainEventPublisher eventPublisher
    ) {
        ConflictSpec spec = findSpec(conflictId);
        Conflict conflict = Conflict.fromSpec(spec);
        addNewConflict(conflict, context, eventPublisher);
        return conflict;
    }

    public void avoidConflict(
            String conflictId,
            GameSessionContext context,
            DomainEventPublisher eventPublisher
    ) {
        Conflict conflict = findConflictById(conflictId, context.activeConflicts());
        if (conflict.stage() != ConflictStage.BREWING) {
            throw new InvalidGameStateException(
                    "Cannot avoid conflict '" + conflictId + "': conflict is not in BREWING stage"
            );
        }
        conflict.avoidAtBrewingStage();
        eventPublisher.publish(
                new ConflictResolvedEvent(context.sessionId(), conflictId, "AVOIDED")
        );
    }

    /**
     * Apply a tactic to the currently active (non-resolved) conflict.
     * Resolves the tactic spec from ConflictEngine, applies CSP changes,
     * determines success/failure, applies stat and relationship changes,
     * and resolves the conflict if opponent CSP reaches 0.
     */
    public void applyTactic(
            String tacticCode,
            GameSessionContext context,
            DomainEventPublisher eventPublisher
    ) {
        Conflict conflict = findActiveConflict(context.activeConflicts());

        ConflictSpec spec = findSpec(conflict.conflictId());

        ConflictTacticSpec tactic = spec.tactics().stream()
                .filter(t -> t.code().equals(tacticCode))
                .findFirst()
                .orElseThrow(() -> new InvalidGameStateException(
                        "Unknown tactic '" + tacticCode + "' for conflict '" + conflict.conflictId() + "'"
                ));

        // Apply CSP: deduct player cost and deal damage to opponent
        ConflictStressPoints updatedCsp = conflict.csp().apply(
                new CspChanges(-tactic.baseCspCost(), -tactic.baseOpponentCspCost())
        );
        conflict.updateCsp(updatedCsp);

        // Success = opponent CSP reaches 0, failure = player CSP reaches 0 first
        boolean success = updatedCsp.isOpponentDefeated() ||
                (!updatedCsp.isPlayerDefeated() && tactic.baseOpponentCspCost() > tactic.baseCspCost());

        Map<String, Integer> statChanges = success
                ? tactic.successOutcome().statChanges()
                : tactic.failureOutcome().statChanges();

        Map<String, Integer> relChanges = success
                ? tactic.successOutcome().relationshipChanges()
                : tactic.failureOutcome().relationshipChanges();

        String narrative = success
                ? tactic.successOutcome().narrative()
                : tactic.failureOutcome().narrative();

        // Apply stat changes
        if (statChanges != null && !statChanges.isEmpty()) {
            StatChanges sc = buildStatChanges(statChanges);
            context.player().applyStatChanges(sc);
        }

        // Apply relationship changes: key format is "NPC_ID.field" e.g. "HUSBAND.closeness"
        if (relChanges != null && !relChanges.isEmpty()) {
            applyRelationshipChanges(relChanges, context);
        }

        // Publish tactic applied event with outcome
        eventPublisher.publish(new ConflictTacticAppliedEvent(
                context.sessionId(), conflict.id(), tacticCode, success, narrative
        ));

        // Resolve conflict if either side reaches 0 CSP
        if (updatedCsp.isOpponentDefeated() || updatedCsp.isPlayerDefeated()) {
            ConflictOutcome outcome = updatedCsp.isOpponentDefeated()
                    ? ConflictOutcome.SUCCESS
                    : ConflictOutcome.FAILURE;
            conflict.resolve(ConflictResolution.fromOutcome(outcome));
            handleConflictResolution(conflict, context, eventPublisher);
        }
    }

    private void handleConflictResolution(
            Conflict conflict,
            GameSessionContext context,
            DomainEventPublisher eventPublisher
    ) {
        ConflictResolution res = conflict.resolution();
        eventPublisher.publish(
                new ConflictResolvedEvent(context.sessionId(), conflict.id(), res.outcome().name())
        );
        if (res.relationshipBreak() && conflict.opponent().isPresent()) {
            String npc = conflict.opponent().get();
            context.relationships().breakRelationship(npc);
            eventPublisher.publish(new RelationshipBrokenEvent(context.sessionId(), npc));
        }
    }

    // ---- helpers ----

    private ConflictSpec findSpec(String conflictId) {
        return conflictEngine.getConflictSpecs().stream()
                .filter(s -> s.id().equals(conflictId))
                .findFirst()
                .orElseThrow(() -> new InvalidGameStateException(
                        "No conflict spec found for id: '" + conflictId + "'"
                ));
    }

    private StatChanges buildStatChanges(Map<String, Integer> raw) {
        int energy = raw.getOrDefault("energy", 0);
        int health = raw.getOrDefault("health", 0);
        int stress = raw.getOrDefault("stress", 0);
        int mood = raw.getOrDefault("mood", 0);
        int money = raw.getOrDefault("money", 0);
        int selfEsteem = raw.getOrDefault("self_esteem", 0);
        return new StatChanges(energy, health, stress, mood, money, selfEsteem);
    }

    private void applyRelationshipChanges(Map<String, Integer> raw, GameSessionContext context) {
        // Group by npcId: collect all field deltas for the same NPC
        Map<String, int[]> byNpc = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : raw.entrySet()) {
            String[] parts = entry.getKey().split("\\.", 2);
            if (parts.length != 2) continue;
            String npcId = parts[0];
            byNpc.computeIfAbsent(npcId, k -> new int[4]); // [closeness, trust, stability, romance]
            switch (parts[1]) {
                case "closeness" -> byNpc.get(npcId)[0] += entry.getValue();
                case "trust"     -> byNpc.get(npcId)[1] += entry.getValue();
                case "stability" -> byNpc.get(npcId)[2] += entry.getValue();
                case "romance"   -> byNpc.get(npcId)[3] += entry.getValue();
            }
        }
        for (Map.Entry<String, int[]> e : byNpc.entrySet()) {
            int[] d = e.getValue();
            context.relationships().applyChanges(
                    e.getKey(),
                    new RelationshipChanges(e.getKey(), d[0], d[1], d[2], d[3])
            );
        }
    }

    private boolean hasActiveConflictOfType(String conflictId, List<Conflict> activeConflicts) {
        return activeConflicts.stream()
                .anyMatch(c -> c.conflictId().equals(conflictId) && !c.isResolved());
    }

    private Conflict findConflictById(String id, List<Conflict> activeConflicts) {
        return activeConflicts.stream()
                .filter(c -> c.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new InvalidGameStateException(
                        "Conflict with id '" + id + "' not found"
                ));
    }

    private Conflict findActiveConflict(List<Conflict> activeConflicts) {
        return activeConflicts.stream()
                .filter(c -> !c.isResolved())
                .findFirst()
                .orElseThrow(() -> new InvalidGameStateException("No active conflict"));
    }
}
