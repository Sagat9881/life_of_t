package ru.lifegame.backend.domain.model.session;

import ru.lifegame.backend.domain.action.*;
import ru.lifegame.backend.domain.conflict.*;
import ru.lifegame.backend.domain.ending.*;
import ru.lifegame.backend.domain.event.*;
import ru.lifegame.backend.domain.exception.InvalidGameStateException;
import ru.lifegame.backend.domain.model.character.PlayerCharacter;
import ru.lifegame.backend.domain.model.pet.Pets;
import ru.lifegame.backend.domain.model.relationship.NpcCode;
import ru.lifegame.backend.domain.model.relationship.RelationshipChanges;
import ru.lifegame.backend.domain.model.relationship.Relationships;
import ru.lifegame.backend.domain.quest.*;

import java.util.*;

/**
 * Root entity of the GameSession aggregate.
 * Manages the complete state of a player's game session.
 * Delegates business logic to domain services while maintaining aggregate consistency.
 */
public class GameSession implements GameSessionReadModel {
    // --- Identity and immutable data ---
    private final String sessionId;
    private final String telegramUserId;
    
    // --- Game state ---
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
    
    // --- Domain services (stateless) ---
    private final ActionExecutor actionExecutor;
    private final ConflictManager conflictManager;
    private final DayEndProcessor dayEndProcessor;
    private final DomainEventPublisher eventPublisher;

    public GameSession(
            String sessionId,
            String telegramUserId,
            PlayerCharacter player,
            Relationships relationships,
            Pets pets,
            GameTime time
    ) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId cannot be null");
        this.telegramUserId = Objects.requireNonNull(telegramUserId, "telegramUserId cannot be null");
        this.player = Objects.requireNonNull(player, "player cannot be null");
        this.relationships = Objects.requireNonNull(relationships, "relationships cannot be null");
        this.pets = Objects.requireNonNull(pets, "pets cannot be null");
        this.time = Objects.requireNonNull(time, "time cannot be null");
        this.activeConflicts = new ArrayList<>();
        this.questLog = new QuestLog();
        this.events = new ArrayList<>();
        
        // Initialize domain services
        this.actionExecutor = new ActionExecutor();
        this.conflictManager = new ConflictManager();
        this.dayEndProcessor = new DayEndProcessor();
        this.eventPublisher = new DomainEventPublisher();
    }

    // --- Public API ---

    /**
     * Execute a game action.
     */
    public ActionResult executeAction(GameAction action) {
        validateNotFinished();
        
        GameSessionContext context = createContext();
        ActionResult result = actionExecutor.execute(action, context, eventPublisher);
        
        // Synchronize time back to session state
        this.time = context.time();
        this.lastActionResult = result;
        
        // Check if day is over and trigger end-of-day processing
        if (time.isDayOver()) {
            endDay();
        }
        
        return result;
    }

    /**
     * Start a new conflict.
     */
    public Conflict startConflict(ConflictType type) {
        validateNotFinished();
        GameSessionContext context = createContext();
        return conflictManager.startConflict(type, context, eventPublisher);
    }

    /**
     * Avoid a brewing conflict.
     */
    public void avoidConflict(String conflictId) {
        validateNotFinished();
        GameSessionContext context = createContext();
        conflictManager.avoidConflict(conflictId, context, eventPublisher);
    }

    /**
     * Apply a tactic to the active conflict.
     */
    public TacticEffects applyTacticToActiveConflict(ConflictTactic tactic) {
        validateNotFinished();
        GameSessionContext context = createContext();
        return conflictManager.applyTactic(tactic, context, eventPublisher);
    }

    /**
     * Apply a choice to a triggered event.
     */
    public EventResult applyEventChoice(String eventId, String optionCode) {
        validateNotFinished();
        
        GameEvent event = events.stream()
                .filter(e -> e.id().equals(eventId) && e.isTriggered())
                .findFirst()
                .orElseThrow(() -> new InvalidGameStateException(
                    "Event not found or not triggered: " + eventId
                ));

        EventResult result = event.applyOption(optionCode);
        
        // Apply event effects
        if (result.statChanges() != null) {
            player.applyStatChanges(result.statChanges());
        }
        if (result.relationshipNpc() != null && !result.relationshipNpc().isEmpty()) {
            NpcCode npc = NpcCode.valueOf(result.relationshipNpc());
            relationships.applyChanges(npc, new RelationshipChanges(npc, result.relationshipDelta(), 0, 0, 0));
        }
        
        return result;
    }

    /**
     * Process end of day.
     */
    public void endDay() {
        GameSessionContext context = createContext();
        dayEndProcessor.processEndOfDay(context, eventPublisher);
        
        // Synchronize state back to session
        this.time = context.time();
        this.ending = context.ending();
        this.gameOverReason = context.gameOverReason();
    }

    /**
     * Drain accumulated domain events.
     */
    public List<DomainEvent> drainDomainEvents() {
        return eventPublisher.drainEvents();
    }

    // --- State queries ---

    public boolean isFinished() {
        return gameOverReason != null || ending != null;
    }

    private void validateNotFinished() {
        if (isFinished()) {
            throw new InvalidGameStateException("Game session is finished");
        }
    }

    private GameSessionContext createContext() {
        return new GameSessionContext(
            sessionId,
            player,
            relationships,
            pets,
            time,
            activeConflicts,
            questLog,
            events
        );
    }

    // --- GameSessionReadModel implementation ---
    @Override public PlayerCharacter player() { return player; }
    @Override public Relationships relationships() { return relationships; }
    @Override public Pets pets() { return pets; }
    @Override public GameTime time() { return time; }

    // --- Getters ---
    public String sessionId() { return sessionId; }
    public String telegramUserId() { return telegramUserId; }
    public List<Conflict> activeConflicts() { return Collections.unmodifiableList(activeConflicts); }
    public QuestLog questLog() { return questLog; }
    public List<GameEvent> events() { return Collections.unmodifiableList(events); }
    public Ending ending() { return ending; }
    public GameOverReason gameOverReason() { return gameOverReason; }
    public ActionResult lastActionResult() { return lastActionResult; }

    // --- Factory method ---
    public static GameSession createNew(String telegramUserId) {
        if (telegramUserId == null || telegramUserId.isBlank()) {
            throw new IllegalArgumentException("telegramUserId cannot be null or blank");
        }
        
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
