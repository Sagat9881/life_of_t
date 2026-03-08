package ru.lifegame.backend.domain.action.spec;

import ru.lifegame.backend.domain.action.ActionType;

/**
 * ActionType implementation backed by PlayerActionSpec data.
 * Replaces Actions enum and StandardActionType enum for data-driven actions.
 */
public record DataDrivenActionType(
        String code,
        String label,
        String description
) implements ActionType {}
