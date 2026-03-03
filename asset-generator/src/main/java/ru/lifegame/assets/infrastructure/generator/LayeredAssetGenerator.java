package ru.lifegame.assets.infrastructure.generator;

import org.springframework.stereotype.Component;
import ru.lifegame.assets.domain.model.asset.*;
import ru.lifegame.assets.domain.service.AssetGenerationService;
import ru.lifegame.assets.infrastructure.writer.AtlasConfigWriter;
import ru.lifegame.assets.infrastructure.writer.PngLayerWriter;
import ru.lifegame.assets.infrastructure.writer.WebpAtlasWriter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Primary implementation of {@link AssetGenerationService}.
 *
 * <p>Orchestrates the full pipeline for a single asset:</p>
 * <ol>
 *   <li>Create the output directory.</li>
 *   <li>Resolve colour palettes.</li>
 *   <li>Apply time-of-day colour shifts.</li>
 *   <li>Write each layer as a PNG via {@link PngLayerWriter}.</li>
 *   <li>Assemble the WebP sprite atlas via {@link WebpAtlasWriter}.</li>
 *   <li>Write the JSON atlas config via {@link AtlasConfigWriter}.</li>
 * </ol>
 *
 * <p>All palette resolution and tinting is done in-memory; the writer
 * components handle all I/O.</p>
 */
@Component
public class LayeredAssetGenerator implements AssetGenerationService {

    private final PngLayerWriter    pngWriter;
    private final WebpAtlasWriter   atlasWriter;
    private final AtlasConfigWriter configWriter;

    public LayeredAssetGenerator(PngLayerWriter pngWriter,
                                 WebpAtlasWriter atlasWriter,
                                 AtlasConfigWriter configWriter) {
        this.pngWriter    = pngWriter;
        this.atlasWriter  = atlasWriter;
        this.configWriter = configWriter;
    }

    // -----------------------------------------------------------------------
    // AssetGenerationService
    // -----------------------------------------------------------------------

    @Override
    public void generate(AssetSpec spec, Path outputDir) {
        createOutputDir(outputDir);

        Map<String, Color> resolvedPalettes = resolvePalettes(spec.palettes());
        List<AssetLayer>   visibleLayers    = resolveVisibleLayers(spec, resolvedPalettes);
        AssetSpec          tinted           = applyTimeOfDayVariation(spec, resolvedPalettes);

        pngWriter.write(tinted, outputDir);
        atlasWriter.write(tinted, outputDir);
        configWriter.write(tinted, outputDir);
    }

    // -----------------------------------------------------------------------
    // Palette resolution
    // -----------------------------------------------------------------------

    /**
     * Build a name → {@link Color} map from the first colour in each palette.
     */
    Map<String, Color> resolvePalettes(List<ColorPalette> palettes) {
        return palettes.stream().collect(Collectors.toMap(
                ColorPalette::name,
                p -> parseHex(p.colors().isEmpty() ? "#808080" : p.colors().get(0))
        ));
    }

    /**
     * Filter out optional layers whose palette reference cannot be resolved.
     */
    List<AssetLayer> resolveVisibleLayers(AssetSpec spec,
                                          Map<String, Color> palettes) {
        return spec.layers().stream()
                   .filter(layer -> !layer.optional()
                           || layer.paletteRef() == null
                           || layer.paletteRef().isBlank()
                           || palettes.containsKey(layer.paletteRef()))
                   .toList();
    }

    // -----------------------------------------------------------------------
    // Time-of-day tinting
    // -----------------------------------------------------------------------

    /**
     * Return a new {@link AssetSpec} with colours adjusted for the first
     * time-of-day variation, or the original spec if no variations are
     * defined.
     */
    AssetSpec applyTimeOfDayVariation(AssetSpec spec,
                                      Map<String, Color> palettes) {
        if (spec.variations().isEmpty()) return spec;

        TimeOfDayVariation variation = spec.variations().get(0);
        Color              tint      = parseHex(variation.colorShift());
        float              alpha     = (float) variation.opacity();

        List<ColorPalette> tinted = spec.palettes().stream()
                .map(p -> tintPalette(p, tint, alpha))
                .toList();

        return new AssetSpec(
                spec.id(), spec.category(), spec.description(),
                spec.layers(), spec.animations(), tinted, spec.variations(),
                spec.naming(), spec.constraints());
    }

    private ColorPalette tintPalette(ColorPalette palette, Color tint, float alpha) {
        List<String> tinted = palette.colors().stream()
                .map(hex -> blendHex(hex, tint, alpha))
                .toList();
        return new ColorPalette(palette.name(), tinted);
    }

    // -----------------------------------------------------------------------
    // Colour helpers
    // -----------------------------------------------------------------------

    Color parseHex(String hex) {
        String clean = hex.startsWith("#") ? hex.substring(1) : hex;
        int rgb = Integer.parseInt(clean, 16);
        return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
    }

    String blendHex(String baseHex, Color tint, float alpha) {
        Color base = parseHex(baseHex);
        int r = Math.round(base.getRed()   * (1 - alpha) + tint.getRed()   * alpha);
        int g = Math.round(base.getGreen() * (1 - alpha) + tint.getGreen() * alpha);
        int b = Math.round(base.getBlue()  * (1 - alpha) + tint.getBlue()  * alpha);
        return String.format("#%02X%02X%02X", r, g, b);
    }

    // -----------------------------------------------------------------------
    // I/O helpers
    // -----------------------------------------------------------------------

    private void createOutputDir(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new AssetGenerationException("Cannot create output directory: " + dir, e);
        }
    }

    // -----------------------------------------------------------------------
    // Exception
    // -----------------------------------------------------------------------

    public static class AssetGenerationException extends RuntimeException {
        public AssetGenerationException(String msg, Throwable cause) { super(msg, cause); }
    }
}
