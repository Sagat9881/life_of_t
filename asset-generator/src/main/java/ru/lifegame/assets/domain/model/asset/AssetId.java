package ru.lifegame.assets.domain.model.asset;

import java.util.Objects;

/**
 * Typed identity of an asset — a pair of (entityType, entityName).
 *
 * <p>Replaces bare strings for asset cross-references (e.g. {@code inheritsFrom})
 * so that the compiler catches mismatched IDs at build time.
 *
 * <p>Immutable record; usable as map key.
 *
 * <p>Examples:
 * <pre>{@code
 *   new AssetId("characters", "tanya")
 *   new AssetId("locations",  "kitchen")
 * }</pre>
 *
 * Ref: java-developer-skill.md §5.2 (no hardcoded asset names in code).
 * TASK-BE-017.
 */
public record AssetId(String entityType, String entityName) {

    public AssetId {
        Objects.requireNonNull(entityType, "entityType must not be null");
        Objects.requireNonNull(entityName, "entityName must not be null");
        if (entityType.isBlank()) throw new IllegalArgumentException("entityType must not be blank");
        if (entityName.isBlank()) throw new IllegalArgumentException("entityName must not be blank");
    }

    /**
     * Parses {@code "entityType/entityName"} notation (used in XML attributes).
     *
     * @param value e.g. {@code "characters/base_character"}
     * @return parsed AssetId
     * @throws IllegalArgumentException if format is invalid
     */
    public static AssetId parse(String value) {
        Objects.requireNonNull(value, "value must not be null");
        int slash = value.indexOf('/');
        if (slash < 1 || slash == value.length() - 1)
            throw new IllegalArgumentException(
                    "AssetId must be in 'entityType/entityName' format, got: '" + value + "'");
        return new AssetId(value.substring(0, slash), value.substring(slash + 1));
    }

    /** Returns {@code "entityType/entityName"} — canonical string form. */
    @Override
    public String toString() { return entityType + "/" + entityName; }
}
