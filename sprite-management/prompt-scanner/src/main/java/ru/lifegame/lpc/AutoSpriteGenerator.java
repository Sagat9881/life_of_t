package ru.lifegame.lpc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.lifegame.lpc.extractor.ConfigExtractor;
import ru.lifegame.lpc.model.AnimationPrompt;
import ru.lifegame.lpc.model.LpcCharacterConfig;
import ru.lifegame.lpc.model.LpcSpriteRequest;
import ru.lifegame.lpc.scanner.PromptScanner;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates automatic sprite generation workflow.
 * <p>
 * Workflow:
 * 1. Scan all prompts from docs/prompts/characters
 * 2. For each character, load or extract LPC config
 * 3. For each animation prompt, check if sprite exists
 * 4. Generate missing sprites via LpcGeneratorService
 * 5. Save sprites to assets/characters/{characterId}/animations/
 * <p>
 * Usage:
 * <pre>
 * AutoSpriteGenerator generator = context.getBean(AutoSpriteGenerator.class);
 * generator.generateMissingSprites();
 * </pre>
 */
@Slf4j
@Service
public class AutoSpriteGenerator {

    @Autowired
    private PromptScanner promptScanner;

    @Autowired
    private ConfigExtractor configExtractor;

    @Autowired
    private LpcGeneratorService lpcService;

    /**
     * Main workflow: scan prompts and generate all missing sprites.
     *
     * @return Number of sprites generated
     */
    public int generateMissingSprites() {
        log.info("=== Starting Auto-Generation Workflow ===");

        // Get statistics before generation
        PromptScanner.ScanStatistics statsBefore = promptScanner.getStatistics();
        log.info("Before: {} characters, {} prompts, {} existing sprites ({:.1f}% complete)",
                 statsBefore.totalCharacters(),
                 statsBefore.totalPrompts(),
                 statsBefore.existingSprites(),
                 statsBefore.completionPercentage());

        // Find missing sprites
        List<AnimationPrompt> missingPrompts = promptScanner.findMissingSprites();

        if (missingPrompts.isEmpty()) {
            log.info("✅ All sprites already exist! No generation needed.");
            return 0;
        }

        log.info("⚠️ Found {} missing sprites. Starting generation...", missingPrompts.size());

        // Group by character for efficient config loading
        Map<String, List<AnimationPrompt>> promptsByCharacter = missingPrompts.stream()
            .collect(java.util.stream.Collectors.groupingBy(AnimationPrompt::getCharacterId));

        int generated = 0;
        int failed = 0;

        // Process each character
        for (var entry : promptsByCharacter.entrySet()) {
            String characterId = entry.getKey();
            List<AnimationPrompt> prompts = entry.getValue();

            log.info("Processing character: {} ({} missing sprites)", characterId, prompts.size());

            // Load or extract character config
            LpcCharacterConfig config = configExtractor.loadOrExtract(characterId);
            log.info("Config loaded: sex={}, hair={}", config.getSex(), config.getHair());

            // Generate each missing sprite
            for (AnimationPrompt prompt : prompts) {
                try {
                    generateSpriteForPrompt(prompt, config);
                    generated++;
                    log.info("✅ [{}/{}] Generated: {}/{}",
                             generated, missingPrompts.size(),
                             characterId, prompt.getName());
                } catch (Exception e) {
                    failed++;
                    log.error("❌ Failed to generate: {}/{}",
                             characterId, prompt.getName(), e);
                }
            }
        }

        // Get statistics after generation
        PromptScanner.ScanStatistics statsAfter = promptScanner.getStatistics();

        log.info("=== Generation Complete ===");
        log.info("✅ Successfully generated: {}", generated);
        log.info("❌ Failed: {}", failed);
        log.info("After: {} existing sprites ({:.1f}% complete)",
                 statsAfter.existingSprites(),
                 statsAfter.completionPercentage());

        return generated;
    }

    /**
     * Generate sprite for a single prompt.
     */
    private void generateSpriteForPrompt(AnimationPrompt prompt, LpcCharacterConfig config) throws Exception {
        String characterId = prompt.getCharacterId();
        String animationName = prompt.getName();

        // Build sprite request
        LpcSpriteRequest request = LpcSpriteRequest.builder()
            .characterId(characterId)
            .animationName(animationName)
            .config(config)
            .overwrite(false)
            .build();

        // Generate sprite
        Path spritePath = lpcService.generateSprite(request);

        log.debug("Sprite saved to: {}", spritePath);
    }

    /**
     * Generate sprites for specific character only.
     */
    public int generateForCharacter(String characterId) {
        log.info("Generating sprites for character: {}", characterId);

        List<AnimationPrompt> prompts = promptScanner.getCharacterPrompts(characterId);
        if (prompts.isEmpty()) {
            log.warn("No prompts found for character: {}", characterId);
            return 0;
        }

        LpcCharacterConfig config = configExtractor.loadOrExtract(characterId);
        int generated = 0;

        for (AnimationPrompt prompt : prompts) {
            if (!prompt.spriteExists()) {
                try {
                    generateSpriteForPrompt(prompt, config);
                    generated++;
                } catch (Exception e) {
                    log.error("Failed to generate: {}/{}", characterId, prompt.getName(), e);
                }
            }
        }

        log.info("Generated {} sprites for {}", generated, characterId);
        return generated;
    }

    /**
     * Generate single sprite for character + animation.
     */
    public void generateSingle(String characterId, String animationName) throws Exception {
        log.info("Generating single sprite: {}/{}", characterId, animationName);

        LpcCharacterConfig config = configExtractor.loadOrExtract(characterId);

        LpcSpriteRequest request = LpcSpriteRequest.builder()
            .characterId(characterId)
            .animationName(animationName)
            .config(config)
            .overwrite(true) // Force regeneration
            .build();

        Path spritePath = lpcService.generateSprite(request);
        log.info("✅ Generated: {}", spritePath);
    }

    /**
     * Preview what would be generated (dry run).
     */
    public void preview() {
        log.info("=== Dry Run Preview ===");

        List<AnimationPrompt> missing = promptScanner.findMissingSprites();

        if (missing.isEmpty()) {
            log.info("✅ No missing sprites. Everything is up to date!");
            return;
        }

        log.info("⚠️ Would generate {} sprites:", missing.size());

        Map<String, List<AnimationPrompt>> grouped = missing.stream()
            .collect(java.util.stream.Collectors.groupingBy(AnimationPrompt::getCharacterId));

        for (var entry : grouped.entrySet()) {
            String characterId = entry.getKey();
            List<AnimationPrompt> prompts = entry.getValue();

            log.info("  {} ({} sprites):", characterId, prompts.size());
            for (AnimationPrompt prompt : prompts) {
                log.info("    - {}", prompt.getName());
            }
        }
    }

    /**
     * Get generation report.
     */
    public GenerationReport getReport() {
        PromptScanner.ScanStatistics stats = promptScanner.getStatistics();
        List<AnimationPrompt> missing = promptScanner.findMissingSprites();

        return new GenerationReport(
            stats.totalCharacters(),
            stats.totalPrompts(),
            stats.existingSprites(),
            stats.missingSprites(),
            stats.completionPercentage(),
            missing
        );
    }

    /**
     * Generation report record.
     */
    public record GenerationReport(
        int totalCharacters,
        int totalPrompts,
        int existingSprites,
        int missingSprites,
        double completionPercentage,
        List<AnimationPrompt> missingPrompts
    ) {
    }
}
