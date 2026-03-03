package ru.lifegame.assets.infrastructure.writer;

import org.springframework.stereotype.Component;
import ru.lifegame.assets.domain.model.asset.AssetLayer;
import ru.lifegame.assets.domain.model.asset.AssetSpec;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes each {@link AssetLayer} as a separate PNG file.
 *
 * <p>The current implementation generates a synthetic placeholder image
 * for each layer: a solid-colour rectangle whose colour is derived
 * deterministically from the layer name's hash code.  This is intentional
 * for the prototyping phase; real pixel data will be injected by the
 * AI-generation pipeline in a later sprint.</p>
 */
@Component
public class PngLayerWriter {

    /**
     * Write one PNG file per layer of {@code spec} into {@code outputDir}.
     *
     * @param spec      asset specification
     * @param outputDir target directory; created if absent
     * @throws LayerWriteException on any I/O error
     */
    public void write(AssetSpec spec, Path outputDir) {
        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            throw new LayerWriteException("Cannot create output directory: " + outputDir, e);
        }

        for (AssetLayer layer : spec.layers()) {
            Path file = outputDir.resolve(layerFileName(spec, layer));
            writeLayer(layer, spec.constraints().widthPx(),
                       spec.constraints().heightPx(), file);
        }
    }

    // -----------------------------------------------------------------------
    // Internals
    // -----------------------------------------------------------------------

    private String layerFileName(AssetSpec spec, AssetLayer layer) {
        if (spec.naming() != null && spec.naming().layerPattern() != null) {
            return String.format(spec.naming().layerPattern(),
                                 spec.naming().prefix(), layer.zIndex());
        }
        return spec.id() + "_layer_" + layer.name() + ".png";
    }

    private void writeLayer(AssetLayer layer, int width, int height, Path dest) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(placeholderColor(layer.name()));
        g.fillRect(0, 0, width, height);
        g.dispose();
        try {
            ImageIO.write(img, "PNG", dest.toFile());
        } catch (IOException e) {
            throw new LayerWriteException("Failed to write layer PNG: " + dest, e);
        }
    }

    private Color placeholderColor(String layerName) {
        int hash = layerName.hashCode();
        return new Color(
                (hash >> 16) & 0xFF,
                (hash >> 8)  & 0xFF,
                 hash        & 0xFF
        );
    }

    // -----------------------------------------------------------------------
    // Exception
    // -----------------------------------------------------------------------

    public static class LayerWriteException extends RuntimeException {
        public LayerWriteException(String msg, Throwable cause) { super(msg, cause); }
    }
}
