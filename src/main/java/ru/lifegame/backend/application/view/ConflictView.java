package ru.lifegame.backend.application.view;

import java.util.List;

public record ConflictView(
        String id,
        String typeCode,
        String label,
        String stage,
        int playerCsp,
        int opponentCsp,
        List<TacticOptionView> availableTactics
) {}

 record TacticOptionView(String code, String label, String description) {}

 record QuestView(String id, String title, String description, int progress, boolean isCompleted) {}

 record EventView(String id, String title, String description, List<EventOptionView> options) {}

 record EventOptionView(String code, String label, String description) {}

 record EndingView(String type, String category, String summary) {}
