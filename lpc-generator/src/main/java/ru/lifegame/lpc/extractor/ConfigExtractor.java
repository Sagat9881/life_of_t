package ru.lifegame.lpc.extractor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.lifegame.lpc.model.LpcCharacterConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts LPC character configuration from prompt files.
 * <p>
 * Parses character-visual-specs.txt and converts to LpcCharacterConfig:
 * - Hair color #8B1538 (burgundy) → LPC: "Shoulder_burgundy"
 * - Clothing descriptions → LPC layer names
 * - Accessories → LPC accessory layers
 */
@Slf4j
@Service
public class ConfigExtractor {

    private static final String PROMPTS_BASE_DIR = "docs/prompts/characters";
    private static final String ASSETS_BASE_DIR = "assets/characters";
    private static final String CONFIG_FILENAME = "config.json";

    private final ObjectMapper objectMapper;

    public ConfigExtractor() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Extract LPC config from character visual specs prompt.
     *
     * @param characterId Character identifier
     * @return Extracted configuration
     */
    public LpcCharacterConfig extractFromPrompt(String characterId) throws IOException {
        Path visualSpecsPath = Paths.get(
            PROMPTS_BASE_DIR,
            characterId,
            "character-visual-specs.txt"
        );

        if (!Files.exists(visualSpecsPath)) {
            log.warn("Visual specs not found for: {}", characterId);
            return createDefaultConfig(characterId);
        }

        log.info("Extracting config from: {}", visualSpecsPath);
        String content = Files.readString(visualSpecsPath);

        return parseVisualSpecs(characterId, content);
    }

    /**
     * Parse visual specs content and extract LPC parameters.
     */
    private LpcCharacterConfig parseVisualSpecs(String characterId, String content) {
        LpcCharacterConfig.LpcCharacterConfigBuilder builder = LpcCharacterConfig.builder()
            .characterId(characterId);

        // Extract sex (male/female)
        String sex = extractSex(content);
        builder.sex(sex);

        // Body type (always light for now, can be enhanced)
        builder.body("Body_Color_light");
        builder.head(sex.equals("male") ? "Human_Male_light" : "Human_Female_light");
        builder.expression("Neutral_light");

        // Extract hair
        String hairColor = extractHairColor(content);
        String hairStyle = extractHairStyle(content);
        if (hairColor != null && hairStyle != null) {
            String hairLayer = mapHairLayer(hairStyle, hairColor);
            builder.hair(List.of(hairLayer));
        }

        // Extract clothing
        LpcCharacterConfig config = builder.build();
        extractClothing(content, config);
        extractAccessories(content, config);

        log.info("Extracted config for {}: sex={}, hair={}", 
                 characterId, sex, config.getHair());
        return config;
    }

    /**
     * Extract sex from content.
     */
    private String extractSex(String content) {
        // Look for gender indicators in content
        String lowerContent = content.toLowerCase();
        
        if (lowerContent.contains("female") || lowerContent.contains("женщин")) {
            return "female";
        }
        if (lowerContent.contains("male") || lowerContent.contains("муж") || lowerContent.contains("парень")) {
            return "male";
        }
        
        // Default to female if unclear
        return "female";
    }

    /**
     * Extract hair color from content.
     * Looks for hex colors or color names.
     */
    private String extractHairColor(String content) {
        // Pattern for hex colors (e.g., #8B1538)
        Pattern hexPattern = Pattern.compile("#([0-9A-Fa-f]{6})");
        Matcher hexMatcher = hexPattern.matcher(content);
        
        if (hexMatcher.find()) {
            String hexColor = hexMatcher.group(1).toUpperCase();
            return mapHexToColorName(hexColor);
        }

        // Look for color keywords
        String lowerContent = content.toLowerCase();
        if (lowerContent.contains("burgundy") || lowerContent.contains("бордо")) {
            return "burgundy";
        }
        if (lowerContent.contains("black") || lowerContent.contains("черн")) {
            return "black";
        }
        if (lowerContent.contains("blonde") || lowerContent.contains("блонд")) {
            return "blonde";
        }
        if (lowerContent.contains("brown") || lowerContent.contains("коричнев")) {
            return "brown";
        }

        return "black"; // Default
    }

    /**
     * Map hex color to LPC color name.
     */
    private String mapHexToColorName(String hex) {
        return switch (hex) {
            case "8B1538", "8B1A1A" -> "burgundy"; // Dark red/burgundy
            case "000000" -> "black";
            case "FFD700", "FFC125" -> "blonde";
            case "8B4513", "A0522D" -> "brown";
            default -> "black";
        };
    }

