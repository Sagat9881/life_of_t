package ru.lifegame.assets.infrastructure.generator.renderers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.lifegame.assets.domain.service.PixelArtRenderer;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for all PixelArtRenderer implementations.
 * Validates frame count, dimensions, ARGB format, and that frames contain non-transparent pixels.
 */
@DisplayName("PixelArt Renderers — frame generation tests")
class PixelArtRenderersTest {

    static Stream<Arguments> rendererProvider() {
        return Stream.of(
                Arguments.of(new TanyaIdleRenderer(),  "tanya_idle",   32, 48, 6),
                Arguments.of(new TanyaWalkRenderer(),  "tanya_walk",   32, 48, 8),
                Arguments.of(new SamIdleRenderer(),    "sam_idle",     24, 20, 4),
                Arguments.of(new SamWalkRenderer(),    "sam_walk",     24, 20, 6),
                Arguments.of(new BedStaticRenderer(),  "bed_static",   48, 32, 1),
                Arguments.of(new HomeRoomBgRenderer(), "home_room_bg", 320, 192, 1)
        );
    }

    @ParameterizedTest(name = "{1}: {4} frames at {2}×{3} px")
    @MethodSource("rendererProvider")
    @DisplayName("Renderer produces correct frame count")
    void rendererProducesCorrectFrameCount(PixelArtRenderer renderer,
                                           String expectedId,
                                           int fw, int fh, int fc) {
        assertThat(renderer.spriteId()).isEqualTo(expectedId);
        List<BufferedImage> frames = renderer.renderFrames(fw, fh, fc);
        assertThat(frames).hasSize(fc);
    }

    @ParameterizedTest(name = "{1}: frame dimensions {2}×{3}")
    @MethodSource("rendererProvider")
    @DisplayName("All frames have correct dimensions")
    void allFramesHaveCorrectDimensions(PixelArtRenderer renderer,
                                        String id, int fw, int fh, int fc) {
        List<BufferedImage> frames = renderer.renderFrames(fw, fh, fc);
        for (BufferedImage frame : frames) {
            assertThat(frame.getWidth()).isEqualTo(fw);
            assertThat(frame.getHeight()).isEqualTo(fh);
        }
    }

    @ParameterizedTest(name = "{1}: ARGB 32-bit format")
    @MethodSource("rendererProvider")
    @DisplayName("All frames are ARGB 32-bit")
    void allFramesAreArgb32bit(PixelArtRenderer renderer,
                               String id, int fw, int fh, int fc) {
        List<BufferedImage> frames = renderer.renderFrames(fw, fh, fc);
        for (BufferedImage frame : frames) {
            assertThat(frame.getType()).isEqualTo(BufferedImage.TYPE_INT_ARGB);
            assertThat(frame.getColorModel().hasAlpha()).isTrue();
            assertThat(frame.getColorModel().getPixelSize()).isEqualTo(32);
        }
    }

    @ParameterizedTest(name = "{1}: frames contain non-transparent content")
    @MethodSource("rendererProvider")
    @DisplayName("Frames are not fully transparent (content is drawn)")
    void framesContainVisiblePixels(PixelArtRenderer renderer,
                                    String id, int fw, int fh, int fc) {
        List<BufferedImage> frames = renderer.renderFrames(fw, fh, fc);
        for (BufferedImage frame : frames) {
            boolean hasVisiblePixel = false;
            outer:
            for (int y = 0; y < frame.getHeight(); y++) {
                for (int x = 0; x < frame.getWidth(); x++) {
                    int alpha = (frame.getRGB(x, y) >> 24) & 0xFF;
                    if (alpha > 0) {
                        hasVisiblePixel = true;
                        break outer;
                    }
                }
            }
            assertThat(hasVisiblePixel)
                    .as("Frame of %s should contain at least one visible pixel", id)
                    .isTrue();
        }
    }

    @Test
    @DisplayName("Tanya idle: breathing offset creates different frames")
    void tanyaIdleFramesDiffer() {
        TanyaIdleRenderer renderer = new TanyaIdleRenderer();
        List<BufferedImage> frames = renderer.renderFrames(32, 48, 6);

        // At least two frames should differ in pixel content
        boolean foundDifference = false;
        BufferedImage first = frames.get(0);
        for (int i = 1; i < frames.size(); i++) {
            if (!imagesIdentical(first, frames.get(i))) {
                foundDifference = true;
                break;
            }
        }
        assertThat(foundDifference).as("Idle animation should have frame differences").isTrue();
    }

    @Test
    @DisplayName("Tanya walk: all 8 frames differ (walk cycle)")
    void tanyaWalkFramesAllDiffer() {
        TanyaWalkRenderer renderer = new TanyaWalkRenderer();
        List<BufferedImage> frames = renderer.renderFrames(32, 48, 8);

        // Consecutive frames should differ
        for (int i = 0; i < frames.size() - 1; i++) {
            assertThat(imagesIdentical(frames.get(i), frames.get(i + 1)))
                    .as("Walk frame %d and %d should differ", i, i + 1)
                    .isFalse();
        }
    }

    @Test
    @DisplayName("Sam idle: tail wag creates different frames")
    void samIdleTailWag() {
        SamIdleRenderer renderer = new SamIdleRenderer();
        List<BufferedImage> frames = renderer.renderFrames(24, 20, 4);

        boolean foundDifference = false;
        for (int i = 1; i < frames.size(); i++) {
            if (!imagesIdentical(frames.get(0), frames.get(i))) {
                foundDifference = true;
                break;
            }
        }
        assertThat(foundDifference).as("Sam idle should have tail wag variation").isTrue();
    }

    @Test
    @DisplayName("Home room bg: fills substantial area (not mostly empty)")
    void homeRoomBgHasSubstantialContent() {
        HomeRoomBgRenderer renderer = new HomeRoomBgRenderer();
        List<BufferedImage> frames = renderer.renderFrames(320, 192, 1);
        BufferedImage bg = frames.get(0);

        int visiblePixels = 0;
        int totalPixels = bg.getWidth() * bg.getHeight();
        for (int y = 0; y < bg.getHeight(); y++) {
            for (int x = 0; x < bg.getWidth(); x++) {
                int alpha = (bg.getRGB(x, y) >> 24) & 0xFF;
                if (alpha > 0) {
                    visiblePixels++;
                }
            }
        }
        double fillRatio = (double) visiblePixels / totalPixels;
        assertThat(fillRatio).as("Room background should fill at least 70%% of canvas").isGreaterThan(0.70);
    }

    @Test
    @DisplayName("Bed static: single frame, mostly opaque")
    void bedStaticSingleFrame() {
        BedStaticRenderer renderer = new BedStaticRenderer();
        List<BufferedImage> frames = renderer.renderFrames(48, 32, 1);
        assertThat(frames).hasSize(1);

        BufferedImage bed = frames.get(0);
        int visiblePixels = 0;
        for (int y = 0; y < bed.getHeight(); y++) {
            for (int x = 0; x < bed.getWidth(); x++) {
                if (((bed.getRGB(x, y) >> 24) & 0xFF) > 0) {
                    visiblePixels++;
                }
            }
        }
        assertThat(visiblePixels).as("Bed should have significant visible content").isGreaterThan(100);
    }

    private boolean imagesIdentical(BufferedImage a, BufferedImage b) {
        if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) {
            return false;
        }
        for (int y = 0; y < a.getHeight(); y++) {
            for (int x = 0; x < a.getWidth(); x++) {
                if (a.getRGB(x, y) != b.getRGB(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }
}
