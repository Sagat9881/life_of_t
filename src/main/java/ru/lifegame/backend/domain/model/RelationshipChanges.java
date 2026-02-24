package ru.lifegame.backend.domain.model;

import java.util.Optional;

public record RelationshipChanges(
        NpcCode npcCode,
        int closeness,
        int trust,
        int stability,
        int romance
) {
    public static RelationshipChanges none(NpcCode npc) {
        return new RelationshipChanges(npc, 0, 0, 0, 0);
    }

    public static RelationshipChanges forHusband(int closeness, int trust, int stability, int romance) {
        return new RelationshipChanges(NpcCode.HUSBAND, closeness, trust, stability, romance);
    }

    public static RelationshipChanges forFather(int closeness, int trust, int stability, int romance) {
        return new RelationshipChanges(NpcCode.FATHER, closeness, trust, stability, romance);
    }
}
