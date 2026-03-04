package ru.lifegame.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.lifegame.assets.domain.service.PixelArtRenderer;
import ru.lifegame.assets.domain.service.PixelArtRendererRegistry;
import ru.lifegame.demo.dto.DemoDtos.AssetInfoDto;

import javax.imageio.ImageIO;
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
 * Generates pixel-art sprite-atlas PNGs at application startup using
 * registered {@link PixelArtRenderer} implementations from the asset-generator module.
 *
 * <p>Each asset is a <em>horizontal-strip atlas</em>: all frames are laid out
 * left-to-right in a single PNG, with no anti-aliasing (pure nearest-neighbour
 * rendering).</p>
 *
 * <p>Naming conventions:</p>
 * <ul>
 *   <li>Output directory: {@code snake_case}</li>
 *   <li>Asset files: {@code snake_case.png}</li>
 * </ul>
 */
@Service
public class DemoAssetService {

    private static final Logger log = LoggerFactory.getLogger(DemoAssetService.class);

    /** Compact description of one animation that will become a horizontal strip. */
    record SpriteSpec(
            String id,
            int frameWidth,
            int frameHeight,
            int frameCount,
            int frameRate
    ) {}

    private static final List<SpriteSpec> SPECS = List.of(
            new SpriteSpec("tanya_idle",    32, 48,  6, 8),
            new SpriteSpec("tanya_walk",    32, 48,  8, 10),
            new SpriteSpec("sam_idle",      24, 20,  4, 6),
            new SpriteSpec("sam_walk",      24, 20,  6, 8),
            new SpriteSpec("bed_static",    48, 32,  1, 1),
            new SpriteSpec("home_room_bg",  320, 192, 1, 1)
    );

    private final Path outputDir;
    private final PixelArtRendererRegistry rendererRegistry;
    private final Map<String, Path>       assetPaths = new LinkedHashMap<>();
    private final Map<String, SpriteSpec> assetSpecs = new LinkedHashMap<>();

    public DemoAssetService(
            @Value("${demo.assets.output-dir:${java.io.tmpdir}/life-of-t-assets}") String outputDirStr,
            PixelArtRendererRegistry rendererRegistry) {
        this.outputDir = Path.of(outputDirStr);
        this.rendererRegistry = rendererRegistry;
    }

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

    /**
     * Generates a horizontal-strip PNG atlas for the given spec.
     * Delegates frame rendering to the {@link PixelArtRenderer} registered for
     * the sprite id; no more simple coloured rectangles.
     */
    private void generate(SpriteSpec spec, Path dest) {
        int frames = Math.min(spec.frameCount(), 50); // hard cap per project rules
        int atlasWidth  = spec.frameWidth() * frames;
        int atlasHeight = spec.frameHeight();

        BufferedImage atlas = new BufferedImage(atlasWidth, atlasHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = atlas.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        List<BufferedImage> renderedFrames = rendererRegistry.find(spec.id())
                .map(renderer -> renderer.renderFrames(spec.frameWidth(), spec.frameHeight(), frames))
                .orElseGet(() -> {
                    log.warn("No PixelArtRenderer for '{}', using empty frames", spec.id());
                    return emptyFrames(spec.frameWidth(), spec.frameHeight(), frames);
                });

        for (int i = 0; i < frames; i++) {
            BufferedImage frame = renderedFrames.get(i);
            g.drawImage(frame, i * spec.frameWidth(), 0, null);
        }

        g.dispose();

        try {
            ImageIO.write(atlas, "PNG", dest.toFile());
            log.debug("Generated asset: {}", dest);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write asset PNG: " + dest, e);
        }
    }

    private List<BufferedImage> emptyFrames(int w, int h, int count) {
        List<BufferedImage> frames = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            frames.add(new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB));
        }
        return frames;
    }
}
