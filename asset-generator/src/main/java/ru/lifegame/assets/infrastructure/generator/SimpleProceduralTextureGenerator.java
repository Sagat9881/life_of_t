package ru.lifegame.assets.infrastructure.generator;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Generates simple procedural placeholder textures for use during
 * early prototyping.
 *
 * <p>Each texture is a solid-colour 64×64 PNG.  The colour is derived
 * deterministically from the supplied {@code seed} string via its hash
 * code, ensuring reproducible output.</p>
 *
 * @deprecated Use {@link LayeredAssetGenerator} for all new asset work.
 *             This class will be removed in a future release.
 */
@Deprecated(since = "1.0.0", forRemoval = true)
@Component
public class SimpleProceduralTextureGenerator {

    private static final int DEFAULT_SIZE = 64;

    /**
     * Generate a procedural texture PNG and write it to {@code outputPath}.
     *
     * @param seed       a string used to deterministically pick the fill colour
     * @param outputPath destination file path (extension should be {@code .png})
     * @throws TextureGenerationException if the file cannot be written
     */
    public void generate(String seed, Path outputPath) {
        BufferedImage image = createImage(seed);
        writeImage(image, outputPath);
    }

    // -----------------------------------------------------------------------
    // Internals
    // -----------------------------------------------------------------------

    private BufferedImage createImage(String seed) {
        BufferedImage image = new BufferedImage(
                DEFAULT_SIZE, DEFAULT_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(colorFromSeed(seed));
        g.fillRect(0, 0, DEFAULT_SIZE, DEFAULT_SIZE);
        g.dispose();
        return image;
    }

    private Color colorFromSeed(String seed) {
        int hash = seed.hashCode();
        return new Color(
                (hash >> 16) & 0xFF,
                (hash >> 8)  & 0xFF,
                 hash        & 0xFF
        );
    }

    private void writeImage(BufferedImage image, Path outputPath) {
        try {
            ImageIO.write(image, "PNG", outputPath.toFile());
        } catch (IOException e) {
            throw new TextureGenerationException(
                    "Failed to write texture to " + outputPath, e);
        }
    }

    // -----------------------------------------------------------------------
    // Exception
    // -----------------------------------------------------------------------

    public static class TextureGenerationException extends RuntimeException {
        public TextureGenerationException(String msg, Throwable cause) { super(msg, cause); }
    }
}
