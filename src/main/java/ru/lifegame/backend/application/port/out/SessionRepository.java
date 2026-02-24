package ru.lifegame.backend.application.port.out;

import ru.lifegame.backend.domain.model.session.GameSession;
import ru.lifegame.backend.domain.model.session.GameSessionRepository;

/**
 * Application port for session repository.
 * Extends domain repository interface for consistency.
 */
public interface SessionRepository extends GameSessionRepository {
    // Inherits all methods from GameSessionRepository
    // Can add application-specific methods if needed
}
