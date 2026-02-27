package ru.lifegame.backend.infrastructure.persistence;

import ru.lifegame.backend.application.port.out.SessionRepository;
import ru.lifegame.backend.domain.model.session.GameSession;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemorySessionRepository implements SessionRepository {

    private final Map<String, GameSession> sessions = new ConcurrentHashMap<>();

    @Override
    public Optional<GameSession> findBySessionId(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }
    
    @Override
    public Optional<GameSession> findByTelegramUserId(long telegramUserId) {
        return sessions.values().stream()
                .filter(s -> s.telegramUserId() == telegramUserId)
                .findFirst();
    }

    @Override
    public void save(GameSession session) {
        sessions.put(session.sessionId(), session);
    }

    @Override
    public void delete(String sessionId) {
        sessions.remove(sessionId);
    }

    @Override
    public boolean existsByTelegramUserId(long telegramUserId) {
        return sessions.values().stream()
                .anyMatch(s -> s.telegramUserId() == telegramUserId);
    }
}
