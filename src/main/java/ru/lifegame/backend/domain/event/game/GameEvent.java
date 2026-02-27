package ru.lifegame.backend.domain.event.game;

import java.util.List;
import java.util.Map;

public class GameEvent {
    private final String id;
    private final GameEventType type;
    private final String title;
    private final String description;
    private final List<EventOption> options;
    private final Map<String, Object> context;
    private final int priority;
    private final int dayTriggered;
    private boolean triggered;
    private boolean resolved;

    public GameEvent(
            String id,
            GameEventType type,
            String title,
            String description,
            List<EventOption> options,
            Map<String, Object> context,
            int priority,
            int dayTriggered
    ) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.description = description;
        this.options = options;
        this.context = context;
        this.priority = priority;
        this.dayTriggered = dayTriggered;
        this.triggered = false;
        this.resolved = false;
    }

    public void applyOption(String optionId) {
        this.resolved = true;
    }

    public String id() { return id; }
    public GameEventType type() { return type; }
    public String title() { return title; }
    public String description() { return description; }
    public List<EventOption> options() { return options; }
    public Map<String, Object> context() { return context; }
    public int priority() { return priority; }
    public int dayTriggered() { return dayTriggered; }
    public boolean isTriggered() { return triggered; }
    public boolean isResolved() { return resolved; }
    
    public void markTriggered() { this.triggered = true; }
}
