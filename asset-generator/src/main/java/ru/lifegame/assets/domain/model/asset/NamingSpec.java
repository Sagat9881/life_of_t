package ru.lifegame.assets.domain.model.asset;

/**
 * Naming conventions for generated assets.
 *
 * @param entityType entity type: "characters", "locations", "pets"
 * @param entityName entity name in snake_case: "tanya", "home", "garfield"
 * @param outputDir  target output directory relative to assets root
 */
public record NamingSpec(
        String entityType,
        String entityName,
        String outputDir
) {
    public NamingSpec {
        if (entityType == null || entityType.isBlank()) {
            throw new IllegalArgumentException("entityType must not be blank");
        }
        if (entityName == null || entityName.isBlank()) {
            throw new IllegalArgumentException("entityName must not be blank");
        }
        if (outputDir == null || outputDir.isBlank()) {
            outputDir = "assets/" + entityType + "/" + entityName;
        }
    }
}
