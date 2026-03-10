package ru.lifegame.backend.domain.event.game;

import java.util.List;
import java.util.Map;

/**
 * Runtime narrative event hydrated from EventSpec and attached to a GameSession.
 *
 * type: string from EventSpec.meta.type — one of "RANDOM", "TRIGGERED", "SEASONAL".
 * (GameEventType enum was removed; classification is now string-based to match XML.)
 */
public class GameEvent {
    private final String id;
    private final String type;
    private final String title;
    private final String description;
    private final List<DialogueLine> dialogueLines;
    private final List<EventOption> options;
    private final Map<String, Object> context;
    private final int priority;
    private final int dayTriggered;
    private boolean triggered;
    private boolean resolved;

    /** A single dialogue line shown before event choice buttons. */
    public record DialogueLine(String speaker, String textRu) {}

    public GameEvent(
            String id,
            String type,
            String title,
            String description,
            List<DialogueLine> dialogueLines,
            List<EventOption> options,
            Map<String, Object> context,
            int priority,
            int dayTriggered
    ) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.description = description;
        this.dialogueLines = dialogueLines != null ? List.copyOf(dialogueLines) : List.of();
        this.options = options;
        this.context = context;
        this.priority = priority;
        this.dayTriggered = dayTriggered;
        this.triggered = false;
        this.resolved = false;
    }

    public EventResult applyOption(String optionId) {
        EventOption option = options.stream()
            .filter(o -> o.id().equals(optionId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Option not found: " + optionId));

        this.resolved = true;

        return new EventResult(
            null,
            option.statChanges(),
            option.relationshipChanges(),
            null
        );
    }

    public String id()                        { return id; }
    public String type()                      { return type; }
    public String title()                     { return title; }
    public String description()               { return description; }
    public List<DialogueLine> dialogueLines() { return dialogueLines; }
    public List<EventOption> options()        { return options; }
    public Map<String, Object> context()      { return context; }
    public int priority()                     { return priority; }
    public int dayTriggered()                 { return dayTriggered; }
    public boolean isTriggered()              { return triggered; }
    public boolean isResolved()               { return resolved; }

    public void markTriggered() { this.triggered = true; }
}
