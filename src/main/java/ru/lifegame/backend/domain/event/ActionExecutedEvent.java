package ru.lifegame.backend.domain.event;
public record ActionExecutedEvent(String sessionId, String actionCode) implements DomainEvent {}
