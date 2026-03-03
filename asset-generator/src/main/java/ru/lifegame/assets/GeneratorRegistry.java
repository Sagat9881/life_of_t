package ru.lifegame.assets;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Routes an {@link AssetRequest} to the appropriate {@link AssetGenerator}
 * based on {@link AssetType}:
 * <ul>
 *   <li>{@code TEXTURE}   → {@code ProceduralTextureGenerator}</li>
 *   <li>{@code CHARACTER} → {@code LpcSpriteCompositor}</li>
 *   <li>{@code BACKGROUND}, {@code ITEM} → first registered generator that
 *       is not type-specific, or falls back to ProceduralTextureGenerator</li>
 * </ul>
 */
public class GeneratorRegistry {

    private static final String TEXTURE_GENERATOR_NAME   = "ProceduralTextureGenerator";
    private static final String CHARACTER_GENERATOR_NAME = "LpcSpriteCompositor";

    private final Map<String, AssetGenerator> byName;
    private final AssetGenerator textureGenerator;
    private final AssetGenerator characterGenerator;

    public GeneratorRegistry(List<AssetGenerator> generators) {
        if (generators == null || generators.isEmpty()) {
            throw new IllegalArgumentException("At least one generator must be registered");
        }
        this.byName = generators.stream()
                .collect(Collectors.toMap(AssetGenerator::name, Function.identity()));

        this.textureGenerator = resolve(TEXTURE_GENERATOR_NAME, generators.get(0));
        this.characterGenerator = resolve(CHARACTER_GENERATOR_NAME,
                generators.size() > 1 ? generators.get(1) : generators.get(0));
    }

    private AssetGenerator resolve(String name, AssetGenerator fallback) {
        return byName.getOrDefault(name, fallback);
    }

    /**
     * Selects the generator appropriate for the request's {@link AssetType}
     * and delegates generation to it.
     */
    public BufferedImage generate(AssetRequest request) {
        AssetGenerator generator = selectGenerator(request.type());
        return generator.generate(request);
    }

    /**
     * Selects the generator for the given asset type.
     */
    public AssetGenerator selectGenerator(AssetType type) {
        return switch (type) {
            case CHARACTER -> characterGenerator;
            case TEXTURE, BACKGROUND, ITEM -> textureGenerator;
        };
    }

    /** Returns the generator registered under the given name, or null. */
    public AssetGenerator getByName(String name) {
        return byName.get(name);
    }

    /** Returns all registered generators. */
    public List<AssetGenerator> all() {
        return List.copyOf(byName.values());
    }
}
