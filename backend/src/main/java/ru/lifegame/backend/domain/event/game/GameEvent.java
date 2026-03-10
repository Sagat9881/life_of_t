package ru.lifegame.backend.domain.event.game;

import java.util.List;
import java.util.Map;

public class GameEvent {
    private final String id;
    private final GameEventType type;
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

    /**
     * Full constructor with dialogue lines.
     */
    public GameEvent(
            String id,
            GameEventType type,
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

    /**
     * Backward-compatible constructor (no dialogue lines).
     * Existing callers that don't provide dialogue continue to work.
     */
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
        this(id, type, title, description, List.of(), options, context, priority, dayTriggered);
    }

    public EventResult applyOption(String optionId) {
        EventOption option = options.stream()
            .filter(o -> o.id().equals(optionId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Option not found: " + optionId));

        this.resolved = true;

        return new EventResult(
            option.resultText(),
            option.statChanges(),
            option.relationshipChanges(),
            null
        );
    }

    public String id() { return id; }
    public GameEventType type() { return type; }
    public String title() { return title; }
    public String description() { return description; }
    public List<DialogueLine> dialogueLines() { return dialogueLines; }
    public List<EventOption> options() { return options; }
    public Map<String, Object> context() { return context; }
    public int priority() { return priority; }
    public int dayTriggered() { return dayTriggered; }
    public boolean isTriggered() { return triggered; }
    public boolean isResolved() { return resolved; }

    public void markTriggered() { this.triggered = true; }
}
