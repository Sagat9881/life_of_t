package ru.lifegame.backend.application.view;

import java.util.List;
import java.util.Map;

public record GameStateView(
        String sessionId,
        String telegramUserId,
        PlayerView player,
        List<RelationshipView> relationships,
        List<PetView> pets,
        TimeView time,
        List<ActionOptionView> availableActions,
        List<QuestView> activeQuests,
        List<String> completedQuests,
        List<ConflictView> activeConflicts,
        EventView pendingEvent,
        EndingView ending,
        ActionResultView lastActionResult
) {}
