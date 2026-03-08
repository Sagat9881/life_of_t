package ru.lifegame.backend.domain.npc;

import ru.lifegame.backend.domain.model.relationship.NpcCode;
import ru.lifegame.backend.domain.model.relationship.Relationship;

/**
 * Unified NPC profile: identity + mood + memory + schedule.
 * One per NPC in the game session.
 */
public class NpcProfile {
    private final NpcCode code;
    private final String displayName;
    private NpcMood mood;
    private final NpcMemory memory;
    private final NpcSchedule schedule;

    public NpcProfile(NpcCode code, String displayName, NpcMood mood, NpcSchedule schedule) {
        this.code = code;
        this.displayName = displayName;
        this.mood = mood;
        this.memory = new NpcMemory();
        this.schedule = schedule;
    }

    public static NpcProfile husband() {
        return new NpcProfile(NpcCode.HUSBAND, "Sam",
            NpcMood.initialHusband(), NpcSchedule.husbandSchedule());
    }

    public static NpcProfile father() {
        return new NpcProfile(NpcCode.FATHER, "Dad",
            NpcMood.initialFather(), NpcSchedule.fatherSchedule());
    }

    public void dailyTick(Relationship relationship, int currentDay) {
        int daysSince = currentDay - relationship.lastInteractionDay();
        this.mood = mood.dailyTick(relationship.closeness(), daysSince);
    }

    public void onPlayerInteraction(int day, String actionCode) {
        memory.record(day, actionCode, "direct");
        this.mood = mood.onInteraction(0);
    }

    public void observePlayerAction(int day, String actionCode) {
        memory.record(day, actionCode, "observed");
    }

    public NpcCode code() { return code; }
    public String displayName() { return displayName; }
    public NpcMood mood() { return mood; }
    public NpcMemory memory() { return memory; }
    public NpcSchedule schedule() { return schedule; }
}