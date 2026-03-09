package ru.lifegame.backend.application.view;

import java.util.Map;

public record DomainEventView(
        String eventType,
        String timestamp,
        Map<String, Object> payload
) {
}
