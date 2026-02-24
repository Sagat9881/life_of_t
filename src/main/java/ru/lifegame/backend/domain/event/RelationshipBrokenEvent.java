package ru.lifegame.backend.domain.event;
public record RelationshipBrokenEvent(String sessionId, String npcCode) implements DomainEvent {}
