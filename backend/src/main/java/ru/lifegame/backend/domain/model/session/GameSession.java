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
    private Ending ending;

    public GameSession(
            String sessionId,
            String telegramUserId,
            PlayerCharacter player,
            Relationships relationships,
            Pets pets,
            QuestLog questLog,
            GameTime time
    ) {
        this.sessionId = sessionId;
        this.telegramUserId = telegramUserId;
        this.eventPublisher = new DomainEventPublisher();
        this.context = new GameSessionContext(
                sessionId, player, relationships, pets, time,
                new ArrayList<>(), questLog, new ArrayList<>()
        );
        this.domainEvents = new ArrayList<>();
    }

    public static GameSession createNew(String telegramUserId) {
        String sessionId = UUID.randomUUID().toString();
        PlayerCharacter player = PlayerCharacter.initial();
        Relationships relationships = Relationships.initial();
        Pets pets = Pets.initial();
        QuestLog questLog = new QuestLog();
        GameTime time = new GameTime(1, GameBalance.DAY_START_HOUR);
        return new GameSession(sessionId, telegramUserId, player, relationships, pets, questLog, time);
    }

    public String sessionId() { return sessionId; }
    public String telegramUserId() { return telegramUserId; }
    public PlayerCharacter player() { return context.player(); }
    public Relationships relationships() { return context.relationships(); }
    public Pets pets() { return context.pets(); }
    public QuestLog questLog() { return context.questLog(); }
    public GameTime time() { return context.time(); }
    public List<Conflict> activeConflicts() { return context.activeConflicts(); }
    public List<GameEvent> events() { return context.events(); }
    public Ending ending() { return ending; }
    public GameSessionContext context() { return context; }

    /**
     * Publish a domain event from external services (narrative engines, quest engines).
     * These events will be drained into the response and sent to frontend.
     */
    public void publishDomainEvent(DomainEvent event) {
        eventPublisher.publish(event);
    }

    public ActionResult executeAction(GameAction actionType) {
        ActionExecutor executor = new ActionExecutor();
        ActionResult result = executor.execute(actionType, context, eventPublisher);
        eventPublisher.publish(new ActionExecutedEvent(sessionId, actionType.type().code()));
        return result;
    }

    /**
     * End day logic delegated to DayEndProcessor (injected via service layer).
     * GameSession does NOT instantiate DayEndProcessor directly.
     */
    public void endDay(DayEndProcessor processor) {
        processor.processEndOfDay(context, eventPublisher);
        eventPublisher.publish(new DayEndedEvent(sessionId, time().day()));
    }

    /**
     * Start a conflict from ConflictSpec (loaded from XML).
     * @param conflictId ID from conflicts.xml (e.g. "BURNOUT", "LACK_OF_ATTENTION")
     */
    public Conflict startConflict(String conflictId) {
        ConflictManager manager = new ConflictManager();
        return manager.startConflict(conflictId, context, eventPublisher);
    }

    public void avoidBrewingConflict(String conflictId) {
        ConflictManager manager = new ConflictManager();
        manager.avoidConflict(conflictId, context, eventPublisher);
    }

    /**
     * Apply a tactic to the active conflict.
     * @param tacticCode Tactic code from conflicts.xml (e.g. "SURRENDER", "COMPROMISE")
     */
    public void applyTacticToActiveConflict(String tacticCode) {
        ConflictManager manager = new ConflictManager();
        manager.applyTactic(tacticCode, context, eventPublisher);
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
        eventPublisher.publish(
                new EventTriggeredEvent(sessionId, event.id())
        );
    }

    public void chooseEventOption(String eventId, String optionId) {
        GameEvent event = events().stream()
                .filter(e -> e.id().equals(eventId))
                .findFirst()
                .orElseThrow(() -> new InvalidGameStateException(
                        "Event not found: " + eventId
                ));

        if (!event.isTriggered() || event.isResolved()) {
            throw new InvalidGameStateException(
                    "Event is not active: " + eventId
            );
        }

        EventResult result = event.applyOption(optionId);

        if (result.statChanges() != null) {
            player().applyStatChanges(result.statChanges());
        }
        if (result.relationshipChanges() != null && !result.relationshipChanges().isEmpty()) {
            result.relationshipChanges().forEach((npcId, delta) -> {
                relationships().applyChanges(npcId,
                        new ru.lifegame.backend.domain.model.relationship.RelationshipChanges(
                                npcId, delta, 0, 0, 0
                        ));
            });
        }
    }


    public boolean isGameOver() {
        return ending != null;
    }

    /**
     * Drain all accumulated domain events. Used by services to include
     * events in the response JSON so frontend can trigger animations.
     */
    public List<DomainEvent> drainDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(eventPublisher.drainEvents());
        domainEvents.addAll(events);
        return events;
    }
}
