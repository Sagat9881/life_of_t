package ru.lifegame.backend.domain.model.relationship;

public record RelationshipChanges(
        String npcId,
        int closeness,
        int trust,
        int stability,
        int romance
) {
    public static RelationshipChanges none(String npcId) {
        return new RelationshipChanges(npcId, 0, 0, 0, 0);
    }

    public static RelationshipChanges closeness(String npcId, int delta) {
        return new RelationshipChanges(npcId, delta, 0, 0, 0);
    }

    public static RelationshipChanges trust(String npcId, int delta) {
        return new RelationshipChanges(npcId, 0, delta, 0, 0);
    }
}
