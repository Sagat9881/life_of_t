package com.life_of_t.application.view;

import java.util.List;
import java.util.Map;

/**
 * DTO for frontend: current physical state of an NPC.
 * Frontend uses this to render NPC sprite/animation at correct location.
 */
public record NpcActivityView(
        String npcId,
        String displayName,
        String category,       // "human", "cat", "dog"
        String activityId,     // "breakfast", "sleeping", "phone_scroll"
        String animationKey,   // "eating", "sleeping", "typing"
        String locationId,     // "kitchen", "living_room", "away"
        String moodSummary,    // dominant mood axis: "happy", "lonely", "irritated"
        boolean isAvailable    // can player interact right now?
) {

    /**
     * Creates view from NpcInstance state.
     */
    public static NpcActivityView fromInstance(
            String npcId,
            String displayName,
            String category,
            String activityId,
            String animationKey,
            String locationId,
            String moodSummary,
            boolean isAvailable
    ) {
        return new NpcActivityView(
                npcId, displayName, category,
                activityId, animationKey, locationId,
                moodSummary, isAvailable
        );
    }
}
