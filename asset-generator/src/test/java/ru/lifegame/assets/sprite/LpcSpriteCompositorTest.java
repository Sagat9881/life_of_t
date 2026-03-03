package ru.lifegame.assets.sprite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.lifegame.assets.AssetRequest;
import ru.lifegame.assets.AssetType;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Тесты LpcSpriteCompositor")
class LpcSpriteCompositorTest {

    private LpcSpriteCompositor compositor;

    @BeforeEach
    void setUp() {
        compositor = new LpcSpriteCompositor();
    }

    @Test
    @DisplayName("Генерация спрайта Татьяны: размер 64x64")
    void testGenerate_tatyana_size() {
        AssetRequest request = AssetRequest.of(AssetType.CHARACTER, "tatyana", 64, 64);
        BufferedImage result = compositor.generate(request);
        assertThat(result.getWidth()).isEqualTo(64);
        assertThat(result.getHeight()).isEqualTo(64);
    }

    @Test
    @DisplayName("Генерация спрайта Мужа")
    void testGenerate_husband_size() {
        AssetRequest request = AssetRequest.of(AssetType.CHARACTER, "husband", 64, 64);
        BufferedImage result = compositor.generate(request);
        assertThat(result).isNotNull();
        assertThat(result.getWidth()).isEqualTo(64);
    }

    @Test
    @DisplayName("Генерация спрайта для неизвестного персонажа -> tatyana по умолчанию")
    void testGenerate_unknownCharacter_fallsBackToTatyana() {
        AssetRequest request = new AssetRequest(AssetType.CHARACTER, "unknown", 32, 32,
                java.util.Map.of("character", "nobody"));
        BufferedImage result = compositor.generate(request);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Плейсхолдер: не null, нужного размера")
    void testGeneratePlaceholder_notNull() {
        SpriteLayer layer = SpriteLayer.of("test", SpriteLayerCategory.BASE_BODY, null);
        BufferedImage img = compositor.generatePlaceholder(layer, 64, 64);
        assertThat(img).isNotNull();
        assertThat(img.getWidth()).isEqualTo(64);
        assertThat(img.getHeight()).isEqualTo(64);
    }

    @Test
    @DisplayName("Плейсхолдер: тип ARGB")
    void testGeneratePlaceholder_isARGB() {
        SpriteLayer layer = SpriteLayer.of("test", SpriteLayerCategory.HAIR, null);
        BufferedImage img = compositor.generatePlaceholder(layer, 32, 32);
        assertThat(img.getType()).isEqualTo(BufferedImage.TYPE_INT_ARGB);
    }

    @Test
    @DisplayName("Плейсхолдер: цвет волос — коричневый")
    void testGeneratePlaceholder_hairColorBrown() {
        SpriteLayer layer = SpriteLayer.of("hair", SpriteLayerCategory.HAIR, null);
        BufferedImage img = compositor.generatePlaceholder(layer, 64, 64);
        int centerPixel = img.getRGB(32, 32);
        Color color = new Color(centerPixel, true);
        assertThat(color.getRed()).isGreaterThan(color.getBlue());
    }

    @Test
    @DisplayName("Композиция: размер совпадает с запрошенным")
    void testComposite_sizeMatchesRequest() {
        CharacterDefinition def = CharacterDefinition.tatyana();
        BufferedImage result = compositor.composite(def, 48, 48);
        assertThat(result.getWidth()).isEqualTo(48);
        assertThat(result.getHeight()).isEqualTo(48);
    }

    @Test
    @DisplayName("Загрузка слоя: несуществующего файла -> плейсхолдер")
    void testLoadLayerImage_missingFile_returnsPlaceholder() {
        SpriteLayer layer = SpriteLayer.of("missing", SpriteLayerCategory.BASE_BODY, "/nonexistent/path.png");
        BufferedImage img = compositor.loadLayerImage(layer, 64, 64);
        assertThat(img).isNotNull();
        assertThat(img.getWidth()).isEqualTo(64);
    }

    @Test
    @DisplayName("Название генератора")
    void testName() {
        assertThat(compositor.name()).isEqualTo("LpcSpriteCompositor");
    }

    @Test
    @DisplayName("Композиция всех категорий слоёв")
    void testComposite_allLayerCategories() {
        List<SpriteLayer> layers = List.of(
                SpriteLayer.of("base",    SpriteLayerCategory.BASE_BODY,       null),
                SpriteLayer.of("shoes",   SpriteLayerCategory.SHOES,           null),
                SpriteLayer.of("bottom",  SpriteLayerCategory.CLOTHING_BOTTOM, null),
                SpriteLayer.of("top",     SpriteLayerCategory.CLOTHING_TOP,    null),
                SpriteLayer.of("eyes",    SpriteLayerCategory.EYES,            null),
                SpriteLayer.of("hair",    SpriteLayerCategory.HAIR,            null),
                SpriteLayer.of("glasses", SpriteLayerCategory.ACCESSORY,       null)
        );
        CharacterDefinition def = new CharacterDefinition("test", layers);
        BufferedImage result = compositor.composite(def, 64, 64);
        assertThat(result).isNotNull();
        assertThat(result.getWidth()).isEqualTo(64);
    }
}
