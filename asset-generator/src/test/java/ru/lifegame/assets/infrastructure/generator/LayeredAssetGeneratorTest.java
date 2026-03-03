package ru.lifegame.assets.infrastructure.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.lifegame.assets.domain.model.asset.AnimationSpec;
import ru.lifegame.assets.domain.model.asset.AssetConstraints;
import ru.lifegame.assets.domain.model.asset.AssetLayer;
import ru.lifegame.assets.domain.model.asset.AssetSpec;
import ru.lifegame.assets.domain.model.asset.ColorPalette;
import ru.lifegame.assets.domain.model.asset.NamingSpec;
import ru.lifegame.assets.infrastructure.writer.AtlasConfigWriter;
import ru.lifegame.assets.infrastructure.writer.PngLayerWriter;
import ru.lifegame.assets.infrastructure.writer.WebpAtlasWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LayeredAssetGenerator — генерация PNG по XML-спеке")
class LayeredAssetGeneratorTest {

    private LayeredAssetGenerator generator;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        generator = new LayeredAssetGenerator(
                new PngLayerWriter(),
                new WebpAtlasWriter(),
                new AtlasConfigWriter()
        );
    }

    @Test
    @DisplayName("Генерация локации: 3 слоя (background, midground, foreground)")
    void generateLocationLayers() {
        AssetSpec spec = locationSpec();
        List<Path> files = generator.generateAsset(spec, tempDir);

        // 3 layers = 3 PNG files
        long pngCount = files.stream()
                .filter(p -> p.toString().endsWith(".png"))
                .filter(p -> !p.toString().contains("atlas"))
                .count();
        assertThat(pngCount).isEqualTo(3);
    }

    @Test
    @DisplayName("Сгенерированные PNG — формат RGBA 32-bit")
    void generatedPngIsRgba32bit() throws Exception {
        AssetSpec spec = locationSpec();
        List<Path> files = generator.generateAsset(spec, tempDir);

        Path firstPng = files.stream()
                .filter(p -> p.toString().endsWith(".png"))
                .filter(p -> !p.toString().contains("atlas"))
                .findFirst().orElseThrow();

        BufferedImage image = ImageIO.read(firstPng.toFile());
        assertThat(image).isNotNull();
        // After PNG round-trip, ImageIO may decode as TYPE_4BYTE_ABGR (6)
        // instead of TYPE_INT_ARGB (2). Both are 32-bit with alpha.
        assertThat(image.getColorModel().hasAlpha()).isTrue();
        assertThat(image.getColorModel().getPixelSize()).isEqualTo(32);
    }

    @Test
    @DisplayName("Размеры слоёв соответствуют ожидаемым")
    void layerDimensionsCorrect() throws Exception {
        AssetSpec spec = locationSpec();
        List<Path> files = generator.generateAsset(spec, tempDir);

        for (Path file : files) {
            if (file.toString().endsWith(".png") && !file.toString().contains("atlas")) {
                BufferedImage image = ImageIO.read(file.toFile());
                assertThat(image.getWidth()).isEqualTo(128);
                assertThat(image.getHeight()).isEqualTo(128);
            }
        }
    }

    @Test
    @DisplayName("Генерация с анимацией создаёт atlas файлы")
    void generateWithAnimationCreatesAtlas() {
        AssetSpec spec = characterSpec();
        List<Path> files = generator.generateAsset(spec, tempDir);

        long atlasCount = files.stream()
                .filter(p -> p.toString().contains("atlas"))
                .filter(p -> p.toString().endsWith(".png"))
                .count();
        assertThat(atlasCount).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Генерация с анимацией создаёт atlas-config.json")
    void generateWithAnimationCreatesConfig() {
        AssetSpec spec = characterSpec();
        List<Path> files = generator.generateAsset(spec, tempDir);

        long configCount = files.stream()
                .filter(p -> p.toString().endsWith(".json"))
                .count();
        assertThat(configCount).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Слой background генерируется как прозрачный RGBA")
    void backgroundLayerHasTransparency() throws Exception {
        AssetSpec spec = locationSpec();
        List<Path> files = generator.generateAsset(spec, tempDir);

        Path bgFile = files.stream()
                .filter(p -> p.getFileName().toString().equals("background.png"))
                .findFirst().orElseThrow();

        BufferedImage image = ImageIO.read(bgFile.toFile());
        // Check that at least one pixel has alpha < 255 (transparent area)
        boolean hasTransparentPixel = false;
        for (int y = 0; y < image.getHeight() && !hasTransparentPixel; y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int alpha = (image.getRGB(x, y) >> 24) & 0xFF;
                if (alpha < 255) {
                    hasTransparentPixel = true;
                    break;
                }
            }
        }
        assertThat(hasTransparentPixel).isTrue();
    }

    @Test
    @DisplayName("generateLayerImage возвращает ARGB BufferedImage")
    void generateLayerImageReturnsArgb() {
        AssetLayer layer = new AssetLayer("test", "background", "test layer", 0);
        AssetSpec spec = locationSpec();
        BufferedImage image = generator.generateLayerImage(layer, spec.colorPalette(), spec);

        assertThat(image.getType()).isEqualTo(BufferedImage.TYPE_INT_ARGB);
    }

    @Test
    @DisplayName("generateAnimationFrames возвращает правильное количество кадров")
    void generateAnimationFramesCorrectCount() {
        AnimationSpec animSpec = new AnimationSpec("idle", 24, 12, true, 128, 128);
        List<BufferedImage> frames = generator.generateAnimationFrames(
                animSpec, ColorPalette.projectDefault());

        assertThat(frames).hasSize(24);
    }

    @Test
    @DisplayName("Кадры анимации одинакового размера")
    void animationFramesSameSize() {
        AnimationSpec animSpec = new AnimationSpec("walk", 20, 12, true, 64, 64);
        List<BufferedImage> frames = generator.generateAnimationFrames(
                animSpec, ColorPalette.projectDefault());

        for (BufferedImage frame : frames) {
            assertThat(frame.getWidth()).isEqualTo(64);
            assertThat(frame.getHeight()).isEqualTo(64);
        }
    }

    // --- Test fixtures ---

    private AssetSpec locationSpec() {
        return new AssetSpec(
                "locations", "home", "1.0.0",
                List.of(
                        new AssetLayer("background", "background", "Far wall", 0),
                        new AssetLayer("midground", "midground", "Main space", 1),
                        new AssetLayer("foreground", "foreground", "Near elements", 2)
                ),
                ColorPalette.projectDefault(),
                List.of(),
                List.of(),
                new NamingSpec("locations", "home", "assets/locations/home"),
                AssetConstraints.defaults()
        );
    }

    private AssetSpec characterSpec() {
        return new AssetSpec(
                "characters", "tanya", "1.0.0",
                List.of(
                        new AssetLayer("base", "base", "Base body", 0),
                        new AssetLayer("outfit", "outfit", "Clothing", 1)
                ),
                ColorPalette.projectDefault(),
                List.of(
                        new AnimationSpec("idle", 24, 12, true, 128, 128)
                ),
                List.of(),
                new NamingSpec("characters", "tanya", "assets/characters/tanya"),
                AssetConstraints.defaults()
        );
    }
}
