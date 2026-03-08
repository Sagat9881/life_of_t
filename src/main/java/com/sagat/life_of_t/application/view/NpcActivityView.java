package com.sagat.life_of_t.application.view;

/**
 * DTO for frontend: current NPC state for rendering.
 */
public record NpcActivityView(
        String npcId,
        String activity,
        String animation,
        String location,
        int happiness,
        int energy
) {}
