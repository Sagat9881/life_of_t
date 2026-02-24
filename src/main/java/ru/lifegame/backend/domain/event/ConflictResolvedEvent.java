package ru.lifegame.backend.domain.event;
public record ConflictResolvedEvent(String sessionId, String conflictId, String outcome) implements DomainEvent {}
