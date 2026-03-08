package ru.lifegame.backend.domain.npc;

/**
 * Current physical activity of an NPC at a given moment.
 * Sent to frontend for rendering correct animation + location.
 */
public record NpcActivity(
    String activityId,
    String animationKey,
    String locationId
) {
    public static NpcActivity away() {
        return new NpcActivity("away", "none", "away");
    }

    public static NpcActivity fromScheduleSlot(NpcSpec.ScheduleSlot slot) {
        return new NpcActivity(slot.activityId(), slot.animationKey(), slot.locationId());
    }

    public boolean isPresent() {
        return !"away".equals(locationId);
    }
}
