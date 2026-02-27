package ru.lifegame.backend.application.port.out;

import ru.lifegame.backend.domain.event.domain.DomainEvent;

public interface EventPublisher {
    void publish(DomainEvent event);
}