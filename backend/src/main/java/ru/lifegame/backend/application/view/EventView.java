package ru.lifegame.backend.application.view;

import java.util.List;

/**
 * View DTO for a narrative event sent to the frontend.
 *
 * Rendering contract:
 *   1. Show titleRu as modal header
 *   2. Show descriptionRu as subtitle/flavour text
 *   3. Render each DialogueLineView as a speech bubble (speaker + text)
 *   4. Render options as buttons the player can click
 *
 * Every event has at least one option (minimum: a single "Ok" button).
 */
public record EventView(
        String id,
        String titleRu,
        String descriptionRu,
        List<DialogueLineView> dialogue,
        List<EventOptionView> options
) {}
