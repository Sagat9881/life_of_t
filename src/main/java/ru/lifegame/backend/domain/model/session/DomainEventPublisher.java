package ru.lifegame.backend.domain.model.session;

import ru.lifegame.backend.domain.event.DomainEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain service for accumulating and publishing domain events.
 * Provides a clean separation between event production and consumption.
 */
public class DomainEventPublisher {
    private final List<DomainEvent> events;

    public DomainEventPublisher() {
        this.events = new ArrayList<>();
    }

    /**
     * Publish a domain event by adding it to the accumulator.
     */
    public void publish(DomainEvent event) {
        events.add(event);
    }

    /**
     * Drain all accumulated events and clear the buffer.
     * This method should be called after a transaction completes.
     */
    public List<DomainEvent> drainEvents() {
        List<DomainEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    /**
     * Check if there are any pending events.
     */
    public boolean hasEvents() {
        return !events.isEmpty();
    }

    /**
     * Get the number of pending events without draining.
     */
    public int eventCount() {
        return events.size();
    }
}
