package ru.lifegame.backend.domain.event;
public record EndingAchievedEvent(String sessionId, String endingType) implements DomainEvent {}