    /**
     * Extract hair style from content.
     */
    private String extractHairStyle(String content) {
        String lowerContent = content.toLowerCase();
        
        if (lowerContent.contains("shoulder") || lowerContent.contains("плеч")) {
            return "Shoulder";
        }
        if (lowerContent.contains("long") || lowerContent.contains("длинн")) {
            return "Long";
        }
        if (lowerContent.contains("short") || lowerContent.contains("коротк")) {
            return "Short";
        }

        return "Shoulder"; // Default
    }

    /**
     * Map hair style + color to LPC layer name.
     */
    private String mapHairLayer(String style, String color) {
        return style + "_" + color;
    }

    /**
     * Extract clothing from content.
     */
    private void extractClothing(String content, LpcCharacterConfig config) {
        String lowerContent = content.toLowerCase();

        // Tops
        if (lowerContent.contains("sweater") || lowerContent.contains("свитер")) {
            config.addClothing("tops", "Longsleeve_beige");
        } else if (lowerContent.contains("turtleneck")) {
            config.addClothing("tops", "Turtleneck_beige");
        } else if (lowerContent.contains("shirt")) {
            config.addClothing("tops", "Shirt_white");
        }

        // Bottoms
        if (lowerContent.contains("jeans") || lowerContent.contains("джинс")) {
            config.addClothing("bottoms", "Pants_gray_blue");
        } else if (lowerContent.contains("pants") || lowerContent.contains("брюки")) {
            config.addClothing("bottoms", "Pants_gray");
        }

        // Shoes
        if (lowerContent.contains("sneakers") || lowerContent.contains("slippers")) {
            config.addClothing("shoes", "Slippers_white");
        } else if (lowerContent.contains("boots")) {
            config.addClothing("shoes", "Boots_brown");
        }
    }

    /**
     * Extract accessories from content.
     */
    private void extractAccessories(String content, LpcCharacterConfig config) {
        String lowerContent = content.toLowerCase();

        // Necklace
        if (lowerContent.contains("necklace") || lowerContent.contains("кулон")) {
            if (lowerContent.contains("heart") || lowerContent.contains("сердечко")) {
                config.addAccessory("necklaces", "Heart_gold");
            } else {
                config.addAccessory("necklaces", "Chain_gold");
            }
        }
    }

    /**
     * Create default config when visual specs not found.
     */
    private LpcCharacterConfig createDefaultConfig(String characterId) {
        return LpcCharacterConfig.builder()
            .characterId(characterId)
            .sex("female")
            .body("Body_Color_light")
            .head("Human_Female_light")
            .expression("Neutral_light")
            .hair(List.of("Shoulder_black"))
            .build();
    }

    /**
     * Save config to JSON file.
     */
    public void saveConfig(String characterId, LpcCharacterConfig config) throws IOException {
        Path configPath = Paths.get(ASSETS_BASE_DIR, characterId, CONFIG_FILENAME);
        
        // Create directories if needed
        Files.createDirectories(configPath.getParent());

        // Write JSON
        objectMapper.writerWithDefaultPrettyPrinter()
            .writeValue(configPath.toFile(), config);

        log.info("Saved config to: {}", configPath);
    }

    /**
     * Load config from JSON file.
     */
    public LpcCharacterConfig loadConfig(String characterId) throws IOException {
        Path configPath = Paths.get(ASSETS_BASE_DIR, characterId, CONFIG_FILENAME);

        if (!Files.exists(configPath)) {
            log.warn("Config file not found: {}", configPath);
            throw new IOException("Config not found: " + characterId);
        }

        LpcCharacterConfig config = objectMapper.readValue(
            configPath.toFile(),
            LpcCharacterConfig.class
        );

        log.info("Loaded config from: {}", configPath);
        return config;
    }

    /**
     * Load or extract config (try load first, extract if not found).
     */
    public LpcCharacterConfig loadOrExtract(String characterId) {
        try {
            return loadConfig(characterId);
        } catch (IOException e) {
            log.info("Config not found, extracting from prompts...");
            try {
                LpcCharacterConfig config = extractFromPrompt(characterId);
                saveConfig(characterId, config);
                return config;
            } catch (IOException ex) {
                log.error("Failed to extract config", ex);
                return createDefaultConfig(characterId);
            }
        }
    }
}
