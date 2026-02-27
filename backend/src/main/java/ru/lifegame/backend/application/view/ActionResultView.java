package ru.lifegame.backend.application.view;

import java.util.Map;

public record ActionResultView(String actionCode, int actualTimeCost, String description,
                                 Map<String, Integer> statChanges, Map<String, Integer> relationshipChanges,
                                 Map<String, Integer> petMoodChanges) {}