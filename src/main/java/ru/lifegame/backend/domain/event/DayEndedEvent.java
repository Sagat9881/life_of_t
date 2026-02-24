package ru.lifegame.backend.domain.event;
public record DayEndedEvent(String sessionId, int day) implements DomainEvent {}
