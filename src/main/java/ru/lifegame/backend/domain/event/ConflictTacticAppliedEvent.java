package ru.lifegame.backend.domain.event;
public record ConflictTacticAppliedEvent(String sessionId, String conflictId, String tacticCode) implements DomainEvent {}
