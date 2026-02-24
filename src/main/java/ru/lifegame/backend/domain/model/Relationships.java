package ru.lifegame.backend.domain.model;

import ru.lifegame.backend.domain.balance.GameBalance;

import java.util.*;

public class Relationships {
    private final Map<NpcCode, Relationship> map;

    public Relationships(Map<NpcCode, Relationship> map) {
        this.map = new EnumMap<>(map);
    }

    public Relationship get(NpcCode npc) {
        return map.get(npc);
    }

    public void applyChanges(NpcCode npc, RelationshipChanges changes) {
        Relationship r = map.get(npc);
        if (r != null && !r.broken()) {
            map.put(npc, r.applyChanges(changes));
        }
    }

    public void markInteraction(NpcCode npc, int currentDay) {
        Relationship r = map.get(npc);
        if (r != null) {
            map.put(npc, r.markInteraction(currentDay));
        }
    }

    public void applyDailyDecay(int currentDay) {
        for (NpcCode npc : map.keySet()) {
            Relationship r = map.get(npc);
            if (!r.broken()) {
                map.put(npc, r.applyDecay(currentDay));
            }
        }
    }

    public boolean isHusbandConflictTriggered(PlayerCharacter player) {
        Relationship husband = map.get(NpcCode.HUSBAND);
        if (husband == null || husband.broken()) return false;
        if (husband.closeness() < GameBalance.HUSBAND_CLOSENESS_ATTENTION) return true;
        if (husband.romance() < GameBalance.HUSBAND_ROMANCE_CRISIS
                && player.consecutiveWorkDays() >= GameBalance.HUSBAND_CONSECUTIVE_WORK_DAYS) return true;
        return false;
    }

    public boolean isFatherConflictTriggered(PlayerCharacter player) {
        Relationship father = map.get(NpcCode.FATHER);
        if (father == null || father.broken()) return false;
        if (father.closeness() < GameBalance.FATHER_CLOSENESS_NEGLECTED) return true;
        if (player.job().satisfaction() < GameBalance.FATHER_CRITICISM_SATISFACTION
                || player.stats().selfEsteem() < GameBalance.FATHER_CRITICISM_SELF_ESTEEM) return true;
        return false;
    }

    public void breakRelationship(NpcCode npc) {
        Relationship r = map.get(npc);
        if (r != null) {
            map.put(npc, r.breakRelationship());
        }
    }

    public boolean isDivorced() {
        Relationship husband = map.get(NpcCode.HUSBAND);
        return husband != null && husband.broken();
    }

    public int totalCloseness() {
        return map.values().stream()
                .filter(r -> !r.broken())
                .mapToInt(Relationship::closeness)
                .sum();
    }

    public Map<NpcCode, Relationship> all() {
        return Collections.unmodifiableMap(map);
    }

    public static Relationships initial() {
        var m = new EnumMap<NpcCode, Relationship>(NpcCode.class);
        m.put(NpcCode.HUSBAND, new Relationship(NpcCode.HUSBAND,
                GameBalance.HUSBAND_INITIAL_CLOSENESS, GameBalance.HUSBAND_INITIAL_TRUST,
                GameBalance.HUSBAND_INITIAL_STABILITY, GameBalance.HUSBAND_INITIAL_ROMANCE, 1, false));
        m.put(NpcCode.FATHER, new Relationship(NpcCode.FATHER,
                GameBalance.FATHER_INITIAL_CLOSENESS, GameBalance.FATHER_INITIAL_TRUST,
                GameBalance.FATHER_INITIAL_STABILITY, 0, 1, false));
        return new Relationships(m);
    }
}
