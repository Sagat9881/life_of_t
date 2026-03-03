package ru.lifegame.assets.texture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.lifegame.assets.AssetRequest;
import ru.lifegame.assets.AssetType;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Тесты ProceduralTextureGenerator")
class ProceduralTextureGeneratorTest {

    private ProceduralTextureGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new ProceduralTextureGenerator();
    }

    @Test
    void testName() {
        assertThat(generator.name()).isEqualTo("ProceduralTextureGenerator");
    }

    @Test
    void testGeneration_32x32() {
        AssetRequest request = new AssetRequest(AssetType.TEXTURE, "test", 32, 32, Map.of("seed", "1"));
        BufferedImage image = generator.generate(request);
        assertThat(image.getWidth()).isEqualTo(32);
        assertThat(image.getHeight()).isEqualTo(32);
    }

    @Test
    void testGeneration_oddSize() {
        AssetRequest request = new AssetRequest(AssetType.TEXTURE, "test", 17, 23, Map.of("seed", "5"));
        BufferedImage image = generator.generate(request);
        assertThat(image.getWidth()).isEqualTo(17);
        assertThat(image.getHeight()).isEqualTo(23);
    }

    @Test
    void testGeneration_64x64() {
        AssetRequest request = new AssetRequest(AssetType.TEXTURE, "big", 64, 64, Map.of("seed", "2"));
        BufferedImage image = generator.generate(request);
        assertThat(image.getWidth()).isEqualTo(64);
        assertThat(image.getHeight()).isEqualTo(64);
    }

    @Test
    void testVerticalSymmetry_pixelByPixel() {
        AssetRequest request = new AssetRequest(AssetType.TEXTURE, "sym", 32, 32,
                Map.of("symmetry", "vertical", "seed", "42", "palette", "wood"));
        BufferedImage image = generator.generate(request);
        int w = image.getWidth();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < w / 2; x++) {
                int leftPixel  = image.getRGB(x, y);
                int rightPixel = image.getRGB(w - 1 - x, y);
                assertThat(leftPixel).isEqualTo(rightPixel);
            }
        }
    }

    @Test
    void testQuadSymmetry_allQuadrantsMatch() {
        AssetRequest request = new AssetRequest(AssetType.TEXTURE, "quad", 32, 32,
                Map.of("symmetry", "quad", "seed", "77", "palette", "stone"));
        BufferedImage image = generator.generate(request);
        int w = image.getWidth();
        int h = image.getHeight();
        int hw = w / 2;
        int hh = h / 2;
        for (int y = 0; y < hh; y++) {
            for (int x = 0; x < hw; x++) {
                int topLeft     = image.getRGB(x, y);
                int topRight    = image.getRGB(w - 1 - x, y);
                int bottomLeft  = image.getRGB(x, h - 1 - y);
                int bottomRight = image.getRGB(w - 1 - x, h - 1 - y);
                assertThat(topLeft).isEqualTo(topRight);
                assertThat(topLeft).isEqualTo(bottomLeft);
                assertThat(topLeft).isEqualTo(bottomRight);
            }
        }
    }

    @Test
    void testSameSeed_identicalImages() {
        Map<String, String> params = Map.of("seed", "12345", "symmetry", "none");
        AssetRequest r1 = new AssetRequest(AssetType.TEXTURE, "t1", 32, 32, params);
        AssetRequest r2 = new AssetRequest(AssetType.TEXTURE, "t2", 32, 32, params);
        BufferedImage img1 = generator.generate(r1);
        BufferedImage img2 = generator.generate(r2);
        assertThat(imagesEqual(img1, img2)).isTrue();
    }

    @Test
    void testDifferentSeeds_differentImages() {
        AssetRequest r1 = new AssetRequest(AssetType.TEXTURE, "t1", 32, 32, Map.of("seed", "100", "symmetry", "none"));
        AssetRequest r2 = new AssetRequest(AssetType.TEXTURE, "t2", 32, 32, Map.of("seed", "200", "symmetry", "none"));
        BufferedImage img1 = generator.generate(r1);
        BufferedImage img2 = generator.generate(r2);
        assertThat(imagesEqual(img1, img2)).isFalse();
    }

    @Test
    void testFillProbabilityZero_allBackground() {
        TextureColorPalette palette = TextureColorPalette.wood();
        int expectedRgb = palette.bg().getRGB();
        AssetRequest request = new AssetRequest(AssetType.TEXTURE, "empty", 32, 32,
                Map.of("fill_probability", "0.0", "symmetry", "none", "seed", "1", "palette", "wood"));
        BufferedImage image = generator.generate(request);
        for (int y = 0; y < image.getHeight(); y++)
            for (int x = 0; x < image.getWidth(); x++)
                assertThat(image.getRGB(x, y)).isEqualTo(expectedRgb);
    }

    @Test
    void testFillProbabilityOne_allFilled() {
        TextureColorPalette palette = TextureColorPalette.wood();
        int bgRgb = palette.bg().getRGB();
        AssetRequest request = new AssetRequest(AssetType.TEXTURE, "full", 32, 32,
                Map.of("fill_probability", "1.0", "symmetry", "none", "seed", "1", "palette", "wood"));
        BufferedImage image = generator.generate(request);
        for (int y = 0; y < image.getHeight(); y++)
            for (int x = 0; x < image.getWidth(); x++)
                assertThat(image.getRGB(x, y)).isNotEqualTo(bgRgb);
    }

    @Test
    void testHorizontalSymmetry_pixelByPixel() {
        AssetRequest request = new AssetRequest(AssetType.TEXTURE, "hflip", 32, 32,
                Map.of("symmetry", "horizontal", "seed", "33", "palette", "fabric"));
        BufferedImage image = generator.generate(request);
        int h = image.getHeight();
        for (int y = 0; y < h / 2; y++)
            for (int x = 0; x < image.getWidth(); x++)
                assertThat(image.getRGB(x, y)).isEqualTo(image.getRGB(x, h - 1 - y));
    }

    @Test
    void testCustomColors_overridePalette() {
        AssetRequest request = new AssetRequest(AssetType.TEXTURE, "custom", 32, 32,
                Map.of("fill_probability", "0.5", "symmetry", "none", "seed", "7",
                       "fg_color", "#FF0000", "bg_color", "#0000FF", "outline_color", "#880000"));
        BufferedImage image = generator.generate(request);
        int redRgb  = new Color(0xFF, 0x00, 0x00).getRGB();
        int blueRgb = new Color(0x00, 0x00, 0xFF).getRGB();
        boolean foundRed = false, foundBlue = false;
        for (int y = 0; y < image.getHeight(); y++)
            for (int x = 0; x < image.getWidth(); x++) {
                if (image.getRGB(x, y) == redRgb) foundRed = true;
                if (image.getRGB(x, y) == blueRgb) foundBlue = true;
            }
        assertThat(foundRed).isTrue();
        assertThat(foundBlue).isTrue();
    }

    private boolean imagesEqual(BufferedImage a, BufferedImage b) {
        if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) return false;
        for (int y = 0; y < a.getHeight(); y++)
            for (int x = 0; x < a.getWidth(); x++)
                if (a.getRGB(x, y) != b.getRGB(x, y)) return false;
        return true;
    }
}
