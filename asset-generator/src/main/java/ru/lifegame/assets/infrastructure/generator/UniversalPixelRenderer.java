package ru.lifegame.assets.infrastructure.generator;

import ru.lifegame.assets.domain.model.asset.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Universal pixel-art renderer. Knows nothing about specific game entities.
 * Reads pixel-data from AssetLayer and draws via PixelCanvas.
 */
public final class UniversalPixelRenderer {

    /**
     * Renders a single static layer from its pixel-data.
     */
    public BufferedImage renderLayer(AssetLayer layer, int defaultWidth, int defaultHeight) {
        int w = layer.width() > 0 ? layer.width() : defaultWidth;
        int h = layer.height() > 0 ? layer.height() : defaultHeight;
        PixelCanvas canvas = new PixelCanvas(w, h);
        drawPixelData(canvas, layer.pixelData(), 0, 0);
        return canvas.toImage();
    }

    /**
     * Renders all layers composited into a single image, sorted by z-order.
     */
    public BufferedImage renderComposite(List<AssetLayer> layers, int width, int height) {
        PixelCanvas canvas = new PixelCanvas(width, height);
        layers.stream()
                .sorted(Comparator.comparingInt(AssetLayer::zOrder))
                .forEach(layer -> drawPixelData(canvas, layer.pixelData(), 0, 0));
        return canvas.toImage();
    }

    /**
     * Renders animation frames by compositing all layers with per-frame offsets.
     */
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

            for (AssetLayer layer : sorted) {
                int[] layerOffset = offset != null
                        ? offset.getOffset(layer.id())
                        : new int[]{0, 0};
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
            canvas.fillRect(rect.x() + dx, rect.y() + dy,
                    rect.w(), rect.h(), parseColor(rect.color()));
        }
        for (PixelLine line : data.lines()) {
            if (line.direction() == PixelLine.Direction.HORIZONTAL) {
                canvas.hLine(line.x() + dx, line.y() + dy, line.length(), parseColor(line.color()));
            } else {
                canvas.vLine(line.x() + dx, line.y() + dy, line.length(), parseColor(line.color()));
            }
        }
        for (PixelDot dot : data.dots()) {
            canvas.setPixel(dot.x() + dx, dot.y() + dy, parseColor(dot.color()));
        }
    }

    private Color parseColor(String hex) {
        String cleaned = hex.startsWith("#") ? hex.substring(1) : hex;
        int r = Integer.parseInt(cleaned.substring(0, 2), 16);
        int g = Integer.parseInt(cleaned.substring(2, 4), 16);
        int b = Integer.parseInt(cleaned.substring(4, 6), 16);
        if (cleaned.length() == 8) {
            int a = Integer.parseInt(cleaned.substring(6, 8), 16);
            return new Color(r, g, b, a);
        }
        return new Color(r, g, b);
    }
}
