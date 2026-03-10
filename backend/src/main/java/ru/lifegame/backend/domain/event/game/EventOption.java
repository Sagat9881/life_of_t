package ru.lifegame.backend.domain.event.game;

import ru.lifegame.backend.domain.model.stats.StatChanges;

import java.util.Map;

/**
 * A player-facing choice inside a narrative GameEvent.
 *
 * id:                    machine-readable code sent back on selection (e.g. "ok", "breathe")
 * labelRu:               button label shown to the player
 * statChanges:           stat mutations applied when this option is chosen
 * relationshipChanges:   NPC relationship deltas applied when this option is chosen
 *
 * resultText was removed — outcome text is not stored in EventSpec and was always null.
 * Legacy alias methods code()/label()/description() were removed;
 * use id() and labelRu() directly.
 */
public record EventOption(
    String id,
    String labelRu,
    StatChanges statChanges,
    Map<String, Integer> relationshipChanges
) {}
