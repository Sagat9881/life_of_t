package ru.lifegame.assets.domain.model.docs;

import java.util.Collections;
import java.util.List;

/**
 * Immutable domain DTO describing one entity for the docs-preview JSON output.
 * Corresponds to one element of the FR-2 array in visual-docs-preview-mode.md.
 *
 * <p>Field rules (per FR-2):
 * <ul>
 *   <li>{@code id} — last segment of {@code path} (e.g. "characters/tanya" → "tanya")</li>
 *   <li>{@code path} — full path from manifest</li>
 *   <li>{@code type} — first path segment (e.g. "characters")</li>
 *   <li>{@code displayName} — first letter capitalised</li>
 *   <li>{@code spriteAtlasFile} — idle PNG name or null</li>
 *   <li>{@code animations} — list from &lt;animations&gt; or &lt;animations-extra&gt;</li>
 *   <li>{@code colorPalette} — from &lt;color-palette&gt; or empty list</li>
 *   <li>{@code constraints} — from &lt;constraints&gt; or null</li>
 *   <li>{@code abstractEntity} — from manifest abstract attribute</li>
 * </ul>
 */
public record EntityDocsDescriptor(
        String id,
        String path,
        String type,
        String displayName,
        String spriteAtlasFile,
        List<String> animations,
        List<ColorEntry> colorPalette,
        ConstraintsDescriptor constraints,
        boolean abstractEntity
) {
    public EntityDocsDescriptor {
        if (id   == null || id.isBlank())   throw new IllegalArgumentException("id must not be blank");
        if (path == null || path.isBlank()) throw new IllegalArgumentException("path must not be blank");
        if (type == null || type.isBlank()) throw new IllegalArgumentException("type must not be blank");
        if (displayName == null || displayName.isBlank()) throw new IllegalArgumentException("displayName must not be blank");
        animations   = animations   != null ? Collections.unmodifiableList(animations)   : List.of();
        colorPalette = colorPalette != null ? Collections.unmodifiableList(colorPalette) : List.of();
    }

    /**
     * Derives the entity id from the path: last segment after the final '/'.
     */
    public static String idFromPath(String path) {
        if (path == null || path.isBlank()) throw new IllegalArgumentException("path must not be blank");
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    /**
     * Derives the entity type from the path: first segment before the first '/'.
     */
    public static String typeFromPath(String path) {
        if (path == null || path.isBlank()) throw new IllegalArgumentException("path must not be blank");
        int slash = path.indexOf('/');
        return slash >= 0 ? path.substring(0, slash) : path;
    }

    /**
     * Capitalises the first letter of name, preserving the rest.
     */
    public static String displayName(String name) {
        if (name == null || name.isBlank()) return name;
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
