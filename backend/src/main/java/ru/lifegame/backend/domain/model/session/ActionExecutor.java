package ru.lifegame.backend.domain.model.session;

import ru.lifegame.backend.domain.action.ActionResult;
import ru.lifegame.backend.domain.action.GameAction;
import ru.lifegame.backend.domain.action.spec.DataDrivenAction;
import ru.lifegame.backend.domain.action.spec.PlayerActionSpec;
import ru.lifegame.backend.domain.event.domain.ActionExecutedEvent;
import ru.lifegame.backend.domain.exception.InvalidActionException;
import ru.lifegame.backend.domain.exception.NotEnoughTimeException;
import ru.lifegame.backend.domain.model.pet.PetCode;
import ru.lifegame.backend.domain.model.pet.Pets;
import ru.lifegame.backend.domain.model.relationship.RelationshipChanges;
import ru.lifegame.backend.domain.model.relationship.Relationships;

public class ActionExecutor {

    public ActionResult execute(
            GameAction action,
            GameSessionContext context,
            DomainEventPublisher eventPublisher
    ) {
        validateActionPreconditions(action, context);
        ActionResult result = action.calculate(context.asReadModel());
        applyActionResult(result, context, action);
        eventPublisher.publish(new ActionExecutedEvent(context.sessionId(), action.type().code()));
        return result;
    }

    private void validateActionPreconditions(GameAction action, GameSessionContext context) {
        int timeCost = action.calculateTimeCost(context.asReadModel());
        if (!context.time().hasEnoughTime(timeCost)) {
            throw new NotEnoughTimeException("Not enough time for action: " + action.type().code());
        }
        if (!context.player().canPerformAction(action.type(), context.time(), timeCost)) {
            throw new InvalidActionException("Cannot perform action: " + action.type().code());
        }
    }

    private void applyActionResult(ActionResult result, GameSessionContext context, GameAction action) {
        context.player().applyStatChanges(result.statChanges());
        context.advanceTime(result.timeCost());

        if (result.rested()) context.player().markRested();
        if (result.workedToday()) context.player().markWorked();

        applyRelationshipChanges(result, context);
        applyPetChanges(result, context);

        // Data-driven skill gains + job effects from spec
        if (action instanceof DataDrivenAction dda) {
            applySkillGains(dda.spec(), context);
            applyJobEffects(dda.spec(), context);
            applyExtraRelationshipEffects(dda.spec(), context);
            if (dda.spec().flags().resetHouseholdDays()) {
                context.player().resetHouseholdDays();
            }
        }

        // Mark NPC interactions from interactedNpcs set
        int currentDay = context.time().day();
        if (result.interactedNpcs() != null) {
            for (String npcId : result.interactedNpcs()) {
                context.relationships().markInteraction(npcId, currentDay);
            }
        }
    }

    private void applyRelationshipChanges(ActionResult result, GameSessionContext context) {
        Relationships relationships = context.relationships();
        result.relationshipChanges().forEach((npcId, delta) -> {
            relationships.applyChanges(npcId, new RelationshipChanges(npcId, delta, 0, 0, 0));
        });
    }

    private void applyPetChanges(ActionResult result, GameSessionContext context) {
        Pets pets = context.pets();
        result.petMoodChanges().forEach((petStr, delta) -> {
            try {
                PetCode pet = PetCode.valueOf(petStr.toUpperCase());
                pets.applyMoodChange(pet, delta);
                pets.applyAttentionChange(pet, Math.abs(delta));
            } catch (IllegalArgumentException e) { /* skip unknown pet */ }
        });
    }

    private void applySkillGains(PlayerActionSpec spec, GameSessionContext context) {
        if (spec.skillGains() != null) {
            spec.skillGains().forEach((skill, xp) ->
                    context.player().improveSkill(skill, xp));
        }
    }

    private void applyJobEffects(PlayerActionSpec spec, GameSessionContext context) {
        if (spec.jobEffects() != null) {
            if (spec.jobEffects().satisfaction() != 0) {
                context.player().changeJobSatisfaction(spec.jobEffects().satisfaction());
            }
            if (spec.jobEffects().burnoutRisk() != 0) {
                context.player().changeJobBurnoutRisk(spec.jobEffects().burnoutRisk());
            }
        }
    }

    private void applyExtraRelationshipEffects(PlayerActionSpec spec, GameSessionContext context) {
        if (spec.extraRelationshipEffects() != null) {
            Relationships relationships = context.relationships();
            for (PlayerActionSpec.ExtraRelEffect e : spec.extraRelationshipEffects()) {
                relationships.applyChanges(e.target(),
                        new RelationshipChanges(e.target(), e.closeness(), e.trust(), e.stability(), e.romance()));
            }
        }
    }
}
