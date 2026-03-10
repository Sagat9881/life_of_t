package ru.lifegame.backend.application.view;

/**
 * A single choice button inside a narrative event modal.
 * code:    machine-readable id sent back to the server on selection
 * labelRu: button label shown to the player
 */
public record EventOptionView(String code, String labelRu) {}
