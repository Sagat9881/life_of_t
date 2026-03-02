package ru.lifegame.sprite.scanner.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Request for generating a specific LPC sprite animation.
 * <p>
 * Combines character config with animation-specific parameters.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LpcSpriteRequest {

    /**
     * Character identifier (e.g., "tatyana")
     */
    private String characterId;

    /**
     * Animation name (e.g., "idle-neutral", "walk-south")
     * Used for file naming and organization
     */
    private String animationName;

    /**
     * Animation code (optional)
     * Used when multiple prompts map to same animation
     */
    private String animationCode;

    /**
     * Character appearance configuration
     */
    private LpcCharacterConfig config;

    /**
     * Output format
     * Default: "png"
     * Options: "png", "webp"
     */
    @Builder.Default
    private String format = "png";

    /**
     * Whether to overwrite existing sprite
     * Default: false (skip if exists)
     */
    @Builder.Default
    private boolean overwrite = false;

    /**
     * Target directory for output
     * Default: "assets/characters/{characterId}/animations/"
     */
    private String outputDirectory;
}
