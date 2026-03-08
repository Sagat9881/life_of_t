package ru.lifegame.backend.infrastructure.persistence;

import ru.lifegame.backend.application.port.out.SessionRepository;
import ru.lifegame.backend.domain.model.session.GameSession;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemorySessionRepository implements SessionRepository {

    private final ConcurrentHashMap<String, GameSession> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> userIdIndex = new ConcurrentHashMap<>();

    @Override
    public GameSession save(GameSession session) {
        sessions.put(session.sessionId(), session);
        userIdIndex.put(String.valueOf(session.telegramUserId()), session.sessionId());
        return session;
    }

    @Override
    public Optional<GameSession> findById(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    @Override
    public Optional<GameSession> findByTelegramUserId(String telegramUserId) {
        String sessionId = userIdIndex.get(telegramUserId);
        if (sessionId == null) return Optional.empty();
        return Optional.ofNullable(sessions.get(sessionId));
    }

    @Override
    public void delete(String sessionId) {
        GameSession removed = sessions.remove(sessionId);
        if (removed != null) {
            userIdIndex.remove(String.valueOf(removed.telegramUserId()));
        }
    }
}
