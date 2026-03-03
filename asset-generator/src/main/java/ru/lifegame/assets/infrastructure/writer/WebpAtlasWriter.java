package ru.lifegame.assets.infrastructure.writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lifegame.assets.domain.model.asset.AnimationSpec;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Creates horizontal-strip sprite atlas images from a list of animation frames.
 * <p>
 * Primary format is PNG (since Java does not natively support WebP encoding).
 * The generated atlas follows the horizontal-strip convention:
 * all frames placed side-by-side in a single row.
 */
public class WebpAtlasWriter {

    private static final Logger log = LoggerFactory.getLogger(WebpAtlasWriter.class);

    /**
     * Creates a horizontal-strip atlas from the given frames.
     *
     * @param frames    list of individual frame images (must all be same dimensions)
     * @param spec      animation specification with frame metadata
     * @param outputDir directory to write the atlas into
     * @return path to the generated atlas file
     * @throws IOException if writing fails
     * @throws IllegalArgumentException if frames are empty or inconsistent sizes
     */
    public Path writeAtlas(List<BufferedImage> frames, AnimationSpec spec, Path outputDir)
            throws IOException {
        if (frames == null || frames.isEmpty()) {
            throw new IllegalArgumentException("frames must not be empty");
        }

        int frameWidth = frames.get(0).getWidth();
        int frameHeight = frames.get(0).getHeight();

        for (int i = 1; i < frames.size(); i++) {
            BufferedImage frame = frames.get(i);
            if (frame.getWidth() != frameWidth || frame.getHeight() != frameHeight) {
                throw new IllegalArgumentException(
                        "All frames must have identical dimensions. Frame 0: " + frameWidth + "x" + frameHeight
                        + ", Frame " + i + ": " + frame.getWidth() + "x" + frame.getHeight());
            }
        }

        int atlasWidth = frameWidth * frames.size();
        int atlasHeight = frameHeight;

        BufferedImage atlas = new BufferedImage(atlasWidth, atlasHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = atlas.createGraphics();

        for (int i = 0; i < frames.size(); i++) {
            g.drawImage(frames.get(i), i * frameWidth, 0, null);
        }
        g.dispose();

        Files.createDirectories(outputDir);
        String filename = spec.name() + "_atlas.png";
        Path atlasPath = outputDir.resolve(filename);

        ImageIO.write(atlas, "PNG", atlasPath.toFile());
        log.info("Wrote atlas: {} ({}x{}, {} frames)", atlasPath, atlasWidth, atlasHeight, frames.size());

        return atlasPath;
    }

    /**
     * Creates the horizontal-strip atlas BufferedImage without writing to disk.
     * Useful for testing.
     */
    public BufferedImage createAtlasImage(List<BufferedImage> frames) {
        if (frames == null || frames.isEmpty()) {
            throw new IllegalArgumentException("frames must not be empty");
        }
        int frameWidth = frames.get(0).getWidth();
        int frameHeight = frames.get(0).getHeight();
        int atlasWidth = frameWidth * frames.size();

        BufferedImage atlas = new BufferedImage(atlasWidth, frameHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = atlas.createGraphics();
        for (int i = 0; i < frames.size(); i++) {
            g.drawImage(frames.get(i), i * frameWidth, 0, null);
        }
        g.dispose();
        return atlas;
    }
}
