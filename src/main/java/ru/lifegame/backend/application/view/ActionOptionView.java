package ru.lifegame.backend.application.view;

public record ActionOptionView(
        String code,
        String label,
        String description,
        int estimatedTimeCost,
        boolean isAvailable,
        String unavailableReason
) {}

record ActionResultView(
        String actionCode,
        int actualTimeCost,
        String description,
        java.util.Map<String, Integer> statChanges,
        java.util.Map<String, Integer> relationshipChanges,
        java.util.Map<String, Integer> petMoodChanges
) {}
