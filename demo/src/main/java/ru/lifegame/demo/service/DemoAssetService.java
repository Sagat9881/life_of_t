package ru.lifegame.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.lifegame.demo.dto.DemoDtos.AssetInfoDto;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates placeholder pixel-art sprite-atlas PNGs at application startup.
 *
 * <p>Each asset is a <em>horizontal-strip atlas</em>: all frames are laid out
 * left-to-right in a single PNG, with no anti-aliasing (pure nearest-neighbour
 * rendering). JavaScript in {@code index.html} animates the strip by cycling
 * the CSS {@code background-position} property.</p>
 *
 * <p>Naming conventions:</p>
 * <ul>
 *   <li>Output directory: {@code snake_case}</li>
 *   <li>Asset files: {@code snake_case.png}</li>
 * </ul>
 *
 * <p>The asset specs are kept here as inline Java records to avoid dependency
 * on asset-generator XML parsing at runtime; the XML specs under
 * {@code docs/prompts/} serve as the authoritative source for a future
 * AI-generated art pipeline.</p>
 */
@Service
public class DemoAssetService {

    private static final Logger log = LoggerFactory.getLogger(DemoAssetService.class);

    // -----------------------------------------------------------------------
    // Inline asset spec (replaces XML parsing for runtime generation)
    // -----------------------------------------------------------------------

    /** Compact description of one animation that will become a horizontal strip. */
    record SpriteSpec(
            String id,
            int frameWidth,
            int frameHeight,
            int frameCount,
            int frameRate,
            Color baseColor
    ) {}

    private static final List<SpriteSpec> SPECS = List.of(
            new SpriteSpec("tanya_idle",    32, 48,  6, 8,  new Color(0x4A90D9)),
            new SpriteSpec("tanya_walk",    32, 48,  8, 10, new Color(0x3A7DC8)),
            new SpriteSpec("sam_idle",      24, 20,  4, 6,  new Color(0xC8A04A)),
            new SpriteSpec("sam_walk",      24, 20,  6, 8,  new Color(0xB88E3A)),
            new SpriteSpec("bed_static",    48, 32,  1, 1,  new Color(0x8B5E3C)),
            new SpriteSpec("home_room_bg",  320, 192, 1, 1,  new Color(0x2E3B4E))
    );

    // -----------------------------------------------------------------------
    // State
    // -----------------------------------------------------------------------

    private final Path outputDir;
    /** Maps asset id -> generated PNG path. */
    private final Map<String, Path>         assetPaths = new LinkedHashMap<>();
    /** Maps asset id -> spec (for metadata queries). */
    private final Map<String, SpriteSpec>   assetSpecs = new LinkedHashMap<>();

    public DemoAssetService(
            @Value("${demo.assets.output-dir:${java.io.tmpdir}/life-of-t-assets}") String outputDirStr) {
        this.outputDir = Path.of(outputDirStr);
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Generates all assets. Idempotent – skips files that already exist.
     */
    public void generateAll() {
        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create asset output dir: " + outputDir, e);
        }

        for (SpriteSpec spec : SPECS) {
            Path dest = outputDir.resolve(spec.id() + ".png");
            assetSpecs.put(spec.id(), spec);
            if (Files.exists(dest)) {
                log.debug("Asset already exists, skipping: {}", dest);
                assetPaths.put(spec.id(), dest);
                continue;
            }
            generate(spec, dest);
            assetPaths.put(spec.id(), dest);
        }

        log.info("Asset generation complete. {} assets in {}", assetPaths.size(), outputDir);
    }

    /** Returns metadata DTOs for all generated assets. */
    public List<AssetInfoDto> listAssetInfos() {
        List<AssetInfoDto> result = new ArrayList<>();
        for (Map.Entry<String, SpriteSpec> entry : assetSpecs.entrySet()) {
            SpriteSpec s = entry.getValue();
            result.add(new AssetInfoDto(
                    s.id(),
                    "/api/assets/" + s.id() + ".png",
                    s.frameWidth(),
                    s.frameHeight(),
                    s.frameCount(),
                    s.frameRate()
            ));
        }
        return result;
    }

