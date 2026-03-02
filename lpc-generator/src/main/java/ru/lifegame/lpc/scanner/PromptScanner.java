package ru.lifegame.lpc.scanner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.lifegame.lpc.model.AnimationPrompt;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Scans prompt files from docs/prompts/characters and builds animation index.
 * <p>
 * Directory structure:
 * <pre>
 * docs/prompts/characters/
 * ├── tatyana/
 * │   ├── character-visual-specs.txt
 * │   └── animations/
 * │       ├── idle-neutral.txt
 * │       ├── walk-south.txt
 * │       └── ...
 * └── sam/
 *     └── animations/
 *         └── ...
 * </pre>
 */
@Slf4j
@Service
public class PromptScanner {

    private static final String PROMPTS_BASE_DIR = "docs/prompts/characters";
    private static final String ASSETS_BASE_DIR = "assets/characters";
    private static final String ANIMATIONS_SUBDIR = "animations";

    /**
     * Scan all character animation prompts.
     *
     * @return Map of characterId to list of animation prompts
     */
    public Map<String, List<AnimationPrompt>> scanPrompts() {
        log.info("Scanning prompts from: {}", PROMPTS_BASE_DIR);

        Map<String, List<AnimationPrompt>> result = new HashMap<>();
        Path promptsDir = Paths.get(PROMPTS_BASE_DIR);

        if (!Files.exists(promptsDir)) {
            log.warn("Prompts directory does not exist: {}", promptsDir);
            return result;
        }

        try (Stream<Path> characterDirs = Files.list(promptsDir)) {
            characterDirs
                .filter(Files::isDirectory)
                .forEach(characterDir -> {
                    String characterId = characterDir.getFileName().toString();
                    List<AnimationPrompt> prompts = scanCharacterPrompts(characterId, characterDir);
                    if (!prompts.isEmpty()) {
                        result.put(characterId, prompts);
                        log.info("Found {} animation prompts for character: {}", prompts.size(), characterId);
                    }
                });
        } catch (IOException e) {
            log.error("Failed to scan prompts directory", e);
        }

        log.info("Total characters found: {}", result.size());
        return result;
    }

    /**
     * Scan animation prompts for a specific character.
     */
    private List<AnimationPrompt> scanCharacterPrompts(String characterId, Path characterDir) {
        List<AnimationPrompt> prompts = new ArrayList<>();
        Path animationsDir = characterDir.resolve(ANIMATIONS_SUBDIR);

        if (!Files.exists(animationsDir)) {
            log.debug("No animations directory for character: {}", characterId);
            return prompts;
        }

        try (Stream<Path> promptFiles = Files.list(animationsDir)) {
            promptFiles
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".txt"))
                .forEach(promptPath -> {
                    try {
                        AnimationPrompt prompt = parsePrompt(characterId, promptPath);
                        prompts.add(prompt);
                    } catch (Exception e) {
                        log.error("Failed to parse prompt: {}", promptPath, e);
                    }
                });
        } catch (IOException e) {
            log.error("Failed to scan animations directory for: {}", characterId, e);
        }

        return prompts;
    }

    /**
     * Parse a single prompt file.
     */
    private AnimationPrompt parsePrompt(String characterId, Path promptPath) throws IOException {
        String fileName = promptPath.getFileName().toString();
        String animationName = fileName.replace(".txt", "");

        // Read prompt content
        String content = Files.readString(promptPath);

        // Build expected sprite path
        Path spritePath = Paths.get(
            ASSETS_BASE_DIR,
            characterId,
            ANIMATIONS_SUBDIR,
            animationName + ".png"
        );

        log.debug("Parsed prompt: {}/{} -> {}", characterId, animationName, 
                  spritePath.toFile().exists() ? "EXISTS" : "MISSING");

        return AnimationPrompt.builder()
            .characterId(characterId)
            .name(animationName)
            .promptPath(promptPath)
            .content(content)
            .expectedSpritePath(spritePath)
            .build();
    }

    /**
     * Find all prompts with missing sprites.
     *
     * @return List of prompts that need sprite generation
     */
    public List<AnimationPrompt> findMissingSprites() {
        log.info("Searching for missing sprites...");

        Map<String, List<AnimationPrompt>> allPrompts = scanPrompts();
        List<AnimationPrompt> missing = new ArrayList<>();

        for (var entry : allPrompts.entrySet()) {
            String characterId = entry.getKey();
            List<AnimationPrompt> prompts = entry.getValue();

            for (AnimationPrompt prompt : prompts) {
                if (!prompt.spriteExists()) {
                    missing.add(prompt);
                    log.info("Missing sprite: {}/{}", characterId, prompt.getName());
                }
            }
        }

        log.info("Found {} missing sprites", missing.size());
        return missing;
    }

    /**
     * Get prompts for specific character.
     */
    public List<AnimationPrompt> getCharacterPrompts(String characterId) {
        Map<String, List<AnimationPrompt>> allPrompts = scanPrompts();
        return allPrompts.getOrDefault(characterId, Collections.emptyList());
    }

    /**
     * Get statistics about prompts and sprites.
     */
    public ScanStatistics getStatistics() {
        Map<String, List<AnimationPrompt>> allPrompts = scanPrompts();
        
        int totalPrompts = allPrompts.values().stream()
            .mapToInt(List::size)
            .sum();

        int existingSprites = allPrompts.values().stream()
            .flatMap(List::stream)
            .mapToInt(p -> p.spriteExists() ? 1 : 0)
            .sum();

        int missingSprites = totalPrompts - existingSprites;

        return new ScanStatistics(
            allPrompts.size(),
            totalPrompts,
            existingSprites,
            missingSprites
        );
    }

    /**
     * Statistics record.
     */
    public record ScanStatistics(
        int totalCharacters,
        int totalPrompts,
        int existingSprites,
        int missingSprites
    ) {
        public double completionPercentage() {
            if (totalPrompts == 0) return 0.0;
            return (double) existingSprites / totalPrompts * 100.0;
        }
    }
}
