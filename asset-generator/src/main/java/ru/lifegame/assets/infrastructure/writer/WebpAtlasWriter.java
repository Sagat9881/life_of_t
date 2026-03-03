package ru.lifegame.assets.infrastructure.writer;

import org.springframework.stereotype.Component;
import ru.lifegame.assets.domain.model.asset.AnimationSpec;
import ru.lifegame.assets.domain.model.asset.AssetSpec;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

/**
 * Assembles a WebP sprite atlas from the animation frames defined in an
 * {@link AssetSpec}.
 *
 * <p>The atlas is a <em>horizontal strip</em>: all frames are placed side-
 * by-side on a single row.  Each frame is a solid-colour rectangle whose
 * colour cycles through a predefined palette, producing a visually
 * distinguishable placeholder suitable for engine integration testing.</p>
 *
 * <h2>Fallback behaviour</h2>
 * <p>If the JVM does not provide a native WebP {@link ImageWriter} (which is
 * common in CI environments without a native codec), the writer falls back
 * to PNG.  A warning is printed to {@code System.err} in that case.</p>
 */
@Component
public class WebpAtlasWriter {

    private static final Color[] FRAME_COLORS = {
            new Color(0xFF, 0xCC, 0x00),
            new Color(0x00, 0xCC, 0xFF),
            new Color(0x00, 0xFF, 0x66),
            new Color(0xFF, 0x44, 0x44),
    };

    /**
     * Write a WebP (or PNG fallback) atlas for {@code spec} into
     * {@code outputDir}.
     *
     * @param spec      asset specification
     * @param outputDir target directory; created if absent
     * @throws AtlasWriteException on any unrecoverable I/O error
     */
    public void write(AssetSpec spec, Path outputDir) {
        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            throw new AtlasWriteException("Cannot create output directory: " + outputDir, e);
        }

        int totalFrames = totalFrameCount(spec.animations());
        if (totalFrames == 0) return;          // nothing to write

        int frameW = spec.constraints() != null ? spec.constraints().widthPx()  : 64;
        int frameH = spec.constraints() != null ? spec.constraints().heightPx() : 64;

        BufferedImage atlas = buildAtlas(totalFrames, frameW, frameH);
        Path dest = outputDir.resolve(atlasFileName(spec));
        writeAtlas(atlas, dest);
    }

    // -----------------------------------------------------------------------
    // Internals
    // -----------------------------------------------------------------------

    private int totalFrameCount(List<AnimationSpec> animations) {
        return animations.stream()
                         .mapToInt(AnimationSpec::frameCount)
                         .sum();
    }

    private BufferedImage buildAtlas(int frames, int frameW, int frameH) {
        BufferedImage img = new BufferedImage(
                frames * frameW, frameH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        for (int i = 0; i < frames; i++) {
            g.setColor(FRAME_COLORS[i % FRAME_COLORS.length]);
            g.fillRect(i * frameW, 0, frameW, frameH);
        }
        g.dispose();
        return img;
    }

    private String atlasFileName(AssetSpec spec) {
        if (spec.naming() != null && spec.naming().atlasPattern() != null) {
            return String.format(spec.naming().atlasPattern(), spec.naming().prefix());
        }
        return spec.id() + "_atlas.webp";
    }

    private void writeAtlas(BufferedImage image, Path dest) {
        // Try native WebP writer first
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/webp");
        if (writers.hasNext()) {
            ImageWriter writer = writers.next();
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(dest.toFile())) {
                writer.setOutput(ios);
                writer.write(image);
            } catch (IOException e) {
                throw new AtlasWriteException("WebP write failed: " + dest, e);
            } finally {
                writer.dispose();
            }
            return;
        }

        // Fallback: PNG
        System.err.println("[WebpAtlasWriter] No native WebP codec found — falling back to PNG.");
        Path pngDest = dest.resolveSibling(
                dest.getFileName().toString().replaceAll("\\.webp$", ".png"));
        try {
            ImageIO.write(image, "PNG", pngDest.toFile());
        } catch (IOException e) {
            throw new AtlasWriteException("PNG fallback write failed: " + pngDest, e);
        }
    }

    // -----------------------------------------------------------------------
    // Exception
    // -----------------------------------------------------------------------

    public static class AtlasWriteException extends RuntimeException {
        public AtlasWriteException(String msg, Throwable cause) { super(msg, cause); }
    }
}
