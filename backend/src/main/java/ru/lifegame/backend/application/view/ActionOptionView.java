package ru.lifegame.backend.application.view;

public record ActionOptionView(String code, String label, String description,
                                 int estimatedTimeCost, boolean isAvailable, String unavailableReason) {}