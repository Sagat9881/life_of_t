package ru.lifegame.backend.domain.event;

public sealed interface DomainEvent permits
        ActionExecutedEvent, ConflictTriggeredEvent, ConflictTacticAppliedEvent,
        ConflictResolvedEvent, DayEndedEvent, GameOverEvent,
        RelationshipBrokenEvent, QuestProgressUpdatedEvent,
        EventTriggeredEvent, EndingAchievedEvent {}
