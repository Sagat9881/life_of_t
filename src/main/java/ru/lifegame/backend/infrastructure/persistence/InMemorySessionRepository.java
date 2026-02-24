package ru.lifegame.backend.infrastructure.persistence;

import ru.lifegame.backend.application.port.out.SessionRepository;
import ru.lifegame.backend.domain.model.session.GameSession;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of SessionRepository.
 * For development and testing purposes.
 */
public class InMemorySessionRepository implements SessionRepository {
    
    private final Map<String, GameSession> sessions = new ConcurrentHashMap<>();
    private final SessionPersistence persistence;

    public InMemorySessionRepository(SessionPersistence persistence) {
        this.persistence = persistence;
    }

    @Override
    public Optional<GameSession> findByTelegramUserId(String telegramUserId) {
        GameSession cached = sessions.get(telegramUserId);
        if (cached != null) {
            return Optional.of(cached);
        }
        
        Optional<GameSession> loaded = persistence.load(telegramUserId);
        loaded.ifPresent(session -> sessions.put(telegramUserId, session));
        return loaded;
    }

    @Override
    public void save(GameSession session) {
        sessions.put(session.telegramUserId(), session);
        persistence.persist(session);
    }

    @Override
    public boolean exists(String telegramUserId) {
        return sessions.containsKey(telegramUserId) || persistence.exists(telegramUserId);
    }
}
