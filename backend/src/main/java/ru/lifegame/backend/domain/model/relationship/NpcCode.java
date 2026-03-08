package ru.lifegame.backend.domain.model.relationship;

/**
 * Well-known NPC identifier constants.
 * No longer an enum — NPC ids are Strings loaded from XML specs.
 * This class provides constants for backward-compatible code that
 * references well-known NPCs by name.
 */
public final class NpcCode {
    private NpcCode() {}

    public static final String HUSBAND = "HUSBAND";
    public static final String FATHER = "FATHER";

    /** Check if a given npcId matches a well-known constant (case-insensitive). */
    public static String normalize(String npcId) {
        return npcId != null ? npcId.toUpperCase() : null;
    }
}
