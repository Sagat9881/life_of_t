package ru.lifegame.backend.application.port.out;

import ru.lifegame.backend.domain.model.GameSession;
import java.util.Optional;

public interface SessionRepository {
    Optional<GameSession> findByTelegramUserId(String telegramUserId);
    void save(GameSession gameSession);
    void delete(String telegramUserId);
}
