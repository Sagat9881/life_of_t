package ru.lifegame.backend.infrastructure.persistence;

import ru.lifegame.backend.domain.model.session.GameSession;

import java.util.Optional;

/**
 * Interface for session persistence mechanism.
 * Separates storage concerns from repository logic.
 */
public interface SessionPersistence {
    
    /**
     * Load session from persistent storage.
     */
    Optional<GameSession> load(String telegramUserId);
    
    /**
     * Persist session to storage.
     */
    void persist(GameSession session);
    
    /**
     * Check if session exists in storage.
     */
    boolean exists(String telegramUserId);
}
