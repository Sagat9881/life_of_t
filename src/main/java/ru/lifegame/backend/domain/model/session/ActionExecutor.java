package ru.lifegame.backend.domain.model.session;

import ru.lifegame.backend.domain.action.*;
import ru.lifegame.backend.domain.event.domain.ActionExecutedEvent;
import ru.lifegame.backend.domain.exception.*;
import ru.lifegame.backend.domain.model.character.PlayerCharacter;
import ru.lifegame.backend.domain.model.pet.PetCode;
import ru.lifegame.backend.domain.model.pet.Pets;
import ru.lifegame.backend.domain.model.relationship.NpcCode;
import ru.lifegame.backend.domain.model.relationship.RelationshipChanges;
import ru.lifegame.backend.domain.model.relationship.Relationships;

/**
 * Domain service responsible for executing game actions.
 * Validates action preconditions, calculates effects, and applies them to game state.
 */
public class ActionExecutor {

    /**
     * Execute a game action and apply its effects.
     * 
     * @param action The action to execute
     * @param context The current game session context
     * @param eventPublisher Publisher for domain events
     * @return ActionResult containing the effects of the action
     * @throws NotEnoughTimeException if there's insufficient time
     * @throws InvalidActionException if action cannot be performed
     */
    public ActionResult execute(
            GameAction action,
            GameSessionContext context,
            DomainEventPublisher eventPublisher
    ) {
        validateActionPreconditions(action, context);

        ActionResult result = action.calculate(context.asReadModel());
        applyActionResult(result, context);
        
        eventPublisher.publish(new ActionExecutedEvent(context.sessionId(), action.type().code()));
        
        return result;
    }

    private void validateActionPreconditions(GameAction action, GameSessionContext context) {
        int timeCost = action.calculateTimeCost(context.asReadModel());
        
        if (!context.time().hasEnoughTime(timeCost)) {
            throw new NotEnoughTimeException(
                "Not enough time for action: " + action.type().code()
            );
        }
        
        if (!context.player().canPerformAction(action.type(), context.time(), timeCost)) {
            throw new InvalidActionException(
                "Cannot perform action: " + action.type().code()
            );
        }
    }

    private void applyActionResult(ActionResult result, GameSessionContext context) {
        // Apply stat changes
        context.player().applyStatChanges(result.statChanges());
        
        // Advance time
        context.advanceTime(result.timeCost());
        
        // Mark player state flags
        if (result.rested()) context.player().markRested();
        if (result.workedToday()) context.player().markWorked();
        
        // Apply relationship changes
        applyRelationshipChanges(result, context);
        
        // Apply pet changes
        applyPetChanges(result, context);
    }

    private void applyRelationshipChanges(ActionResult result, GameSessionContext context) {
        Relationships relationships = context.relationships();
        int currentDay = context.time().day();
        
        result.relationshipChanges().forEach((npcStr, delta) -> {
            try {
                NpcCode npc = NpcCode.valueOf(npcStr.toUpperCase());
                relationships.applyChanges(npc, new RelationshipChanges(npc, delta, 0, 0, 0));
                relationships.markInteraction(npc, currentDay);
            } catch (IllegalArgumentException e) {
                // Skip unknown NPC codes
            }
        });
        
        if (result.interactedWithHusband()) {
            relationships.markInteraction(NpcCode.HUSBAND, currentDay);
        }
        if (result.interactedWithFather()) {
            relationships.markInteraction(NpcCode.FATHER, currentDay);
        }
    }

    private void applyPetChanges(ActionResult result, GameSessionContext context) {
        Pets pets = context.pets();
        
        result.petMoodChanges().forEach((petStr, delta) -> {
            try {
                PetCode pet = PetCode.valueOf(petStr.toUpperCase());
                pets.applyMoodChange(pet, delta);
                pets.applyAttentionChange(pet, Math.abs(delta));
            } catch (IllegalArgumentException e) {
                // Skip unknown pet codes
            }
        });
    }
}
