package ru.lifegame.backend.domain.model.session;

import ru.lifegame.backend.domain.action.ActionResult;
import ru.lifegame.backend.domain.action.GameAction;
import ru.lifegame.backend.domain.event.domain.ActionExecutedEvent;
import ru.lifegame.backend.domain.exception.InvalidActionException;
import ru.lifegame.backend.domain.exception.NotEnoughTimeException;
import ru.lifegame.backend.domain.model.pet.PetCode;
import ru.lifegame.backend.domain.model.pet.Pets;
import ru.lifegame.backend.domain.model.relationship.NpcCode;
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
        applyActionResult(result, context);
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

    private void applyActionResult(ActionResult result, GameSessionContext context) {
        context.player().applyStatChanges(result.statChanges());
        context.advanceTime(result.timeCost());

        if (result.rested()) context.player().markRested();
        if (result.workedToday()) context.player().markWorked();

        applyRelationshipChanges(result, context);
        applyPetChanges(result, context);
        applySkillProgression(result, context);
        applyJobProgression(result, context);

        String actionCode = result.actionType().code();
        if ("HOUSEHOLD".equals(actionCode) || "household".equals(actionCode)) {
            context.player().resetHouseholdDays();
        }
    }

    private void applyRelationshipChanges(ActionResult result, GameSessionContext context) {
        Relationships relationships = context.relationships();
        int currentDay = context.time().day();

        result.relationshipChanges().forEach((npcStr, delta) -> {
            try {
                NpcCode npc = NpcCode.valueOf(npcStr.toUpperCase());
                relationships.applyChanges(npc, new RelationshipChanges(npc, delta, 0, 0, 0));
                relationships.markInteraction(npc, currentDay);
            } catch (IllegalArgumentException e) { /* skip unknown NPC */ }
        });

        String code = result.actionType().code();
        if ("DATE_WITH_HUSBAND".equals(code)) {
            relationships.applyChanges(NpcCode.HUSBAND,
                new RelationshipChanges(NpcCode.HUSBAND, 0, 5, 0, 15));
            relationships.markInteraction(NpcCode.HUSBAND, currentDay);
        }
        if ("VISIT_FATHER".equals(code)) {
            relationships.applyChanges(NpcCode.FATHER,
                new RelationshipChanges(NpcCode.FATHER, 0, 5, 0, 0));
            relationships.markInteraction(NpcCode.FATHER, currentDay);
        }

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
            } catch (IllegalArgumentException e) { /* skip unknown pet */ }
        });
    }

    private void applySkillProgression(ActionResult result, GameSessionContext context) {
        String code = result.actionType().code();
        switch (code) {
            case "GO_TO_WORK" -> context.player().improveSkill("efficiency", 2);
            case "DATE_WITH_HUSBAND" -> {
                context.player().improveSkill("charisma", 2);
                context.player().improveSkill("empathy", 1);
            }
            case "VISIT_FATHER" -> {
                context.player().improveSkill("empathy", 2);
                context.player().improveSkill("communication", 1);
            }
            case "PLAY_WITH_CAT" -> context.player().improveSkill("empathy", 1);
            case "WALK_DOG" -> context.player().improveSkill("dog_care", 2);
            case "SELF_CARE" -> {
                context.player().improveSkill("assertiveness", 2);
                context.player().improveSkill("communication", 1);
            }
            case "REST_AT_HOME" -> context.player().improveSkill("humor", 1);
            case "HOUSEHOLD" -> context.player().improveSkill("cooking", 2);
            default -> { }
        }
    }

    private void applyJobProgression(ActionResult result, GameSessionContext context) {
        String code = result.actionType().code();
        if ("GO_TO_WORK".equals(code)) {
            context.player().changeJobSatisfaction(2);
        }
        if ("SELF_CARE".equals(code)) {
            context.player().changeJobBurnoutRisk(-3);
        }
    }
}
