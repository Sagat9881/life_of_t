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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates sprite atlas images from animation frames.
 * <p>
 * Supports two layouts:
 * <ul>
 *   <li><b>Horizontal strip</b> — single row of frames (for animations without variants)</li>
 *   <li><b>Grid</b> — multiple rows, one per variant (e.g. time-of-day idle variants).
 *       Columns = frames, Rows = variants. All rows must have the same frame count and dimensions.</li>
 * </ul>
 */
public class WebpAtlasWriter {

    private static final Logger log = LoggerFactory.getLogger(WebpAtlasWriter.class);

    /**
     * Creates a horizontal-strip atlas (single row) from the given frames.
     */
    public Path writeAtlas(List<BufferedImage> frames, AnimationSpec spec, Path outputDir)
            throws IOException {
        if (frames == null || frames.isEmpty()) {
            throw new IllegalArgumentException("frames must not be empty");
        }

        int frameWidth = frames.get(0).getWidth();
        int frameHeight = frames.get(0).getHeight();
        validateFrameDimensions(frames, frameWidth, frameHeight);

        int atlasWidth = frameWidth * frames.size();
        BufferedImage atlas = new BufferedImage(atlasWidth, frameHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = atlas.createGraphics();
        for (int i = 0; i < frames.size(); i++) {
            g.drawImage(frames.get(i), i * frameWidth, 0, null);
        }
        g.dispose();

        Files.createDirectories(outputDir);
        String filename = spec.name() + "_atlas.png";
        Path atlasPath = outputDir.resolve(filename);
        ImageIO.write(atlas, "PNG", atlasPath.toFile());
        log.info("Wrote strip atlas: {} ({}x{}, {} frames)", atlasPath, atlasWidth, frameHeight, frames.size());
        return atlasPath;
    }

    /**
     * Creates a grid atlas where each row is a variant of the same animation.
     * <p>
     * The map key is the variant condition value (e.g. "morning", "day").
     * Iteration order of the map determines row order — use LinkedHashMap.
     *
     * @param rowsByCondition ordered map: conditionValue → list of frame images
     * @param baseName        base animation name (e.g. "idle")
     * @param outputDir       directory to write the atlas into
     * @return path to the generated grid atlas file
     */
    public Path writeGridAtlas(LinkedHashMap<String, List<BufferedImage>> rowsByCondition,
                               String baseName, Path outputDir) throws IOException {
        if (rowsByCondition == null || rowsByCondition.isEmpty()) {
            throw new IllegalArgumentException("rowsByCondition must not be empty");
        }

        var firstEntry = rowsByCondition.values().iterator().next();
        int frameWidth = firstEntry.get(0).getWidth();
        int frameHeight = firstEntry.get(0).getHeight();
        int columns = firstEntry.size();
        int rows = rowsByCondition.size();

        // Validate: all rows must have same frame count and dimensions
        for (var entry : rowsByCondition.entrySet()) {
            List<BufferedImage> frames = entry.getValue();
            if (frames.size() != columns) {
                throw new IllegalArgumentException(
                        "Row '" + entry.getKey() + "' has " + frames.size() +
                        " frames, expected " + columns);
            }
            validateFrameDimensions(frames, frameWidth, frameHeight);
        }

        int atlasWidth = frameWidth * columns;
        int atlasHeight = frameHeight * rows;
        BufferedImage atlas = new BufferedImage(atlasWidth, atlasHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = atlas.createGraphics();

        int rowIndex = 0;
        for (List<BufferedImage> frames : rowsByCondition.values()) {
            for (int col = 0; col < frames.size(); col++) {
                g.drawImage(frames.get(col), col * frameWidth, rowIndex * frameHeight, null);
            }
            rowIndex++;
        }
        g.dispose();

        Files.createDirectories(outputDir);
        String filename = baseName + "_atlas.png";
        Path atlasPath = outputDir.resolve(filename);
        ImageIO.write(atlas, "PNG", atlasPath.toFile());
        log.info("Wrote grid atlas: {} ({}x{}, {}cols x {}rows)",
                atlasPath, atlasWidth, atlasHeight, columns, rows);
        return atlasPath;
    }

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

    private void validateFrameDimensions(List<BufferedImage> frames, int expectedW, int expectedH) {
        for (int i = 0; i < frames.size(); i++) {
            BufferedImage f = frames.get(i);
            if (f.getWidth() != expectedW || f.getHeight() != expectedH) {
                throw new IllegalArgumentException(
                        "Frame " + i + " dimensions " + f.getWidth() + "x" + f.getHeight() +
                        " don't match expected " + expectedW + "x" + expectedH);
            }
        }
    }
}
