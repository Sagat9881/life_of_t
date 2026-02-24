package ru.lifegame.backend.domain.event;
public record EventTriggeredEvent(String sessionId, String eventId) implements DomainEvent {}
