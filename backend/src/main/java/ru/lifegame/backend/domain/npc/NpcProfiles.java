package ru.lifegame.backend.domain.npc;

import ru.lifegame.backend.domain.model.relationship.NpcCode;
import ru.lifegame.backend.domain.model.relationship.Relationships;

import java.util.EnumMap;
import java.util.Map;

/**
 * Aggregate of all NPC profiles in the game session.
 */
public class NpcProfiles {
    private final Map<NpcCode, NpcProfile> profiles;

    public NpcProfiles(Map<NpcCode, NpcProfile> profiles) {
        this.profiles = new EnumMap<>(profiles);
    }

    public static NpcProfiles initial() {
        var map = new EnumMap<NpcCode, NpcProfile>(NpcCode.class);
        map.put(NpcCode.HUSBAND, NpcProfile.husband());
        map.put(NpcCode.FATHER, NpcProfile.father());
        return new NpcProfiles(map);
    }

    public NpcProfile get(NpcCode code) {
        return profiles.get(code);
    }

    public void dailyTick(Relationships relationships, int currentDay) {
        profiles.forEach((code, profile) -> {
            var rel = relationships.get(code);
            if (rel != null && !rel.broken()) {
                profile.dailyTick(rel, currentDay);
            }
        });
    }

    public void observeAction(int day, String actionCode) {
        profiles.values().forEach(p -> p.observePlayerAction(day, actionCode));
    }

    public void onDirectInteraction(NpcCode code, int day, String actionCode) {
        NpcProfile p = profiles.get(code);
        if (p != null) p.onPlayerInteraction(day, actionCode);
    }

    public Map<NpcCode, NpcProfile> all() {
        return Map.copyOf(profiles);
    }
}