package ru.lifegame.backend.domain.event;

import java.util.*;

public class GameEvent {
    private final String id;
    private final String title;
    private final String description;
    private EventStatus status;
    private final List<EventOption> options;
    private EventResult result;

    public GameEvent(String id, String title, String description, List<EventOption> options) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = EventStatus.AVAILABLE;
        this.options = List.copyOf(options);
    }

    public void trigger() {
        if (status == EventStatus.AVAILABLE) {
            status = EventStatus.TRIGGERED;
        }
    }

    public EventResult applyOption(String optionCode) {
        if (status != EventStatus.TRIGGERED) {
            throw new IllegalStateException("Event not triggered");
        }
        EventOption option = options.stream()
                .filter(o -> o.code().equals(optionCode))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown option: " + optionCode));
        this.result = new EventResult(
                option.description(),
                option.statChanges(),
                option.relationshipNpc(),
                option.relationshipDelta()
        );
        this.status = EventStatus.RESOLVED;
        return this.result;
    }

    public void expire() {
        if (status == EventStatus.AVAILABLE || status == EventStatus.TRIGGERED) {
            status = EventStatus.EXPIRED;
        }
    }

    public boolean isResolved() { return status == EventStatus.RESOLVED; }
    public boolean isTriggered() { return status == EventStatus.TRIGGERED; }

    public String id() { return id; }
    public String title() { return title; }
    public String description() { return description; }
    public EventStatus status() { return status; }
    public List<EventOption> options() { return options; }
    public EventResult result() { return result; }
}
