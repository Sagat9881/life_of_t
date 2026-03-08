package ru.lifegame.backend.infrastructure.persistence;

import ru.lifegame.backend.application.port.out.SessionRepository;
import ru.lifegame.backend.domain.model.session.GameSession;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemorySessionRepository implements SessionRepository {

    private final ConcurrentHashMap<String, GameSession> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> userIdIndex = new ConcurrentHashMap<>();

    @Override
    public void save(GameSession session) {
        sessions.put(session.sessionId(), session);
        userIdIndex.put(String.valueOf(session.telegramUserId()), session.sessionId());
    }

    @Override
    public Optional<GameSession> findByTelegramUserId(String telegramUserId) {
        String sessionId = userIdIndex.get(telegramUserId);
        if (sessionId == null) return Optional.empty();
        return Optional.ofNullable(sessions.get(sessionId));
    }

    @Override
    public boolean exists(String telegramUserId) {
        return userIdIndex.containsKey(telegramUserId);
    }
}
