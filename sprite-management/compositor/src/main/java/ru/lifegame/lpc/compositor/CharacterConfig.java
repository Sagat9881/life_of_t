package ru.lifegame.sprite.compositor;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * Configuration for character sprite generation
 */
@Data
@Builder
public class CharacterConfig {
    private String id;
    private String gender;      // male, female
    private String body;        // light, dark, etc.
    private String hairStyle;   // long, short, etc.
    private String hair;        // brown, blonde, etc.
    private List<String> clothes;
    private List<String> accessories;
}