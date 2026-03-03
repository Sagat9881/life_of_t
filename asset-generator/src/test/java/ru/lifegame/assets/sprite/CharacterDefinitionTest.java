package ru.lifegame.assets.sprite;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Тесты CharacterDefinition")
class CharacterDefinitionTest {

    @Test
    @DisplayName("Татьяна: имя 'tatyana'")
    void testTatyana_name() {
        CharacterDefinition def = CharacterDefinition.tatyana();
        assertThat(def.name()).isEqualTo("tatyana");
    }

    @Test
    @DisplayName("Татьяна: слой BASE_BODY")
    void testTatyana_hasBaseBody() {
        CharacterDefinition def = CharacterDefinition.tatyana();
        boolean hasBaseBody = def.layers().stream()
                .anyMatch(l -> l.category() == SpriteLayerCategory.BASE_BODY);
        assertThat(hasBaseBody).isTrue();
    }

    @Test
    @DisplayName("Татьяна: слой HAIR")
    void testTatyana_hasHair() {
        CharacterDefinition def = CharacterDefinition.tatyana();
        boolean hasHair = def.layers().stream()
                .anyMatch(l -> l.category() == SpriteLayerCategory.HAIR);
        assertThat(hasHair).isTrue();
    }

    @Test
    @DisplayName("Татьяна: слой EYES")
    void testTatyana_hasEyes() {
        CharacterDefinition def = CharacterDefinition.tatyana();
        boolean hasEyes = def.layers().stream()
                .anyMatch(l -> l.category() == SpriteLayerCategory.EYES);
        assertThat(hasEyes).isTrue();
    }

    @Test
    @DisplayName("Татьяна: слои одежды")
    void testTatyana_hasClothing() {
        CharacterDefinition def = CharacterDefinition.tatyana();
        boolean hasTop    = def.layers().stream().anyMatch(l -> l.category() == SpriteLayerCategory.CLOTHING_TOP);
        boolean hasBottom = def.layers().stream().anyMatch(l -> l.category() == SpriteLayerCategory.CLOTHING_BOTTOM);
        assertThat(hasTop).isTrue();
        assertThat(hasBottom).isTrue();
    }

    @Test
    @DisplayName("Татьяна: коричневые волосы")
    void testTatyana_brownHairLayer() {
        CharacterDefinition def = CharacterDefinition.tatyana();
        SpriteLayer hairLayer = def.layers().stream()
                .filter(l -> l.category() == SpriteLayerCategory.HAIR)
                .findFirst().orElseThrow();
        assertThat(hairLayer.imagePath()).contains("brown");
    }

    @Test
    @DisplayName("Татьяна: синие глаза")
    void testTatyana_blueEyesLayer() {
        CharacterDefinition def = CharacterDefinition.tatyana();
        SpriteLayer eyesLayer = def.layers().stream()
                .filter(l -> l.category() == SpriteLayerCategory.EYES)
                .findFirst().orElseThrow();
        assertThat(eyesLayer.imagePath()).contains("blue");
    }

    @Test
    @DisplayName("Татьяна: нет аксессуара")
    void testTatyana_noAccessory() {
        CharacterDefinition def = CharacterDefinition.tatyana();
        boolean hasAccessory = def.layers().stream()
                .anyMatch(l -> l.category() == SpriteLayerCategory.ACCESSORY);
        assertThat(hasAccessory).isFalse();
    }

    @Test
    @DisplayName("Муж: имя 'husband'")
    void testHusband_name() {
        CharacterDefinition def = CharacterDefinition.husband();
        assertThat(def.name()).isEqualTo("husband");
    }

    @Test
    @DisplayName("Муж: BASE_BODY")
    void testHusband_hasBaseBody() {
        CharacterDefinition def = CharacterDefinition.husband();
        boolean hasBaseBody = def.layers().stream()
                .anyMatch(l -> l.category() == SpriteLayerCategory.BASE_BODY);
        assertThat(hasBaseBody).isTrue();
    }

    @Test
    @DisplayName("Муж: ACCESSORY (очки)")
    void testHusband_hasAccessory() {
        CharacterDefinition def = CharacterDefinition.husband();
        boolean hasAccessory = def.layers().stream()
                .anyMatch(l -> l.category() == SpriteLayerCategory.ACCESSORY);
        assertThat(hasAccessory).isTrue();
    }

    @Test
    @DisplayName("Муж: тёмные волосы")
    void testHusband_darkHairLayer() {
        CharacterDefinition def = CharacterDefinition.husband();
        SpriteLayer hairLayer = def.layers().stream()
                .filter(l -> l.category() == SpriteLayerCategory.HAIR)
                .findFirst().orElseThrow();
        assertThat(hairLayer.imagePath()).contains("dark");
    }

    @Test
    @DisplayName("Список слоёв неизменяемый")
    void testLayers_immutable() {
        CharacterDefinition def = CharacterDefinition.tatyana();
        List<SpriteLayer> layers = def.layers();
        assertThatThrownBy(() -> layers.add(
                SpriteLayer.of("extra", SpriteLayerCategory.ACCESSORY, null)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void testConstructor_blankNameThrows() {
        assertThatThrownBy(() -> new CharacterDefinition("", List.of(
                SpriteLayer.of("base", SpriteLayerCategory.BASE_BODY, null))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testConstructor_emptyLayersThrows() {
        assertThatThrownBy(() -> new CharacterDefinition("test", List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testDifferentCharacters_differentLayers() {
        CharacterDefinition tatyana = CharacterDefinition.tatyana();
        CharacterDefinition husband = CharacterDefinition.husband();
        assertThat(tatyana.layers()).isNotEqualTo(husband.layers());
    }
}
