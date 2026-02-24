package ru.lifegame.backend.domain.event;
public record QuestProgressUpdatedEvent(String sessionId, String questId) implements DomainEvent {}
