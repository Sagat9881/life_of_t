package ru.lifegame.backend.application.port.out;

import ru.lifegame.backend.domain.event.DomainEvent;

public interface EventPublisher {
    void publish(DomainEvent event);
}
