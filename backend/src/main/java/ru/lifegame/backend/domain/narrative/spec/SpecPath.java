package ru.lifegame.backend.domain.narrative.spec;

import java.util.Objects;

/**
 * Value Object that describes where to find a narrative specification on the classpath.
 *
 * <p>Replaces all hardcoded path strings and prevents switch/if branching on
 * entity names in loading code (java-developer-skill.md §5.1).
 *
 * <p>Examples:
 * <pre>
 *   SpecPath.allIn("quests")                   // classpath:narrative/quests/*.xml
 *   SpecPath.allIn("npc-behavior")             // classpath:narrative/npc-behavior/*.xml
 *   SpecPath.single("quests", "quest_wedding") // classpath:narrative/quests/quest_wedding.xml
 * </pre>
 *
 * <p>Ref: java-developer-skill.md §3.1, §5.1, §7.
 */
public record SpecPath(String blockId, String classpathPattern) {

    private static final String BASE = "classpath:narrative/";

    public SpecPath {
        Objects.requireNonNull(blockId, "blockId must not be null");
        Objects.requireNonNull(classpathPattern, "classpathPattern must not be null");
        if (blockId.isBlank()) throw new IllegalArgumentException("blockId must not be blank");
        if (classpathPattern.isBlank()) throw new IllegalArgumentException("classpathPattern must not be blank");
    }

    /**
     * Creates a SpecPath that matches all XML files in the given block directory.
     *
     * @param blockId narrative block folder name, e.g. "quests", "npc-behavior"
     */
    public static SpecPath allIn(String blockId) {
        return new SpecPath(blockId, BASE + blockId + "/*.xml");
    }

    /**
     * Creates a SpecPath that points to a single named XML file.
     *
     * @param blockId    narrative block folder name
     * @param entityName file name without extension, e.g. "quest_wedding"
     */
    public static SpecPath single(String blockId, String entityName) {
        Objects.requireNonNull(entityName, "entityName must not be null");
        if (entityName.isBlank()) throw new IllegalArgumentException("entityName must not be blank");
        return new SpecPath(blockId, BASE + blockId + "/" + entityName + ".xml");
    }

    /** Returns {@code true} if this path targets exactly one file (no wildcards). */
    public boolean isSingle() {
        return !classpathPattern.contains("*");
    }
}
