package ru.lifegame.lpc.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.nio.file.Path;

/**
 * Represents a parsed animation prompt from docs/prompts/characters.
 * <p>
 * Example:
 * - Path: docs/prompts/characters/tatyana/animations/idle-neutral.txt
 * - Character: tatyana
 * - Animation: idle-neutral
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnimationPrompt {

    /**
     * Character identifier (e.g., "tatyana")
     */
    private String characterId;

    /**
     * Animation name without extension (e.g., "idle-neutral")
     */
    private String name;

    /**
     * Full path to prompt file
     */
    private Path promptPath;

    /**
     * Raw content of prompt file
     */
    private String content;

    /**
     * Expected sprite path
     * Pattern: assets/characters/{characterId}/animations/{name}.png
     */
    private Path expectedSpritePath;

    /**
     * Animation code (optional)
     * Used when multiple prompts map to same animation
     */
    private String code;

    /**
     * Check if sprite already exists
     */
    public boolean spriteExists() {
        return expectedSpritePath != null && expectedSpritePath.toFile().exists();
    }
}
