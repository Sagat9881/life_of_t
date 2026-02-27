package ru.lifegame.backend.infrastructure.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.lifegame.backend.application.port.out.SessionPersistence;
import ru.lifegame.backend.domain.model.session.GameSession;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class FileSessionPersistence implements SessionPersistence {

    private static final Logger log = LoggerFactory.getLogger(FileSessionPersistence.class);

    private final String filePath;
    private final ObjectMapper objectMapper;
    private final AtomicReference<Map<String, GameSession>> latestSnapshot = new AtomicReference<>(Map.of());

    public FileSessionPersistence(@Value("${session.persistence.file:/app/data/sessions.json}") String filePath) {
        this.filePath = filePath;
        this.objectMapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .findAndRegisterModules();
    }

    @Override
    public void persistAll(Map<String, GameSession> sessions) {
        latestSnapshot.set(Map.copyOf(sessions));
        writeToFile(sessions);
    }

    @Scheduled(fixedRateString = "${session.persistence.interval:10}000")
    public void periodicPersist() {
        Map<String, GameSession> snapshot = latestSnapshot.get();
        if (!snapshot.isEmpty()) {
            writeToFile(snapshot);
        }
    }

    private void writeToFile(Map<String, GameSession> sessions) {
        try {
            objectMapper.writeValue(new File(filePath), sessions);
        } catch (IOException e) {
            log.error("Failed to persist sessions to file: {}", filePath, e);
        }
    }

    @Override
    public Map<String, GameSession> loadAll() {
        File file = new File(filePath);
        if (!file.exists()) {
            return new ConcurrentHashMap<>();
        }
        try {
            return objectMapper.readValue(file, new TypeReference<>() {});
        } catch (IOException e) {
            log.error("Failed to load sessions from file: {}", filePath, e);
            return new ConcurrentHashMap<>();
        }
    }
}
