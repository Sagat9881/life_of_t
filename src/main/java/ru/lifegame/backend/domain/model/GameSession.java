package ru.lifegame.backend.domain.model;

import ru.lifegame.backend.domain.action.*;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.conflict.*;
import ru.lifegame.backend.domain.ending.*;
import ru.lifegame.backend.domain.event.*;
import ru.lifegame.backend.domain.exception.*;
import ru.lifegame.backend.domain.quest.*;

import java.util.*;

public class GameSession implements GameSessionReadModel {
    private final String sessionId;
    private final String telegramUserId;
    private final PlayerCharacter player;
    private final Relationships relationships;
    private final Pets pets;
    private GameTime time;
    private final List<Conflict> activeConflicts;
    private final QuestLog questLog;
    private final List<GameEvent> events;
    private Ending ending;
    private GameOverReason gameOverReason;
    private ActionResult lastActionResult;
    private final List<DomainEvent> domainEvents;

    public GameSession(String sessionId, String telegramUserId,
                       PlayerCharacter player, Relationships relationships,
                       Pets pets, GameTime time) {
        this.sessionId = sessionId;
        this.telegramUserId = telegramUserId;
        this.player = player;
        this.relationships = relationships;
        this.pets = pets;
        this.time = time;
        this.activeConflicts = new ArrayList<>();
        this.questLog = new QuestLog();
        this.events = new ArrayList<>();
        this.domainEvents = new ArrayList<>();
    }

    public ActionResult executeAction(GameAction action) {
        int timeCost = action.calculateTimeCost(this);
        if (!time.hasEnoughTime(timeCost)) {
            throw new NotEnoughTimeException("Not enough time for action: " + action.type().code());
        }
        if (!player.canPerformAction(action.type(), time, timeCost)) {
            throw new InvalidActionException("Cannot perform action: " + action.type().code());
        }

        ActionResult result = action.calculate(this);
        applyActionResult(result);
        this.lastActionResult = result;
        domainEvents.add(new ActionExecutedEvent(sessionId, action.type().code()));

        if (time.isDayOver()) {
            endDay();
        }
        return result;
    }

    private void applyActionResult(ActionResult result) {
        player.applyStatChanges(result.statChanges());
        time = time.advanceHours(result.timeCost());

        if (result.rested()) player.markRested();
        if (result.workedToday()) player.markWorked();

        result.relationshipChanges().forEach((npcStr, delta) -> {
            NpcCode npc = NpcCode.valueOf(npcStr);
            relationships.applyChanges(npc, new RelationshipChanges(npc, delta, 0, 0, 0));
            relationships.markInteraction(npc, time.day());
        });

        if (result.interactedWithHusband()) {
            relationships.markInteraction(NpcCode.HUSBAND, time.day());
        }
        if (result.interactedWithFather()) {
            relationships.markInteraction(NpcCode.FATHER, time.day());
        }

        result.petMoodChanges().forEach((petStr, delta) -> {
            PetCode pet = PetCode.valueOf(petStr);
            pets.applyMoodChange(pet, delta);
            pets.applyAttentionChange(pet, Math.abs(delta));
        });
    }

    public TacticEffects applyTacticToActiveConflict(ConflictTactic tactic) {
        Conflict conflict = activeConflicts.stream()
                .filter(c -> !c.isResolved())
                .findFirst()
                .orElseThrow(() -> new InvalidGameStateException("No active conflict"));

        TacticEffects effects = conflict.applyTactic(tactic, player, relationships);
        if (effects.statChanges() != null) player.applyStatChanges(effects.statChanges());
        if (effects.relationshipChanges() != null) {
            relationships.applyChanges(effects.relationshipChanges().npcCode(), effects.relationshipChanges());
        }

        domainEvents.add(new ConflictTacticAppliedEvent(sessionId, conflict.id(), tactic.code()));

        if (conflict.isResolved()) {
            handleConflictResolution(conflict);
        }
        return effects;
    }

