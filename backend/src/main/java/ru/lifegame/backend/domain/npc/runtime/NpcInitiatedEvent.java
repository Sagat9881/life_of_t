package ru.lifegame.backend.domain.npc.runtime;

public record NpcInitiatedEvent(
    String npcId,
    String actionId,
    double score
) {}
