package ru.lifegame.assets.config;

/**
 * System enum for asset generator output mode.
 * STANDARD — PNG + sprite-atlas.json (default).
 * DOCS_PREVIEW — docs-preview.json only (no PNG generation).
 *
 * <p>This is a system type (per skill-file §5.1.3) — not a narrative entity.
 */
public enum OutputMode {
    STANDARD,
    DOCS_PREVIEW
}
