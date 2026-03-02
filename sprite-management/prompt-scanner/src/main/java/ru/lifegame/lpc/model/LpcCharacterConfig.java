package ru.lifegame.lpc.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration for LPC character appearance.
 * <p>
 * Represents all visual parameters needed to generate LPC sprite URL.
 * <p>
 * Example:
 * <pre>
 * LpcCharacterConfig tatyana = LpcCharacterConfig.builder()
 *     .characterId("tatyana")
 *     .sex("female")
 *     .body("Body_Color_light")
 *     .head("Human_Female_light")
 *     .expression("Neutral_light")
 *     .hair(List.of("Shoulder_burgundy"))
 *     .build();
 * </pre>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LpcCharacterConfig {

    /**
     * Character identifier (e.g., "tatyana", "sam", "garfield")
     */
    private String characterId;

    /**
     * Sex: "male" or "female"
     * Required for LPC Generator
     */
    private String sex;

    /**
     * Body type and skin tone
     * Examples: "Body_Color_light", "Body_Color_dark", "Body_Color_darkelf"
     */
    private String body;

    /**
     * Head shape and features
     * Examples: "Human_Male_light", "Human_Female_light"
     */
    private String head;

    /**
     * Facial expression
     * Examples: "Neutral_light", "Happy_light", "Sad_light"
     */
    private String expression;

    /**
     * Hair layers (can have multiple)
     * Examples: ["Shoulder_burgundy", "Bangs_burgundy"]
     */
    @Builder.Default
    private List<String> hair = new ArrayList<>();

    /**
     * Clothing layers organized by type
     * <p>
     * Keys: "tops", "bottoms", "shoes", "hands"
     * Values: List of layer names
     * <p>
     * Example:
     * <pre>
     * clothing.put("tops", List.of("Longsleeve_beige"));
     * clothing.put("bottoms", List.of("Pants_gray_blue"));
     * clothing.put("shoes", List.of("Slippers_white"));
     * </pre>
     */
    @Builder.Default
    private Map<String, List<String>> clothing = new HashMap<>();

    /**
     * Accessory layers organized by type
     * <p>
     * Keys: "necklaces", "bracers", "earrings", "rings"
     * Values: List of layer names
     * <p>
     * Example:
     * <pre>
     * accessories.put("necklaces", List.of("Heart_gold"));
     * </pre>
     */
    @Builder.Default
    private Map<String, List<String>> accessories = new HashMap<>();

    /**
     * Add a hair layer
     */
    public void addHair(String hairLayer) {
        if (this.hair == null) {
            this.hair = new ArrayList<>();
        }
        this.hair.add(hairLayer);
    }

    /**
     * Add a clothing layer
     */
    public void addClothing(String type, String layer) {
        if (this.clothing == null) {
            this.clothing = new HashMap<>();
        }
        this.clothing.computeIfAbsent(type, k -> new ArrayList<>()).add(layer);
    }

    /**
     * Add an accessory layer
     */
    public void addAccessory(String type, String layer) {
        if (this.accessories == null) {
            this.accessories = new HashMap<>();
        }
        this.accessories.computeIfAbsent(type, k -> new ArrayList<>()).add(layer);
    }
}
