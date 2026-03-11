package ru.lifegame.backend.domain.model.session;

import ru.lifegame.backend.domain.action.ActionResult;
import ru.lifegame.backend.domain.action.GameAction;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.conflict.core.Conflict;
import ru.lifegame.backend.domain.ending.Ending;
import ru.lifegame.backend.domain.event.domain.*;
import ru.lifegame.backend.domain.event.game.EventResult;
import ru.lifegame.backend.domain.event.game.GameEvent;
import ru.lifegame.backend.domain.exception.InvalidGameStateException;
import ru.lifegame.backend.domain.model.character.PlayerCharacter;
import ru.lifegame.backend.domain.model.pet.Pets;
import ru.lifegame.backend.domain.model.relationship.RelationshipChanges;
import ru.lifegame.backend.domain.model.relationship.Relationships;
import ru.lifegame.backend.domain.quest.QuestLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GameSession {
    private final String sessionId;
    private final String telegramUserId;
    private final GameSessionContext context;
    private final DomainEventPublisher eventPublisher;
    private final List<DomainEvent> domainEvents;
    private final ConflictManager conflictManager;
    private final ActionExecutor actionExecutor;
    private Ending ending;

    public GameSession(
            String sessionId,
            String telegramUserId,
            PlayerCharacter player,
            Relationships relationships,
            Pets pets,
            QuestLog questLog,
            GameTime time,
            ConflictManager conflictManager,
            ActionExecutor actionExecutor
    ) {
        this.sessionId = sessionId;
        this.telegramUserId = telegramUserId;
        this.eventPublisher = new DomainEventPublisher();
        this.context = new GameSessionContext(
                sessionId, player, relationships, pets, time,
                new ArrayList<>(), questLog, new ArrayList<>()
        );
        this.domainEvents = new ArrayList<>();
        this.conflictManager = conflictManager;
        this.actionExecutor = actionExecutor;
    }

    public static GameSession createNew(
            String telegramUserId,
            ConflictManager conflictManager,
            ActionExecutor actionExecutor
    ) {
        String sessionId = UUID.randomUUID().toString();
        PlayerCharacter player = PlayerCharacter.initial();
        Relationships relationships = Relationships.initial();
        Pets pets = Pets.initial();
        QuestLog questLog = new QuestLog();
        GameTime time = new GameTime(1, GameBalance.DAY_START_HOUR);
        return new GameSession(
                sessionId, telegramUserId, player, relationships,
                pets, questLog, time, conflictManager, actionExecutor
        );
    }

    public String sessionId()                    { return sessionId; }
    public String telegramUserId()               { return telegramUserId; }
    public PlayerCharacter player()              { return context.player(); }
    public Relationships relationships()         { return context.relationships(); }
    public Pets pets()                           { return context.pets(); }
    public QuestLog questLog()                   { return context.questLog(); }
    public GameTime time()                       { return context.time(); }
    public List<Conflict> activeConflicts()      { return context.activeConflicts(); }
    public List<GameEvent> events()              { return context.events(); }
    public Ending ending()                       { return ending; }
    public GameSessionContext context()          { return context; }

    public void publishDomainEvent(DomainEvent event) {
        eventPublisher.publish(event);
    }

    public ActionResult executeAction(GameAction actionType) {
        return actionExecutor.execute(actionType, context, eventPublisher);
    }

    public void endDay(DayEndProcessor processor) {
        processor.processEndOfDay(context, eventPublisher);
        eventPublisher.publish(new DayEndedEvent(sessionId, time().day()));
    }

    public Conflict startConflict(String conflictId) {
        return conflictManager.startConflict(conflictId, context, eventPublisher);
    }

    public void avoidBrewingConflict(String conflictId) {
        conflictManager.avoidConflict(conflictId, context, eventPublisher);
    }

    public void applyTacticToActiveConflict(String tacticCode) {
        conflictManager.applyTactic(tacticCode, context, eventPublisher);
    }

    public Optional<GameEvent> currentEvent() {
        return events().stream()
                .filter(e -> e.isTriggered() && !e.isResolved())
                .findFirst();
    }

    public void triggerEvent(GameEvent event) {
        if (!events().contains(event)) {
            events().add(event);
        }
        event.markTriggered();
    }

    public void chooseEventOption(String eventId, String optionId) {
        GameEvent event = events().stream()
                .filter(e -> e.id().equals(eventId))
                .findFirst()
                .orElseThrow(() -> new InvalidGameStateException(
                        "Event not found: " + eventId
                ));
        if (!event.isTriggered() || event.isResolved()) {
            throw new InvalidGameStateException("Event is not active: " + eventId);
        }
        EventResult result = event.applyOption(optionId);
        if (result.statChanges() != null) {
            player().applyStatChanges(result.statChanges());
        }
        if (result.relationshipChanges() != null && !result.relationshipChanges().isEmpty()) {
            result.relationshipChanges().forEach((npcId, delta) ->
                    relationships().applyChanges(
                            npcId,
                            new RelationshipChanges(npcId, delta, 0, 0, 0)
                    )
            );
        }
    }

    public boolean isGameOver() {
        return ending != null;
    }

    public List<DomainEvent> drainEvents() {
        List<DomainEvent> drained = eventPublisher.drainEvents();
        domainEvents.addAll(drained);
        return drained;
    }

    /** Alias for {@link #drainEvents()} — used by services and mappers. */
    public List<DomainEvent> drainDomainEvents() {
        return drainEvents();
    }
}
