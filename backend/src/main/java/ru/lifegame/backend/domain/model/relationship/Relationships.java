package ru.lifegame.backend.domain.model.relationship;

import ru.lifegame.backend.domain.balance.GameBalance;

import java.util.*;

public class Relationships {
    private final Map<String, Relationship> map;

    public Relationships(Map<String, Relationship> map) {
        this.map = new LinkedHashMap<>(map);
    }

    public Relationship get(String npcId) {
        return map.get(NpcCode.normalize(npcId));
    }

    public void applyChanges(String npcId, RelationshipChanges changes) {
        String key = NpcCode.normalize(npcId);
        Relationship r = map.get(key);
        if (r != null && !r.broken()) {
            map.put(key, r.applyChanges(changes));
        }
    }

    public void markInteraction(String npcId, int currentDay) {
        String key = NpcCode.normalize(npcId);
        Relationship r = map.get(key);
        if (r != null) {
            map.put(key, r.markInteraction(currentDay));
        }
    }

    public void applyDailyDecay(int currentDay) {
        for (String key : map.keySet()) {
            Relationship r = map.get(key);
            if (!r.broken()) {
                map.put(key, r.applyDecay(currentDay));
            }
        }
    }

    public void breakRelationship(String npcId) {
        String key = NpcCode.normalize(npcId);
        Relationship r = map.get(key);
        if (r != null) {
            map.put(key, r.breakRelationship());
        }
    }

    public boolean isDivorced() {
        Relationship husband = get(NpcCode.HUSBAND);
        return husband != null && husband.broken();
    }

    public int totalCloseness() {
        return map.values().stream()
                .filter(r -> !r.broken())
                .mapToInt(Relationship::closeness)
                .sum();
    }

    public Map<String, Relationship> all() {
        return Collections.unmodifiableMap(map);
    }

    public static Relationships initial() {
        var m = new LinkedHashMap<String, Relationship>();
        m.put(NpcCode.HUSBAND, new Relationship(NpcCode.HUSBAND,
                GameBalance.HUSBAND_INITIAL_CLOSENESS, GameBalance.HUSBAND_INITIAL_TRUST,
                GameBalance.HUSBAND_INITIAL_STABILITY, GameBalance.HUSBAND_INITIAL_ROMANCE, 1, false));
        m.put(NpcCode.FATHER, new Relationship(NpcCode.FATHER,
                GameBalance.FATHER_INITIAL_CLOSENESS, GameBalance.FATHER_INITIAL_TRUST,
                GameBalance.FATHER_INITIAL_STABILITY, 0, 1, false));
        return new Relationships(m);
    }
}
