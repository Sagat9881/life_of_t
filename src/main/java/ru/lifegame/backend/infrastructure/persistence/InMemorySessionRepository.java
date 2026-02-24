package ru.lifegame.backend.infrastructure.persistence;

import org.springframework.stereotype.Component;
import ru.lifegame.backend.application.port.out.SessionRepository;
import ru.lifegame.backend.application.port.out.SessionPersistence;
import ru.lifegame.backend.domain.model.GameSession;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemorySessionRepository implements SessionRepository {

    private final ConcurrentHashMap<String, GameSession> sessions = new ConcurrentHashMap<>();
    private final SessionPersistence persistence;

    public InMemorySessionRepository(SessionPersistence persistence) {
        this.persistence = persistence;
    }

    @Override
    public Optional<GameSession> findByTelegramUserId(String telegramUserId) {
        return Optional.ofNullable(sessions.get(telegramUserId));
    }

    @Override
    public void save(GameSession gameSession) {
        sessions.put(gameSession.telegramUserId(), gameSession);
    }

    @Override
    public void delete(String telegramUserId) {
        sessions.remove(telegramUserId);
    }

    @PostConstruct
    private void loadSessions() {
        Map<String, GameSession> loaded = persistence.loadAll();
        sessions.putAll(loaded);
    }

    @PreDestroy
    private void saveSessions() {
        persistence.persistAll(Map.copyOf(sessions));
    }
}
