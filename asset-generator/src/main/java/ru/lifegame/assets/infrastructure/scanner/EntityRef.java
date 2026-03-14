package ru.lifegame.assets.infrastructure.scanner;

/**
 * Reference to a single entity spec discovered from specs-manifest.xml.
 *
 * @param path       path relative to asset-specs root, e.g. "characters/tanya"
 * @param isAbstract true for abstract template specs that should not be rendered
 */
public record EntityRef(String path, boolean isAbstract) {}
