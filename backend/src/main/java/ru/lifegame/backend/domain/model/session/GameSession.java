package ru.lifegame.backend.domain.model.session;

import ru.lifegame.backend.domain.action.ActionResult;
import ru.lifegame.backend.domain.action.ActionType;
import ru.lifegame.backend.domain.action.GameAction;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.conflict.core.Conflict;
import ru.lifegame.backend.domain.conflict.tactics.ConflictTactic;
import ru.lifegame.backend.domain.ending.Ending;
import ru.lifegame.backend.domain.event.domain.*;
import ru.lifegame.backend.domain.event.game.EventResult;
import ru.lifegame.backend.domain.event.game.GameEvent;
import ru.lifegame.backend.domain.exception.InvalidGameStateException;
import ru.lifegame.backend.domain.model.character.PlayerCharacter;
import ru.lifegame.backend.domain.model.common.PlayerId;
import ru.lifegame.backend.domain.model.pet.Pets;
import ru.lifegame.backend.domain.model.relationship.Relationships;
import ru.lifegame.backend.domain.quest.QuestLog;

import java.util.*;

/**
 * Root aggregate for game session.
 * Contains all game state and orchestrates domain operations.
 */
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

    // === Factory method ===
    public static GameSession createNew(String telegramUserId) {
        String sessionId = UUID.randomUUID().toString();
        PlayerCharacter player = PlayerCharacter.initial();
        Relationships relationships = Relationships.initial();
        Pets pets = Pets.initial();
        QuestLog questLog = new QuestLog();
        GameTime time = new GameTime(1, GameBalance.DAY_START_HOUR);
        return new GameSession(sessionId, telegramUserId, player, relationships, pets, questLog, time);
    }

    // === Accessors ===
    public String sessionId() {
        return sessionId;
    }

    public String telegramUserId() {
        return telegramUserId;
    }

    public PlayerCharacter player() {
        return context.player();
    }

    public Relationships relationships() {
        return context.relationships();
    }

    public Pets pets() {
        return context.pets();
    }

    public QuestLog questLog() {
        return context.questLog();
    }

    public GameTime time() {
        return context.time();
    }

    public List<Conflict> activeConflicts() {
        return context.activeConflicts();
    }

    public List<GameEvent> events() {
        return context.events();
    }

    public Ending ending() {
        return ending;
    }

    public GameSessionContext context() {
        return context;
    }

    // === Actions ===
    public ActionResult executeAction(GameAction actionType) {
        ActionExecutor executor = new ActionExecutor();
        ActionResult result = executor.execute(actionType, context, eventPublisher);
        eventPublisher.publish(new ActionExecutedEvent(sessionId, actionType.type().code()));
        return result;
    }

    // === Day Management ===
    public void endDay() {
        DayEndProcessor processor = new DayEndProcessor();
        processor.processEndOfDay(context, eventPublisher);
        eventPublisher.publish(new DayEndedEvent(sessionId, time().day()));
    }

    // === Conflict Management ===
    public Conflict startConflict(ru.lifegame.backend.domain.conflict.core.ConflictType conflictType) {
        ConflictManager manager = new ConflictManager();
        return manager.startConflict(conflictType, context, eventPublisher);
    }

    public void avoidBrewingConflict(String conflictId) {
        ConflictManager manager = new ConflictManager();
        manager.avoidConflict(conflictId, context, eventPublisher);
    }

    public void applyTacticToActiveConflict(ConflictTactic tactic) {
        ConflictManager manager = new ConflictManager();
        manager.applyTactic(tactic, context, eventPublisher);
    }

    // === Event Management ===
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

        // Apply event effects
        if (result.statChanges() != null) {
            player().applyStatChanges(result.statChanges());
        }
        if (result.relationshipChanges() != null && !result.relationshipChanges().isEmpty()) {
            result.relationshipChanges().forEach((npcName, delta) -> {
                // Find NPC by name and apply changes
                relationships().all().forEach((npc, rel) -> {
                    if (npc.name().equals(npcName)) {
                        relationships().applyChanges(npc,
                                new ru.lifegame.backend.domain.model.relationship.RelationshipChanges(
                                        npc, delta, 0, 0, 0
                                ));
                    }
                });
            });
        }
    }

    // === Game Over ===
    public void checkGameOver() {
        GameOverChecker checker = new GameOverChecker();
        Optional<GameOverReason> gameOverEnding = checker.check(player(),relationships(),pets());
        gameOverEnding.ifPresent(end -> {
            this.ending = end.ending();
            this.context.setGameOverReason(end);
            this.context.setEnding(this.ending);
            eventPublisher.publish(
                    new GameOverEvent(sessionId, ending.type().name())
            );
        });
    }

    public boolean isGameOver() {
        return ending != null;
    }

    // === Event Sourcing ===
    public List<DomainEvent> drainDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(eventPublisher.drainEvents());
        domainEvents.addAll(events);
        return events;
    }
}