    /**
     * Resolves the filesystem path for the PNG atlas of the given asset id,
     * or {@code null} if not generated yet.
     */
    public Path resolveAssetPath(String id) {
        return assetPaths.get(id);
    }

    /** Absolute path to the generated assets directory. */
    public Path getOutputDir() {
        return outputDir;
    }

    // -----------------------------------------------------------------------
    // PNG generation
    // -----------------------------------------------------------------------

    /**
     * Generates a horizontal-strip PNG atlas for {@code spec}.
     *
     * <p>Pixel-art rules:
     * <ul>
     *   <li>No anti-aliasing (VALUE_ANTIALIAS_OFF)</li>
     *   <li>Nearest-neighbour interpolation (VALUE_INTERPOLATION_NEAREST_NEIGHBOR)</li>
     *   <li>Each frame is a solid-colour rectangle with a simple pixel silhouette</li>
     *   <li>Total atlas width = frameWidth × frameCount</li>
     *   <li>Maximum 50 frames enforced</li>
     * </ul>
     */
    private void generate(SpriteSpec spec, Path dest) {
        int frames = Math.min(spec.frameCount(), 50); // hard cap per project rules
        int atlasWidth  = spec.frameWidth() * frames;
        int atlasHeight = spec.frameHeight();

        BufferedImage atlas = new BufferedImage(atlasWidth, atlasHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = atlas.createGraphics();

        // Disable anti-aliasing – pixel art must be crisp
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_SPEED);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,   RenderingHints.VALUE_COLOR_RENDER_SPEED);

        // Transparent background
        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, atlasWidth, atlasHeight);

        for (int i = 0; i < frames; i++) {
            int x = i * spec.frameWidth();
            drawPixelFrame(g, spec, x, i, frames);
        }

        g.dispose();

        try {
            ImageIO.write(atlas, "PNG", dest.toFile());
            log.debug("Generated asset: {}", dest);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write asset PNG: " + dest, e);
        }
    }

    /**
     * Draws a single pixel-art frame at the given x offset.
     * Creates a slight colour shift per frame to create visible animation.
     */
    private void drawPixelFrame(Graphics2D g, SpriteSpec spec,
                                int xOffset, int frameIndex, int totalFrames) {
        int fw = spec.frameWidth();
        int fh = spec.frameHeight();
        Color base = spec.baseColor();

        // Slight brightness oscillation between frames (no blending, pure integer)
        float brightness = 0.75f + 0.25f * ((float) frameIndex / Math.max(totalFrames - 1, 1));
        Color frameColor = scaleBrightness(base, brightness);

        // Body rectangle (75% width, 80% height, centred)
        int bw = (int)(fw * 0.75);
        int bh = (int)(fh * 0.80);
        int bx = xOffset + (fw - bw) / 2;
        int by = (fh - bh) / 2;
        g.setColor(frameColor);
        g.fillRect(bx, by, bw, bh);

        // Head dot (top-centre, 6×6 px, lighter)
        if (fh >= 20) {
            int hw = 6;
            int hx = xOffset + (fw - hw) / 2;
            int hy = by - hw - 1;
            if (hy >= 0) {
                g.setColor(scaleBrightness(frameColor, 1.3f));
                g.fillRect(hx, hy, hw, hw);
            }
        }

        // 1-pixel outline
        g.setColor(scaleBrightness(base, 0.4f));
        g.drawRect(bx, by, bw - 1, bh - 1);
    }

    /** Scales each RGB channel by {@code factor}, clamping to [0,255]. No blending. */
    private static Color scaleBrightness(Color c, float factor) {
        int r = Math.min(255, Math.max(0, (int)(c.getRed()   * factor)));
        int g = Math.min(255, Math.max(0, (int)(c.getGreen() * factor)));
        int b = Math.min(255, Math.max(0, (int)(c.getBlue()  * factor)));
        return new Color(r, g, b);
    }
}
