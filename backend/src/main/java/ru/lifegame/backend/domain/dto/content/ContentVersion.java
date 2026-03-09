package ru.lifegame.backend.domain.dto.content;

import java.time.Instant;

/**
 * Version metadata for game content.
 * Used to track content changes and enable client-side caching.
 */
public record ContentVersion(
    String version,
    Instant updatedAt
) {}
