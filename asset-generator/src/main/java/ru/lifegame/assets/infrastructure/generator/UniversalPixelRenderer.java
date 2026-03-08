package ru.lifegame.assets.infrastructure.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lifegame.assets.domain.model.asset.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Universal pixel-art renderer. Knows nothing about specific game entities.
 * Reads pixel-data from AssetLayer and draws via PixelCanvas.
 */
public final class UniversalPixelRenderer {

    private static final Logger log = LoggerFactory.getLogger(UniversalPixelRenderer.class);

    public BufferedImage renderLayer(AssetLayer layer, int defaultWidth, int defaultHeight) {
        int w = layer.width() > 0 ? layer.width() : defaultWidth;
        int h = layer.height() > 0 ? layer.height() : defaultHeight;
        PixelCanvas canvas = new PixelCanvas(w, h);
        drawPixelData(canvas, layer.pixelData(), 0, 0);
        return canvas.toImage();
    }

    public BufferedImage renderComposite(List<AssetLayer> layers, int width, int height) {
        PixelCanvas canvas = new PixelCanvas(width, height);
        layers.stream()
                .sorted(Comparator.comparingInt(AssetLayer::zOrder))
                .forEach(layer -> drawPixelData(canvas, layer.pixelData(), 0, 0));
        return canvas.toImage();
    }

    public List<BufferedImage> renderAnimationFrames(
            List<AssetLayer> layers,
            AnimationSpec animSpec) {

        List<AssetLayer> sorted = layers.stream()
                .sorted(Comparator.comparingInt(AssetLayer::zOrder))
                .toList();

        List<BufferedImage> frames = new ArrayList<>();
        int fw = animSpec.frameWidth();
        int fh = animSpec.frameHeight();

        for (int i = 0; i < animSpec.frames(); i++) {
            PixelCanvas canvas = new PixelCanvas(fw, fh);
            FrameOffset offset = findFrameOffset(animSpec.frameOffsets(), i);

            // Build mutable offset map for follows resolution
            Map<String, int[]> resolvedOffsets = new HashMap<>();
            if (offset != null) {
                resolvedOffsets.putAll(offset.layerOffsets());
            }

            // Resolve follows: inherit parent layer offsets for child layers
            for (AssetLayer layer : sorted) {
                if (layer.follows() != null
                        && !resolvedOffsets.containsKey(layer.id())) {
                    int[] parentOffset = resolvedOffsets.get(layer.follows());
                    if (parentOffset != null) {
                        resolvedOffsets.put(layer.id(),
                                new int[]{parentOffset[0], parentOffset[1]});
                    }
                }
            }

            for (AssetLayer layer : sorted) {
                int[] layerOffset = resolvedOffsets.getOrDefault(
                        layer.id(), new int[]{0, 0});
                drawPixelData(canvas, layer.pixelData(), layerOffset[0], layerOffset[1]);
            }
            frames.add(canvas.toImage());
        }
        return frames;
    }

    private FrameOffset findFrameOffset(List<FrameOffset> offsets, int frameIndex) {
        if (offsets == null || offsets.isEmpty()) return null;
        return offsets.stream()
                .filter(fo -> fo.frameIndex() == frameIndex)
                .findFirst()
                .orElse(null);
    }

    private void drawPixelData(PixelCanvas canvas, PixelData data, int dx, int dy) {
        if (data == null || data.isEmpty()) return;

        for (PixelRect rect : data.rects()) {
            Color color = parseColor(rect.color());
            if (color != null) {
                canvas.fillRect(rect.x() + dx, rect.y() + dy, rect.w(), rect.h(), color);
            }
        }
        for (PixelLine line : data.lines()) {
            Color color = parseColor(line.color());
            if (color != null) {
                if (line.direction() == PixelLine.Direction.HORIZONTAL) {
                    canvas.hLine(line.x() + dx, line.y() + dy, line.length(), color);
                } else {
                    canvas.vLine(line.x() + dx, line.y() + dy, line.length(), color);
                }
            }
        }
        for (PixelDot dot : data.dots()) {
            Color color = parseColor(dot.color());
            if (color != null) {
                canvas.setPixel(dot.x() + dx, dot.y() + dy, color);
            }
        }
    }

    /**
     * Parses hex color string to Color. Returns null for unresolved $-variables
     * or invalid strings (defensive — should not happen after resolution).
     */
    private Color parseColor(String hex) {
        if (hex == null || hex.isBlank()) return null;
        // Skip unresolved $-variable references (defensive guard)
        if (hex.startsWith("$")) {
            log.warn("Unresolved color variable in pixel data: {}", hex);
            return null;
        }
        try {
            String cleaned = hex.startsWith("#") ? hex.substring(1) : hex;
            int r = Integer.parseInt(cleaned.substring(0, 2), 16);
            int g = Integer.parseInt(cleaned.substring(2, 4), 16);
            int b = Integer.parseInt(cleaned.substring(4, 6), 16);
            if (cleaned.length() == 8) {
                int a = Integer.parseInt(cleaned.substring(6, 8), 16);
                return new Color(r, g, b, a);
            }
            return new Color(r, g, b);
        } catch (Exception e) {
            log.warn("Invalid color value '{}': {}", hex, e.getMessage());
            return null;
        }
    }
}
