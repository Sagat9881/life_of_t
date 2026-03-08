package com.sagat.life_of_t.domain.engine.spec;

import java.util.List;
import java.util.Map;

/**
 * Immutable visual specification loaded from XML.
 * Supports abstract specs (templates) and concrete specs that extend them.
 * After resolution, all fields are fully populated — no unresolved references remain.
 */
public record VisualSpec(
        String entityName,
        String entityType,
        String specVersion,
        boolean isAbstract,
        String extendsRef,
        CanvasSpec canvas,
        double displayScale,
        Map<String, String> colorPalette,
        List<LayerSpec> layers,
        List<AnimationSpec> animations
) {
    public record CanvasSpec(int width, int height) {}

    public record LayerSpec(
            String id,
            String type,
            int zOrder,
            int width,
            int height,
            boolean replace,
            List<PixelPrimitive> pixelData
    ) {}

    public sealed interface PixelPrimitive permits PixelRect, PixelLine, PixelDot {
        String resolvedColor(Map<String, String> palette);
    }

    public record PixelRect(int x, int y, int w, int h, String color) implements PixelPrimitive {
        @Override
        public String resolvedColor(Map<String, String> palette) {
            return resolveColor(color, palette);
        }
    }

    public record PixelLine(int x, int y, int len, String color) implements PixelPrimitive {
        @Override
        public String resolvedColor(Map<String, String> palette) {
            return resolveColor(color, palette);
        }
    }

    public record PixelDot(int x, int y, String color) implements PixelPrimitive {
        @Override
        public String resolvedColor(Map<String, String> palette) {
            return resolveColor(color, palette);
        }
    }

    public record AnimationSpec(
            String name,
            int frames,
            int fps,
            boolean loop,
            int frameWidth,
            int frameHeight,
            List<FrameSpec> frameSpecs
    ) {}

    public record FrameSpec(
            int index,
            Map<String, LayerOffset> layerOffsets
    ) {}

    public record LayerOffset(int dx, int dy) {}

    public LayerSpec layerById(String id) {
        return layers.stream()
                .filter(l -> l.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    public AnimationSpec animationByName(String name) {
        return animations.stream()
                .filter(a -> a.name().equals(name))
                .findFirst()
                .orElse(null);
    }

    private static String resolveColor(String raw, Map<String, String> palette) {
        if (raw != null && raw.startsWith("$")) {
            String key = raw.substring(1);
            return palette.getOrDefault(key, raw);
        }
        return raw;
    }
}
