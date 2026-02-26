package ru.lifegame.backend.domain.model.relationship;

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

    public static RelationshipChanges closeness(NpcCode npc, int delta) {
        return new RelationshipChanges(npc, delta, 0, 0, 0);
    }

    public static RelationshipChanges trust(NpcCode npc, int delta) {
        return new RelationshipChanges(npc, 0, delta, 0, 0);
    }
}
