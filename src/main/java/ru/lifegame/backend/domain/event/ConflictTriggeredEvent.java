package ru.lifegame.backend.domain.event;
public record ConflictTriggeredEvent(String sessionId, String conflictId) implements DomainEvent {}
