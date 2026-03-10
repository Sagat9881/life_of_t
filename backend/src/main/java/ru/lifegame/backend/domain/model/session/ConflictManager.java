package ru.lifegame.backend.domain.model.session;

import ru.lifegame.backend.domain.conflict.core.Conflict;
import ru.lifegame.backend.domain.conflict.core.ConflictResolution;
import ru.lifegame.backend.domain.conflict.core.ConflictStage;
import ru.lifegame.backend.domain.event.domain.ConflictResolvedEvent;
import ru.lifegame.backend.domain.event.domain.ConflictTriggeredEvent;
import ru.lifegame.backend.domain.exception.InvalidGameStateException;

import java.util.List;

/**
 * Manages active conflicts within a game session.
 * Updated to work with string-based conflict IDs (no more ConflictType/ConflictTactic enums).
 */
public class ConflictManager {

    /**
     * Add a new conflict instance created by ConflictEngine.
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
     * Create a conflict from conflictId and add to active conflicts.
     * Used when manually triggering a conflict (e.g., from action or event).
     */
    public Conflict startConflict(
            String conflictId,
            GameSessionContext context,
            DomainEventPublisher eventPublisher
    ) {
        // TODO: Lookup ConflictSpec from ConflictEngine and create Conflict instance
        // For now, create a minimal stub
        Conflict conflict = new Conflict(
            java.util.UUID.randomUUID().toString(),
            conflictId,
            "[Placeholder Label]",
            "[Placeholder Description]",
            "SELF",
            "internal"
        );
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
     * Apply tactic (stub for now — tactics engine not yet integrated).
     * TODO: Integrate ConflictEngine tactic application when ready.
     */
    public void applyTactic(
            String tacticCode,
            GameSessionContext context,
            DomainEventPublisher eventPublisher
    ) {
        Conflict conflict = findActiveConflict(context.activeConflicts());
        
        // TODO: Lookup tactic from ConflictEngine, evaluate outcomes, apply effects
        // For now, just mark conflict as resolved (placeholder)
        throw new UnsupportedOperationException("Tactic application not yet implemented with new engine");
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
//            eventPublisher.publish(
//                 new Relationship RelationshipBrokenEvent(context.sessionId(), npc)
//            );
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
