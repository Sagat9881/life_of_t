package ru.lifegame.assets.infrastructure.writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Creates sprite atlas images from animation frames.
 * <p>
 * All animations are packed as <b>grid atlases</b>: multiple rows, one per variant.
 * Columns = frames per row, Rows = variants. All rows must have the same frame count and dimensions.
 */
public class WebpAtlasWriter {

    private static final Logger log = LoggerFactory.getLogger(WebpAtlasWriter.class);

    /**
     * Creates a grid atlas where each row is a variant of the same animation.
     * <p>
     * The map key is the variant condition value (e.g. "morning", "day", "default").
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
