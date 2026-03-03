package ru.lifegame.backend.domain.model.session;

import ru.lifegame.backend.domain.event.domain.DomainEvent;

import java.util.ArrayList;
import java.util.List;

public class DomainEventPublisher {
    private final List<DomainEvent> events = new ArrayList<>();

    public void publish(DomainEvent event) {
        events.add(event);
    }

    public List<DomainEvent> drainEvents() {
        List<DomainEvent> drained = new ArrayList<>(events);
        events.clear();
        return drained;
    }
}
