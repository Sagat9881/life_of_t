package ru.lifegame.demo.dto;

import java.util.List;
import java.util.Map;

/**
 * Snapshot DTOs for REST API responses.
 * All types are Java 21 records – immutable, no Lombok.
 */
public final class DemoDtos {

    private DemoDtos() {}

    // -----------------------------------------------------------------------
    // Top-level status
    // -----------------------------------------------------------------------

    /**
     * Full game-state snapshot returned by {@code GET /api/demo/status}.
     */
    public record GameStateDto(
            CharacterDto character,
            RelationshipsDto relationships,
            GameTimeDto gameTime,
            List<QuestSummaryDto> activeQuests,
            List<String> generatedAssets
    ) {}

    // -----------------------------------------------------------------------
    // Character
    // -----------------------------------------------------------------------

    public record CharacterDto(
            String name,
            StatsDto stats,
            String job,
            String location,
            boolean burnedOut,
            boolean inInternalCrisis,
            boolean bankrupt
    ) {}

    public record StatsDto(
            int energy,
            int health,
            int stress,
            int mood,
            int money,
            int selfEsteem
    ) {}

    // -----------------------------------------------------------------------
    // Relationships
    // -----------------------------------------------------------------------

    public record RelationshipsDto(
            Map<String, RelationshipDto> byNpc
    ) {}

    public record RelationshipDto(
            String npc,
            int closeness,
            int trust,
            int stability,
            int romance,
            boolean broken
    ) {}

    // -----------------------------------------------------------------------
    // Time
    // -----------------------------------------------------------------------

    public record GameTimeDto(
            int day,
            int hour
    ) {}

    // -----------------------------------------------------------------------
    // Quests
    // -----------------------------------------------------------------------

    public record QuestSummaryDto(
            String id,
            String type,
            String title,
            String status,
            int progressPercent,
            List<StepDto> steps
    ) {}

    public record StepDto(
            String description,
            int required,
            int current,
            boolean completed
    ) {}

    // -----------------------------------------------------------------------
    // Assets
    // -----------------------------------------------------------------------

    public record AssetInfoDto(
            String id,
            String path,
            int frameWidth,
            int frameHeight,
            int frameCount,
            int frameRate
    ) {}
}
