package ru.lifegame.backend.domain.engine;

public record NpcInitiatedEvent(
    String npcId,
    String actionId,
    double score
) {}
