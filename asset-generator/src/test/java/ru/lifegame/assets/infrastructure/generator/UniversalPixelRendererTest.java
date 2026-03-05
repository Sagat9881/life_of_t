package ru.lifegame.assets.infrastructure.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.lifegame.assets.domain.model.asset.*;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UniversalPixelRenderer — pixel-data rendering")
class UniversalPixelRendererTest {

    private UniversalPixelRenderer renderer;

    @BeforeEach
    void setUp() {
        renderer = new UniversalPixelRenderer();
    }

    @Test
    @DisplayName("renderLayer draws rect at correct position")
    void renderLayerDrawsRect() {
        AssetLayer layer = new AssetLayer("test", "base", "test", 0, 16, 16,
                new PixelData(
                        List.of(new PixelRect(4, 4, 2, 2, "#FF0000")),
                        List.of(), List.of()));

        BufferedImage img = renderer.renderLayer(layer, 16, 16);

        int pixel = img.getRGB(5, 5);
        assertThat((pixel >> 16) & 0xFF).isEqualTo(0xFF);
        assertThat((pixel >> 8) & 0xFF).isEqualTo(0x00);
        assertThat(pixel & 0xFF).isEqualTo(0x00);
    }

    @Test
    @DisplayName("renderLayer draws individual dots")
    void renderLayerDrawsDot() {
        AssetLayer layer = new AssetLayer("test", "base", "test", 0, 8, 8,
                new PixelData(List.of(), List.of(),
                        List.of(new PixelDot(3, 3, "#00FF00"))));

        BufferedImage img = renderer.renderLayer(layer, 8, 8);
        int pixel = img.getRGB(3, 3);
        assertThat((pixel >> 8) & 0xFF).isEqualTo(0xFF);
    }

    @Test
    @DisplayName("renderLayer draws horizontal line")
    void renderLayerDrawsHLine() {
        AssetLayer layer = new AssetLayer("test", "base", "test", 0, 16, 16,
                new PixelData(List.of(),
                        List.of(new PixelLine(2, 5, 6, PixelLine.Direction.HORIZONTAL, "#0000FF")),
                        List.of()));

        BufferedImage img = renderer.renderLayer(layer, 16, 16);
        for (int x = 2; x < 8; x++) {
            int pixel = img.getRGB(x, 5);
            assertThat(pixel & 0xFF).isEqualTo(0xFF);
        }
    }

    @Test
    @DisplayName("renderComposite layers by z-order")
    void renderCompositeLayersInOrder() {
        AssetLayer back = new AssetLayer("back", "background", "", 0, 8, 8,
                new PixelData(List.of(new PixelRect(0, 0, 8, 8, "#FF0000")),
                        List.of(), List.of()));
        AssetLayer front = new AssetLayer("front", "foreground", "", 1, 8, 8,
                new PixelData(List.of(), List.of(),
                        List.of(new PixelDot(4, 4, "#00FF00"))));

        BufferedImage img = renderer.renderComposite(List.of(front, back), 8, 8);

        // (0,0) should be red (from back layer)
        int bgPixel = img.getRGB(0, 0);
        assertThat((bgPixel >> 16) & 0xFF).isEqualTo(0xFF);
        // (4,4) should be green (front layer overrides)
        int fgPixel = img.getRGB(4, 4);
        assertThat((fgPixel >> 8) & 0xFF).isEqualTo(0xFF);
    }

    @Test
    @DisplayName("renderAnimationFrames applies frame offsets")
    void renderAnimationFramesWithOffsets() {
        AssetLayer dot = new AssetLayer("dot", "base", "", 0, 8, 8,
                new PixelData(List.of(), List.of(),
                        List.of(new PixelDot(4, 4, "#FF0000"))));

        FrameOffset f0 = new FrameOffset(0, Map.of("dot", new int[]{0, 0}));
        FrameOffset f1 = new FrameOffset(1, Map.of("dot", new int[]{1, 0}));

        AnimationSpec anim = new AnimationSpec("test", 2, 8, true, 8, 8,
                List.of(f0, f1));

        List<BufferedImage> frames = renderer.renderAnimationFrames(List.of(dot), anim);
        assertThat(frames).hasSize(2);

        // Frame 0: dot at (4,4)
        int p0 = frames.get(0).getRGB(4, 4);
        assertThat((p0 >> 16) & 0xFF).isEqualTo(0xFF);

        // Frame 1: dot shifted to (5,4)
        int p1 = frames.get(1).getRGB(5, 4);
        assertThat((p1 >> 16) & 0xFF).isEqualTo(0xFF);
    }

    @Test
    @DisplayName("Empty pixel-data renders transparent canvas")
    void emptyPixelDataRendersTransparent() {
        AssetLayer empty = new AssetLayer("empty", "base", "", 0, 8, 8, PixelData.EMPTY);
        BufferedImage img = renderer.renderLayer(empty, 8, 8);

        // Every pixel should be fully transparent
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int alpha = (img.getRGB(x, y) >> 24) & 0xFF;
                assertThat(alpha).isZero();
            }
        }
    }
}
