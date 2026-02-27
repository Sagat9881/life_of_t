package ru.lifegame.backend.domain.model.session;

import java.util.Optional;

/**
 * Repository interface for GameSession aggregate.
 * Defined in domain layer, implemented in infrastructure.
 */
public interface GameSessionRepository {
    
    /**
     * Find game session by telegram user ID.
     * @param telegramUserId unique telegram user identifier
     * @return optional game session
     */
    Optional<GameSession> findByTelegramUserId(String telegramUserId);
    
    /**
     * Save or update game session.
     * @param session game session to save
     */
    void save(GameSession session);
    
    /**
     * Check if session exists for user.
     * @param telegramUserId unique telegram user identifier
     * @return true if session exists
     */
    boolean exists(String telegramUserId);
}
