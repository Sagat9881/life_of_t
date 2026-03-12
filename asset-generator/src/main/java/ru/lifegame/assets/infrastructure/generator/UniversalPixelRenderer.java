package ru.lifegame.assets.infrastructure.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lifegame.assets.domain.model.asset.*;

import java.awt.Color;
import java.awt.Graphics2D;
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

            Map<String, int[]> resolvedOffsets = new HashMap<>();
            if (offset != null) {
                resolvedOffsets.putAll(offset.layerOffsets());
            }

            boolean changed = true;
            while (changed) {
                changed = false;
                for (AssetLayer layer : sorted) {
                    if (layer.follows() != null && !resolvedOffsets.containsKey(layer.id())) {
                        int[] parentOffset = resolvedOffsets.get(layer.follows());
                        if (parentOffset != null) {
                            resolvedOffsets.put(layer.id(),
                                    new int[]{parentOffset[0], parentOffset[1]});
                            changed = true;
                        }
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

    public int[] computeCropBounds(List<BufferedImage> frames) {
        if (frames == null || frames.isEmpty()) {
            return new int[]{0, 0, 0, 0};
        }

        int imgW = frames.get(0).getWidth();
        int imgH = frames.get(0).getHeight();
        int minX = imgW, minY = imgH, maxX = 0, maxY = 0;

        for (BufferedImage frame : frames) {
            for (int y = 0; y < imgH; y++) {
                for (int x = 0; x < imgW; x++) {
                    int alpha = (frame.getRGB(x, y) >> 24) & 0xFF;
                    if (alpha > 0) {
                        if (x < minX) minX = x;
                        if (y < minY) minY = y;
                        if (x > maxX) maxX = x;
                        if (y > maxY) maxY = y;
                    }
                }
            }
        }

        if (maxX < minX || maxY < minY) {
            return new int[]{0, 0, imgW, imgH};
        }

        int cropW = maxX - minX + 1;
        int cropH = maxY - minY + 1;
        return new int[]{minX, minY, cropW, cropH};
    }

    public List<BufferedImage> cropFrames(List<BufferedImage> frames, int[] bounds) {
        int cx = bounds[0], cy = bounds[1], cw = bounds[2], ch = bounds[3];

        if (!frames.isEmpty()
                && cx == 0 && cy == 0
                && cw == frames.get(0).getWidth()
                && ch == frames.get(0).getHeight()) {
            return frames;
        }

        List<BufferedImage> cropped = new ArrayList<>(frames.size());
        for (BufferedImage frame : frames) {
            BufferedImage sub = new BufferedImage(cw, ch, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = sub.createGraphics();
            g.drawImage(frame, 0, 0, cw, ch, cx, cy, cx + cw, cy + ch, null);
            g.dispose();
            cropped.add(sub);
        }
        return cropped;
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

    private Color parseColor(String hex) {
        if (hex == null || hex.isBlank()) return null;
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
