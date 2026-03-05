package ru.lifegame.assets.infrastructure.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.lifegame.assets.domain.model.asset.*;
import ru.lifegame.assets.infrastructure.writer.AtlasConfigWriter;
import ru.lifegame.assets.infrastructure.writer.PngLayerWriter;
import ru.lifegame.assets.infrastructure.writer.WebpAtlasWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LayeredAssetGenerator — XML-driven generation")
class LayeredAssetGeneratorTest {

    private LayeredAssetGenerator generator;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        generator = new LayeredAssetGenerator(
                new UniversalPixelRenderer(),
                new PngLayerWriter(),
                new WebpAtlasWriter(),
                new AtlasConfigWriter()
        );
    }

    @Test
    @DisplayName("Location: generates composite + layer PNGs")
    void generateLocationLayers() {
        AssetSpec spec = locationSpec();
        List<Path> files = generator.generateAsset(spec, tempDir);

        // composite + 3 layers = 4 PNGs
        long pngCount = files.stream()
                .filter(p -> p.toString().endsWith(".png"))
                .count();
        assertThat(pngCount).isEqualTo(4);
    }

    @Test
    @DisplayName("Generated PNGs are RGBA 32-bit")
    void generatedPngIsRgba32bit() throws Exception {
        AssetSpec spec = locationSpec();
        List<Path> files = generator.generateAsset(spec, tempDir);

        Path firstPng = files.stream()
                .filter(p -> p.toString().endsWith(".png"))
                .findFirst().orElseThrow();

        BufferedImage image = ImageIO.read(firstPng.toFile());
        assertThat(image).isNotNull();
        assertThat(image.getColorModel().hasAlpha()).isTrue();
        assertThat(image.getColorModel().getPixelSize()).isEqualTo(32);
    }

    @Test
    @DisplayName("Character with animation creates atlas + config")
    void generateWithAnimationCreatesAtlas() {
        AssetSpec spec = characterSpec();
        List<Path> files = generator.generateAsset(spec, tempDir);

        long atlasCount = files.stream()
                .filter(p -> p.toString().contains("atlas") && p.toString().endsWith(".png"))
                .count();
        assertThat(atlasCount).isGreaterThanOrEqualTo(1);

        long configCount = files.stream()
                .filter(p -> p.toString().endsWith(".json"))
                .count();
        assertThat(configCount).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Pixel-data layers render actual pixels")
    void pixelDataLayersRenderPixels() throws Exception {
        AssetLayer layer = new AssetLayer("test", "base", "test", 0, 16, 16,
                new PixelData(
                        List.of(new PixelRect(2, 2, 4, 4, "#FF0000")),
                        List.of(),
                        List.of(new PixelDot(0, 0, "#00FF00"))
                ));
        AssetSpec spec = new AssetSpec("test", "pixeltest", "1.0.0",
                List.of(layer), ColorPalette.projectDefault(),
                List.of(), List.of(),
                new NamingSpec("test", "pixeltest", "test/pixeltest"),
                AssetConstraints.defaults());

        List<Path> files = generator.generateAsset(spec, tempDir);
        Path compositePng = files.get(0);
        BufferedImage img = ImageIO.read(compositePng.toFile());

        // green dot at (0,0)
        int greenPixel = img.getRGB(0, 0);
        assertThat((greenPixel >> 8) & 0xFF).isEqualTo(0xFF); // green channel

        // red rect at (2,2)
        int redPixel = img.getRGB(3, 3);
        assertThat((redPixel >> 16) & 0xFF).isEqualTo(0xFF); // red channel
    }

    // --- Fixtures ---

    private AssetSpec locationSpec() {
        return new AssetSpec("locations", "home", "1.0.0",
                List.of(
                        new AssetLayer("bg", "background", "wall", 0, 64, 64,
                                new PixelData(List.of(new PixelRect(0, 0, 64, 64, "#FFF4E6")),
                                        List.of(), List.of())),
                        new AssetLayer("mid", "midground", "space", 1, 64, 64,
                                new PixelData(List.of(new PixelRect(10, 10, 20, 20, "#B0D8F0")),
                                        List.of(), List.of())),
                        new AssetLayer("fg", "foreground", "near", 2, 64, 64,
                                new PixelData(List.of(new PixelRect(5, 40, 30, 10, "#C8A882")),
                                        List.of(), List.of()))
                ),
                ColorPalette.projectDefault(), List.of(), List.of(),
                new NamingSpec("locations", "home", "assets/locations/home"),
                AssetConstraints.defaults());
    }

    private AssetSpec characterSpec() {
        return new AssetSpec("characters", "tanya", "1.0.0",
                List.of(
                        new AssetLayer("head", "base", "head", 0, 32, 48,
                                new PixelData(List.of(new PixelRect(12, 8, 8, 9, "#F0C098")),
                                        List.of(), List.of())),
                        new AssetLayer("hair", "overlay", "hair", 1, 32, 48,
                                new PixelData(List.of(new PixelRect(11, 5, 10, 4, "#801828")),
                                        List.of(), List.of()))
                ),
                ColorPalette.projectDefault(),
                List.of(new AnimationSpec("idle", 4, 8, true, 32, 48)),
                List.of(),
                new NamingSpec("characters", "tanya", "assets/characters/tanya"),
                AssetConstraints.defaults());
    }
}