    private void handleConflictResolution(Conflict conflict) {
        ConflictResolution res = conflict.resolution();
        domainEvents.add(new ConflictResolvedEvent(sessionId, conflict.id(), res.outcome().name()));
        if (res.relationshipBreak() && conflict.type().opponent().isPresent()) {
            NpcCode npc = conflict.type().opponent().get();
            relationships.breakRelationship(npc);
            domainEvents.add(new RelationshipBrokenEvent(sessionId, npc.name()));
        }
    }

    public EventResult applyEventChoice(String eventId, String optionCode) {
        GameEvent event = events.stream()
                .filter(e -> e.id().equals(eventId) && e.isTriggered())
                .findFirst()
                .orElseThrow(() -> new InvalidGameStateException("Event not found or not triggered: " + eventId));

        EventResult result = event.applyOption(optionCode);
        if (result.statChanges() != null) {
            player.applyStatChanges(result.statChanges());
        }
        if (result.relationshipNpc() != null && !result.relationshipNpc().isEmpty()) {
            NpcCode npc = NpcCode.valueOf(result.relationshipNpc());
            relationships.applyChanges(npc, new RelationshipChanges(npc, result.relationshipDelta(), 0, 0, 0));
        }
        return result;
    }

    public void endDay() {
        player.applyEndOfDayDecay();
        relationships.applyDailyDecay(time.day());
        pets.applyDailyDecay();

        ConflictTriggers triggers = new ConflictTriggers();
        List<Conflict> newConflicts = triggers.checkTriggers(player, relationships, time);
        for (Conflict c : newConflicts) {
            if (activeConflicts.stream().noneMatch(
                    existing -> existing.type().code().equals(c.type().code()) && !existing.isResolved())) {
                activeConflicts.add(c);
                domainEvents.add(new ConflictTriggeredEvent(sessionId, c.id()));
            }
        }

        GameOverChecker checker = new GameOverChecker();
        checker.check(player, relationships, pets).ifPresent(reason -> {
            this.gameOverReason = reason;
            domainEvents.add(new GameOverEvent(sessionId, reason.name()));
        });

        if (gameOverReason == null && time.day() >= GameBalance.MAX_GAME_DAYS) {
            EndingEvaluator evaluator = new EndingEvaluator();
            evaluator.findBestEnding(player, relationships, pets, questLog, time)
                    .ifPresent(e -> {
                        this.ending = e;
                        domainEvents.add(new EndingAchievedEvent(sessionId, e.type().name()));
                    });
        }

        domainEvents.add(new DayEndedEvent(sessionId, time.day()));
        time = time.startNewDay();
    }

    public boolean isFinished() {
        return gameOverReason != null || ending != null;
    }

    public List<DomainEvent> drainDomainEvents() {
        List<DomainEvent> copy = List.copyOf(domainEvents);
        domainEvents.clear();
        return copy;
    }

    // GameSessionReadModel implementation
    @Override public PlayerCharacter player() { return player; }
    @Override public Relationships relationships() { return relationships; }
    @Override public Pets pets() { return pets; }
    @Override public GameTime time() { return time; }

    // Getters
    public String sessionId() { return sessionId; }
    public String telegramUserId() { return telegramUserId; }
    public List<Conflict> activeConflicts() { return Collections.unmodifiableList(activeConflicts); }
    public QuestLog questLog() { return questLog; }
    public List<GameEvent> events() { return Collections.unmodifiableList(events); }
    public Ending ending() { return ending; }
    public GameOverReason gameOverReason() { return gameOverReason; }
    public ActionResult lastActionResult() { return lastActionResult; }

    public static GameSession createNew(String telegramUserId) {
        return new GameSession(
                UUID.randomUUID().toString(),
                telegramUserId,
                PlayerCharacter.initial(),
                Relationships.initial(),
                Pets.initial(),
                GameTime.initial()
        );
    }
}
