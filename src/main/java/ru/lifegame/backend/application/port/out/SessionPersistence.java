package ru.lifegame.backend.application.port.out;

import ru.lifegame.backend.domain.model.GameSession;
import java.util.Map;

public interface SessionPersistence {
    void persistAll(Map<String, GameSession> sessions);
    Map<String, GameSession> loadAll();
}
