package ru.lifegame.backend.domain.model.session;

import ru.lifegame.backend.domain.conflict.core.Conflict;
import ru.lifegame.backend.domain.conflict.core.ConflictResolution;
import ru.lifegame.backend.domain.conflict.core.ConflictStage;
import ru.lifegame.backend.domain.conflict.core.ConflictType;
import ru.lifegame.backend.domain.conflict.tactics.ConflictTactic;
import ru.lifegame.backend.domain.conflict.tactics.TacticEffects;
import ru.lifegame.backend.domain.event.domain.ConflictResolvedEvent;
import ru.lifegame.backend.domain.event.domain.ConflictTacticAppliedEvent;
import ru.lifegame.backend.domain.event.domain.ConflictTriggeredEvent;
import ru.lifegame.backend.domain.event.domain.RelationshipBrokenEvent;
import ru.lifegame.backend.domain.exception.InvalidGameStateException;

import java.util.List;
import java.util.UUID;

public class ConflictManager {

    public Conflict startConflict(
            ConflictType type,
            GameSessionContext context,
            DomainEventPublisher eventPublisher
    ) {
        List<Conflict> activeConflicts = context.activeConflicts();
        
        if (hasActiveConflictOfType(type, activeConflicts)) {
            throw new InvalidGameStateException(
                "Conflict of type '" + type.code() + "' is already active"
            );
        }

        String conflictId = UUID.randomUUID().toString();
        Conflict conflict = new Conflict(conflictId, type);
        activeConflicts.add(conflict);
        
        eventPublisher.publish(new ConflictTriggeredEvent(context.sessionId(), conflictId));
        
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

    public TacticEffects applyTactic(
            ConflictTactic tactic,
            GameSessionContext context,
            DomainEventPublisher eventPublisher
    ) {
        Conflict conflict = findActiveConflict(context.activeConflicts());

        TacticEffects effects = conflict.applyTactic(
            tactic,
            context.player(),
            context.relationships()
        );
        
        if (effects.statChanges() != null) {
            context.player().applyStatChanges(effects.statChanges());
        }
        if (effects.relationshipChanges() != null) {
            context.relationships().applyChanges(
                effects.relationshipChanges().npcId(),
                effects.relationshipChanges()
            );
        }

        eventPublisher.publish(
            new ConflictTacticAppliedEvent(context.sessionId(), conflict.id(), tactic.code())
        );

        if (conflict.isResolved()) {
            handleConflictResolution(conflict, context, eventPublisher);
        }
        
        return effects;
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
        
        if (res.relationshipBreak() && conflict.type().opponent().isPresent()) {
            String npc = conflict.type().opponent().get();
            context.relationships().breakRelationship(npc);
            eventPublisher.publish(
                new RelationshipBrokenEvent(context.sessionId(), npc)
            );
        }
    }

    private boolean hasActiveConflictOfType(ConflictType type, List<Conflict> activeConflicts) {
        return activeConflicts.stream()
            .anyMatch(c -> c.type().code().equals(type.code()) && !c.isResolved());
    }

    private Conflict findConflictById(String conflictId, List<Conflict> activeConflicts) {
        return activeConflicts.stream()
            .filter(c -> c.id().equals(conflictId))
            .findFirst()
            .orElseThrow(() -> new InvalidGameStateException(
                "Conflict with id '" + conflictId + "' not found"
            ));
    }

    private Conflict findActiveConflict(List<Conflict> activeConflicts) {
        return activeConflicts.stream()
            .filter(c -> !c.isResolved())
            .findFirst()
            .orElseThrow(() -> new InvalidGameStateException("No active conflict"));
    }
}
