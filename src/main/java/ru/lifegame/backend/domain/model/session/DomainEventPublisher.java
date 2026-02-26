package ru.lifegame.backend.domain.model.session;

import ru.lifegame.backend.domain.event.domain.DomainEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain service for accumulating and draining domain events.
 * Used for Event Sourcing and integration with external systems.
 */
public class DomainEventPublisher {
    private final List<DomainEvent> events = new ArrayList<>();

    /**
     * Publish a domain event to the accumulator.
     */
    public void publish(DomainEvent event) {
        events.add(event);
    }

    /**
     * Drain all accumulated events and clear the buffer.
     */
    public List<DomainEvent> drainEvents() {
        List<DomainEvent> drained = new ArrayList<>(events);
        events.clear();
        return drained;
    }
}
