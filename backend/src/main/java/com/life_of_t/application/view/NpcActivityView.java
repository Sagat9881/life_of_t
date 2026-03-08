package com.life_of_t.application.view;

import java.util.List;
import java.util.Map;

/**
 * DTO for frontend: current state of all NPCs for rendering.
 * Frontend uses activityId/animationKey/locationId to determine
 * what sprite to show and where.
 */
public record NpcActivityView(
        String npcId,
        String displayName,
        String category,
        String activityId,
        String animationKey,
        String locationId,
        boolean available,
        String unavailableReason
) {

    /**
     * Aggregate view of all NPCs for a single API response.
     */
    public record AllNpcActivitiesView(
            int currentHour,
            int currentDay,
            List<NpcActivityView> npcs
    ) {}
}
