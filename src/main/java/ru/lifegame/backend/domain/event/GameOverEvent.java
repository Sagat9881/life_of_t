package ru.lifegame.backend.domain.event;
public record GameOverEvent(String sessionId, String reason) implements DomainEvent {}
