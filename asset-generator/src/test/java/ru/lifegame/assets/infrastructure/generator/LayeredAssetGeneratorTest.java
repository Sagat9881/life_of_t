package ru.lifegame.assets.infrastructure.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.lifegame.assets.domain.model.asset.*;
import ru.lifegame.assets.infrastructure.writer.AtlasConfigWriter;
import ru.lifegame.assets.infrastructure.writer.PngLayerWriter;
import ru.lifegame.assets.infrastructure.writer.WebpAtlasWriter;

import java.awt.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link LayeredAssetGenerator}.
 *
 * <p>Covers palette resolution, layer visibility filtering, time-of-day
 * tinting, hex parsing, colour blending, and the top-level
 * {@code generate()} orchestration (via a real temp directory).</p>
 */
class LayeredAssetGeneratorTest {

    @TempDir
    Path tempDir;

    private LayeredAssetGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new LayeredAssetGenerator(
                new PngLayerWriter(),
                new WebpAtlasWriter(),
                new AtlasConfigWriter()
        );
    }

    // -----------------------------------------------------------------------
    // resolvePalettes
    // -----------------------------------------------------------------------

    @Test
    void resolvePalettes_returnsMappedColors() {
        List<ColorPalette> palettes = List.of(
                new ColorPalette("skin", List.of("#FFCCAA")),
                new ColorPalette("hair", List.of("#331100"))
        );
        Map<String, Color> resolved = generator.resolvePalettes(palettes);

        assertThat(resolved).containsKey("skin");
        assertThat(resolved).containsKey("hair");
        assertThat(resolved.get("skin")).isEqualTo(new Color(0xFF, 0xCC, 0xAA));
        assertThat(resolved.get("hair")).isEqualTo(new Color(0x33, 0x11, 0x00));
    }

    @Test
    void resolvePalettes_emptyPaletteColors_usesGrey() {
        List<ColorPalette> palettes = List.of(
                new ColorPalette("empty", List.of())
        );
        Map<String, Color> resolved = generator.resolvePalettes(palettes);
        assertThat(resolved.get("empty")).isEqualTo(new Color(0x80, 0x80, 0x80));
    }

    @Test
    void resolvePalettes_emptyInput_returnsEmptyMap() {
        assertThat(generator.resolvePalettes(List.of())).isEmpty();
    }

    // -----------------------------------------------------------------------
    // resolveVisibleLayers
    // -----------------------------------------------------------------------

    @Test
    void resolveVisibleLayers_includesNonOptionalLayersAlways() {
        AssetSpec spec = minimalSpec(List.of(
                new AssetLayer("base", "base", null, 0, false)
        ));
        Map<String, Color> palettes = Map.of();
        List<AssetLayer> visible = generator.resolveVisibleLayers(spec, palettes);
        assertThat(visible).hasSize(1);
    }

    @Test
    void resolveVisibleLayers_excludesOptionalLayerWithMissingPaletteRef() {
        AssetSpec spec = minimalSpec(List.of(
                new AssetLayer("overlay", "overlay", "missing_palette", 1, true)
        ));
        Map<String, Color> palettes = Map.of(); // palette not present
        List<AssetLayer> visible = generator.resolveVisibleLayers(spec, palettes);
        assertThat(visible).isEmpty();
    }

    @Test
    void resolveVisibleLayers_includesOptionalLayerWhenPaletteExists() {
        AssetSpec spec = minimalSpec(List.of(
                new AssetLayer("overlay", "overlay", "skin", 1, true)
        ));
        Map<String, Color> palettes = Map.of("skin", Color.RED);
        List<AssetLayer> visible = generator.resolveVisibleLayers(spec, palettes);
        assertThat(visible).hasSize(1);
    }

    // -----------------------------------------------------------------------
    // applyTimeOfDayVariation
    // -----------------------------------------------------------------------

    @Test
    void applyTimeOfDayVariation_noVariations_returnsSameSpec() {
        AssetSpec spec = minimalSpec(List.of());
        AssetSpec result = generator.applyTimeOfDayVariation(spec, Map.of());
        assertThat(result).isSameAs(spec);
    }

    @Test
    void applyTimeOfDayVariation_withVariation_tintsPalettes() {
        List<ColorPalette> palettes = List.of(
                new ColorPalette("skin", List.of("#FFFFFF"))
        );
        List<TimeOfDayVariation> variations = List.of(
                new TimeOfDayVariation("evening", "#FF0000", 0.5)
        );
        AssetSpec spec = new AssetSpec(
                "test", "character", "desc",
                List.of(), List.of(), palettes, variations,
                null, defaultConstraints());

        Map<String, Color> resolved = generator.resolvePalettes(palettes);
        AssetSpec tinted = generator.applyTimeOfDayVariation(spec, resolved);

        // palette should be modified; verify first colour is now a blend
        String blended = tinted.palettes().get(0).colors().get(0);
        assertThat(blended).isNotEqualTo("#FFFFFF");
    }

    // -----------------------------------------------------------------------
    // parseHex + blendHex
    // -----------------------------------------------------------------------

    @Test
    void parseHex_withHash_parsesCorrectly() {
        Color c = generator.parseHex("#FF8800");
        assertThat(c.getRed()).isEqualTo(0xFF);
        assertThat(c.getGreen()).isEqualTo(0x88);
        assertThat(c.getBlue()).isEqualTo(0x00);
    }

    @Test
    void parseHex_withoutHash_parsesCorrectly() {
        Color c = generator.parseHex("00FF00");
        assertThat(c.getGreen()).isEqualTo(0xFF);
    }

    @Test
    void blendHex_zeroAlpha_returnsBase() {
        String blended = generator.blendHex("#FF0000", Color.BLUE, 0f);
        assertThat(blended).isEqualToIgnoringCase("#FF0000");
    }

    @Test
    void blendHex_fullAlpha_returnsTint() {
        String blended = generator.blendHex("#FF0000", new Color(0, 255, 0), 1f);
        assertThat(blended).isEqualToIgnoringCase("#00FF00");
    }

    @Test
    void blendHex_halfAlpha_blendsMidpoint() {
        String blended = generator.blendHex("#FF0000", new Color(0xFF, 0xFF, 0x00), 0.5f);
        // Red: 255, Green: 127 or 128, Blue: 0
        assertThat(blended).startsWith("#FF");
    }

    // -----------------------------------------------------------------------
    // generate() integration
    // -----------------------------------------------------------------------

    @Test
    void generate_createsOutputDirectory() {
        AssetSpec spec = minimalSpec(List.of(
                new AssetLayer("base", "base", null, 0, false)
        ));
        Path outDir = tempDir.resolve("output");
        generator.generate(spec, outDir);
        assertThat(outDir).isDirectory();
    }

    @Test
    void generate_writesPngForEachLayer() {
        AssetSpec spec = minimalSpec(List.of(
                new AssetLayer("base", "base", null, 0, false),
                new AssetLayer("eyes", "overlay", null, 1, false)
        ));
        generator.generate(spec, tempDir);
        long pngCount = countFiles(tempDir, ".png");
        assertThat(pngCount).isGreaterThanOrEqualTo(2);
    }

    @Test
    void generate_writesAtlasConfig() {
        AnimationSpec anim = new AnimationSpec("idle", 2, 8, true, List.of(0, 1));
        AssetSpec spec = new AssetSpec(
                "hero", "character", "desc",
                List.of(new AssetLayer("base", "base", null, 0, false)),
                List.of(anim),
                List.of(), List.of(),
                null, defaultConstraints());
        generator.generate(spec, tempDir);
        long jsonCount = countFiles(tempDir, ".json");
        assertThat(jsonCount).isGreaterThanOrEqualTo(1);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private AssetSpec minimalSpec(List<AssetLayer> layers) {
        return new AssetSpec(
                "test_asset", "character", "A test asset",
                layers, List.of(), List.of(), List.of(),
                null, defaultConstraints());
    }

    private AssetConstraints defaultConstraints() {
        return new AssetConstraints(64, 64, 16, true, "png,webp");
    }

    private long countFiles(Path dir, String ext) {
        try {
            return java.nio.file.Files.walk(dir)
                    .filter(java.nio.file.Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(ext))
                    .count();
        } catch (Exception e) { return 0; }
    }
}
