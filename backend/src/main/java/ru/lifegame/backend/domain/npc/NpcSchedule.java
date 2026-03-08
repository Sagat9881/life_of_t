package ru.lifegame.backend.domain.npc;

import ru.lifegame.backend.domain.model.relationship.NpcCode;

/**
 * Defines when NPC is available and where they are.
 * Affects action availability and event context.
 */
public class NpcSchedule {
    private final NpcCode npcCode;
    private final int[][] availableSlots; // [slot][0]=startHour, [slot][1]=endHour
    private final String[] slotLocations;

    public NpcSchedule(NpcCode npcCode, int[][] availableSlots, String[] slotLocations) {
        this.npcCode = npcCode;
        this.availableSlots = availableSlots;
        this.slotLocations = slotLocations;
    }

    public static NpcSchedule husbandSchedule() {
        return new NpcSchedule(NpcCode.HUSBAND,
            new int[][]{{8, 9}, {18, 24}},
            new String[]{"home_morning", "home_evening"}
        );
    }

    public static NpcSchedule fatherSchedule() {
        return new NpcSchedule(NpcCode.FATHER,
            new int[][]{{9, 13}, {16, 21}},
            new String[]{"home_morning", "home_evening"}
        );
    }

    public boolean isAvailable(int hour) {
        for (int[] slot : availableSlots) {
            if (hour >= slot[0] && hour < slot[1]) return true;
        }
        return false;
    }

    public String currentLocation(int hour) {
        for (int i = 0; i < availableSlots.length; i++) {
            if (hour >= availableSlots[i][0] && hour < availableSlots[i][1]) {
                return slotLocations[i];
            }
        }
        return "away";
    }

    public String unavailableReason(int hour) {
        if (npcCode == NpcCode.HUSBAND) {
            if (hour >= 9 && hour < 18) return "Sam is at work right now.";
            return "Sam is sleeping.";
        }
        if (npcCode == NpcCode.FATHER) {
            if (hour >= 21) return "Dad is already asleep.";
            if (hour >= 13 && hour < 16) return "Dad is resting after lunch.";
            return "Dad is busy right now.";
        }
        return "Not available.";
    }

    public NpcCode npcCode() { return npcCode; }
}