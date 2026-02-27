package ru.lifegame.backend.infrastructure.persistence;

import org.springframework.stereotype.Component;
import ru.lifegame.backend.application.port.out.SessionRepository;
import ru.lifegame.backend.domain.model.session.GameSession;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemorySessionRepository implements SessionRepository {
    private final Map<String, GameSession> sessions = new ConcurrentHashMap<>();

    @Override
    public Optional<GameSession> findByTelegramUserId(String telegramUserId) {
        return sessions.values().stream()
                .filter(s -> telegramUserId.equals(s.telegramUserId()))
                .findFirst();
    }

    @Override
    public void save(GameSession session) {
        sessions.put(session.sessionId(), session);
    }

    @Override
    public boolean exists(String telegramUserId) {
        return sessions.values().stream()
                .anyMatch(s -> telegramUserId.equals(s.telegramUserId()));
    }
}
