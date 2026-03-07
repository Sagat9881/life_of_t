package ru.lifegame.assets.domain.model.asset;

/**
 * Naming conventions for generated assets.
 *
 * @param entityType entity type: "characters", "locations", "pets"
 * @param entityName entity name in snake_case: "tanya", "home", "garfield"
 * @param outputDir  target output directory relative to generator output root.
 *                   Generator writes to: {outputRoot}/{outputDir}/
 *                   Maven copies {outputRoot}/ → frontend/public/assets/
 *                   So final URL: /assets/{outputDir}/...
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
            // No "assets/" prefix — outputRoot is already the assets root.
            // Generator: target/generated-assets/{type}/{name}/
            // Maven copies: target/generated-assets/ → frontend/public/assets/
            // Result: frontend/public/assets/{type}/{name}/
            // Frontend URL: /assets/{type}/{name}/
            outputDir = entityType + "/" + entityName;
        }
    }
}
