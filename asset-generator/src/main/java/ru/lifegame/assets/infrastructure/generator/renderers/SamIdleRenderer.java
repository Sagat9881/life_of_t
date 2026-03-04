package ru.lifegame.assets.infrastructure.generator.renderers;

import ru.lifegame.assets.domain.service.PixelArtRenderer;
import ru.lifegame.assets.infrastructure.generator.PixelCanvas;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders Sam the Zwergpinscher (miniature pinscher) idle animation (24×20 px, 4 frames).
 *
 * <p>Side-view small dog. Body: tan/golden, elongated. Legs: darker tan.
 * Pointed ears, thin tail. Tail wags through frames.</p>
 */
public final class SamIdleRenderer implements PixelArtRenderer {

    private static final Color BODY       = new Color(0xC8, 0x80, 0x30); // tan/chocolate
    private static final Color BODY_DARK  = new Color(0xA0, 0x60, 0x18); // darker brown
    private static final Color BODY_LIGHT = new Color(0xE0, 0xA0, 0x48);
    private static final Color BELLY      = new Color(0xD8, 0xA0, 0x60); // lighter belly
    private static final Color EYE        = new Color(0x20, 0x20, 0x20);
    private static final Color NOSE       = new Color(0x20, 0x20, 0x20);
    private static final Color LEG        = new Color(0xA0, 0x60, 0x18);
    private static final Color OUTLINE    = new Color(0x50, 0x30, 0x10);

    @Override
    public String spriteId() {
        return "sam_idle";
    }

    @Override
    public List<BufferedImage> renderFrames(int fw, int fh, int frameCount) {
        List<BufferedImage> frames = new ArrayList<>();
        for (int i = 0; i < frameCount; i++) {
            // Tail wag: up, neutral, down, neutral
            int tailOffset = switch (i % 4) {
                case 0 -> -2;
                case 1 -> 0;
                case 2 -> 2;
                default -> 0;
            };
            frames.add(renderFrame(fw, fh, tailOffset));
        }
        return frames;
    }

    private BufferedImage renderFrame(int fw, int fh, int tailY) {
        PixelCanvas c = new PixelCanvas(fw, fh);

        // Dog faces right. Body centred vertically.
        int bodyY = fh - 11; // body top
        int bodyX = 3;       // body left

        // Body (elongated oval shape)
        c.fillRect(bodyX, bodyY, 14, 6, BODY);
        c.fillRect(bodyX + 1, bodyY - 1, 12, 1, BODY); // top curve
        c.fillRect(bodyX + 1, bodyY + 6, 12, 1, BODY);  // bottom curve
        // Belly highlight
        c.fillRect(bodyX + 2, bodyY + 4, 10, 2, BELLY);

        // Head (triangular muzzle shape for Zwergpinscher)
        int headX = bodyX + 12;
        int headY = bodyY - 3;
        c.fillRect(headX, headY, 6, 5, BODY);
        c.fillRect(headX + 1, headY - 1, 4, 1, BODY); // forehead
        // Muzzle (pointed)
        c.fillRect(headX + 5, headY + 2, 2, 2, BODY_LIGHT);
        c.setPixel(headX + 7, headY + 2, BODY_LIGHT); // nose tip

        // Nose
        c.setPixel(headX + 7, headY + 2, NOSE);
        c.setPixel(headX + 6, headY + 2, NOSE);

        // Eye
        c.setPixel(headX + 3, headY + 1, EYE);
        c.setPixel(headX + 4, headY + 1, EYE);

        // Ears (pointed, characteristic of Zwergpinscher)
        c.fillRect(headX + 1, headY - 3, 2, 3, BODY_DARK);
        c.setPixel(headX + 1, headY - 4, BODY_DARK);
        c.fillRect(headX + 3, headY - 3, 2, 3, BODY_DARK);
        c.setPixel(headX + 4, headY - 4, BODY_DARK);

        // Legs (4 visible in side-view as 2 pairs)
        int legY = bodyY + 7;
        // Front legs
        c.fillRect(headX - 2, legY, 2, 4, LEG);
        c.fillRect(headX, legY, 2, 4, LEG);
        // Back legs
        c.fillRect(bodyX + 1, legY, 2, 4, LEG);
        c.fillRect(bodyX + 3, legY, 2, 4, LEG);

        // Tail (thin, curves up — characteristic of min pin)
        int tailBaseX = bodyX;
        int tailBaseY = bodyY - 1;
        c.setPixel(tailBaseX - 1, tailBaseY + tailY, BODY_DARK);
        c.setPixel(tailBaseX - 2, tailBaseY - 1 + tailY, BODY_DARK);
        c.setPixel(tailBaseX - 2, tailBaseY - 2 + tailY, BODY_DARK);
        c.setPixel(tailBaseX - 3, tailBaseY - 3 + tailY, BODY_DARK);

        return c.toImage();
    }
}
