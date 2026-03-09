package ru.lifegame.backend.domain.model.session;

import lombok.Setter;
import ru.lifegame.backend.domain.action.GameSessionReadModel;
import ru.lifegame.backend.domain.conflict.core.Conflict;
import ru.lifegame.backend.domain.ending.Ending;
import ru.lifegame.backend.domain.event.game.GameEvent;
import ru.lifegame.backend.domain.model.character.PlayerCharacter;
import ru.lifegame.backend.domain.model.pet.Pets;
import ru.lifegame.backend.domain.model.relationship.Relationships;
import ru.lifegame.backend.domain.quest.QuestLog;
import ru.lifegame.backend.domain.npc.runtime.NpcRegistry;

import java.util.List;

/**
 * Context object providing access to game session state for domain services.
 * Encapsulates mutable state and provides controlled access.
 */
public class GameSessionContext {
    private final String sessionId;
    private final PlayerCharacter player;
    private final Relationships relationships;
    private final Pets pets;
    private GameTime time;
    private final List<Conflict> activeConflicts;
    private final QuestLog questLog;
    private final List<GameEvent> events;
    private final NpcRegistry npcRegistry;
    @Setter
    private Ending ending;

    public GameSessionContext(
            String sessionId,
            PlayerCharacter player,
            Relationships relationships,
            Pets pets,
            GameTime time,
            List<Conflict> activeConflicts,
            QuestLog questLog,
            List<GameEvent> events
    ) {
        this(sessionId, player, relationships, pets, time, activeConflicts,
             questLog, events, new NpcRegistry());
    }

    public GameSessionContext(
            String sessionId,
            PlayerCharacter player,
            Relationships relationships,
            Pets pets,
            GameTime time,
            List<Conflict> activeConflicts,
            QuestLog questLog,
            List<GameEvent> events,
            NpcRegistry npcRegistry
    ) {
        this.sessionId = sessionId;
        this.player = player;
        this.relationships = relationships;
        this.pets = pets;
        this.time = time;
        this.activeConflicts = activeConflicts;
        this.questLog = questLog;
        this.events = events;
        this.npcRegistry = npcRegistry;
    }

    public String sessionId() { return sessionId; }
    public PlayerCharacter player() { return player; }
    public Relationships relationships() { return relationships; }
    public Pets pets() { return pets; }
    public GameTime time() { return time; }
    public List<Conflict> activeConflicts() { return activeConflicts; }
    public QuestLog questLog() { return questLog; }
    public List<GameEvent> events() { return events; }
    public Ending ending() { return ending; }
    public NpcRegistry npcRegistry() { return npcRegistry; }

    public void advanceTime(int hours) {
        this.time = time.advanceHours(hours);
    }

    public void startNewDay() {
        this.time = time.startNewDay();
    }

    public GameSessionReadModel asReadModel() {
        return new GameSessionReadModel() {
            @Override public PlayerCharacter player() { return player; }
            @Override public Relationships relationships() { return relationships; }
            @Override public Pets pets() { return pets; }
            @Override public GameTime time() { return time; }
        };
    }
}
