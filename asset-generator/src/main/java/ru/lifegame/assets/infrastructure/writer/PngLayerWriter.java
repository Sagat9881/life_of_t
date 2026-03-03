package ru.lifegame.assets.infrastructure.writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lifegame.assets.domain.model.asset.AssetLayer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes individual asset layers as 32-bit RGBA PNG files.
 */
public class PngLayerWriter {

    private static final Logger log = LoggerFactory.getLogger(PngLayerWriter.class);

    /**
     * Writes the given image as a 32-bit RGBA PNG file.
     *
     * @param image    the image to write
     * @param layer    the layer metadata (used for naming)
     * @param outputDir target directory
     * @return path to the written file
     * @throws IOException if writing fails
     */
    public Path write(BufferedImage image, AssetLayer layer, Path outputDir) throws IOException {
        Files.createDirectories(outputDir);
        String filename = layer.id() + ".png";
        Path outputPath = outputDir.resolve(filename);

        if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
            BufferedImage argbImage = new BufferedImage(
                    image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            argbImage.getGraphics().drawImage(image, 0, 0, null);
            image = argbImage;
        }

        ImageIO.write(image, "PNG", outputPath.toFile());
        log.info("Wrote PNG layer: {}", outputPath);
        return outputPath;
    }

    /**
     * Writes a BufferedImage to a specific file path.
     *
     * @param image      the image to write
     * @param outputPath target file path
     * @return the output path
     * @throws IOException if writing fails
     */
    public Path writeToPath(BufferedImage image, Path outputPath) throws IOException {
        Files.createDirectories(outputPath.getParent());
        ImageIO.write(image, "PNG", outputPath.toFile());
        log.info("Wrote PNG: {}", outputPath);
        return outputPath;
    }
}
