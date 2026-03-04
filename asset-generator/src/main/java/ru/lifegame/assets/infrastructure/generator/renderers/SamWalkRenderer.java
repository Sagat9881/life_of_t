package ru.lifegame.assets.infrastructure.generator.renderers;

import ru.lifegame.assets.domain.service.PixelArtRenderer;
import ru.lifegame.assets.infrastructure.generator.PixelCanvas;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders Sam the Zwergpinscher walk animation (24×20 px, 6 frames).
 * Side-view with alternating leg pairs and bobbing head.
 */
public final class SamWalkRenderer implements PixelArtRenderer {

    private static final Color BODY       = new Color(0xC8, 0x80, 0x30);
    private static final Color BODY_DARK  = new Color(0xA0, 0x60, 0x18);
    private static final Color BODY_LIGHT = new Color(0xE0, 0xA0, 0x48);
    private static final Color BELLY      = new Color(0xD8, 0xA0, 0x60);
    private static final Color EYE        = new Color(0x20, 0x20, 0x20);
    private static final Color NOSE       = new Color(0x20, 0x20, 0x20);
    private static final Color LEG        = new Color(0xA0, 0x60, 0x18);

    @Override
    public String spriteId() {
        return "sam_walk";
    }

    @Override
    public List<BufferedImage> renderFrames(int fw, int fh, int frameCount) {
        List<BufferedImage> frames = new ArrayList<>();
        for (int i = 0; i < frameCount; i++) {
            int phase = i % 6;
            int headBob = (phase == 1 || phase == 4) ? -1 : 0;
            int tailWag = (phase % 2 == 0) ? -1 : 1;
            int frontLegPhase = phase;
            int backLegPhase = (phase + 3) % 6;
            frames.add(renderFrame(fw, fh, headBob, tailWag, frontLegPhase, backLegPhase));
        }
        return frames;
    }

    private int legYOffset(int phase) {
        // 6-frame cycle: 0=down, 1=mid, 2=up, 3=down, 4=mid, 5=up
        return switch (phase % 3) {
            case 0 -> 1;
            case 1 -> 0;
            case 2 -> -1;
            default -> 0;
        };
    }

    private BufferedImage renderFrame(int fw, int fh, int headBob, int tailWag,
                                      int frontPhase, int backPhase) {
        PixelCanvas c = new PixelCanvas(fw, fh);

        int bodyY = fh - 11;
        int bodyX = 3;

        // Body
        c.fillRect(bodyX, bodyY, 14, 6, BODY);
        c.fillRect(bodyX + 1, bodyY - 1, 12, 1, BODY);
        c.fillRect(bodyX + 1, bodyY + 6, 12, 1, BODY);
        c.fillRect(bodyX + 2, bodyY + 4, 10, 2, BELLY);

        // Head
        int headX = bodyX + 12;
        int headY = bodyY - 3 + headBob;
        c.fillRect(headX, headY, 6, 5, BODY);
        c.fillRect(headX + 1, headY - 1, 4, 1, BODY);
        c.fillRect(headX + 5, headY + 2, 2, 2, BODY_LIGHT);
        c.setPixel(headX + 7, headY + 2, NOSE);
        c.setPixel(headX + 6, headY + 2, NOSE);
        c.setPixel(headX + 3, headY + 1, EYE);
        c.setPixel(headX + 4, headY + 1, EYE);

        // Ears
        c.fillRect(headX + 1, headY - 3, 2, 3, BODY_DARK);
        c.setPixel(headX + 1, headY - 4, BODY_DARK);
        c.fillRect(headX + 3, headY - 3, 2, 3, BODY_DARK);
        c.setPixel(headX + 4, headY - 4, BODY_DARK);

        // Front legs (walking)
        int legY = bodyY + 7;
        int frontOff = legYOffset(frontPhase);
        c.fillRect(headX - 2, legY + frontOff, 2, 4, LEG);
        c.fillRect(headX, legY - frontOff, 2, 4, LEG);

        // Back legs (walking)
        int backOff = legYOffset(backPhase);
        c.fillRect(bodyX + 1, legY + backOff, 2, 4, LEG);
        c.fillRect(bodyX + 3, legY - backOff, 2, 4, LEG);

        // Tail
        int tailBaseY = bodyY - 1;
        c.setPixel(bodyX - 1, tailBaseY + tailWag, BODY_DARK);
        c.setPixel(bodyX - 2, tailBaseY - 1 + tailWag, BODY_DARK);
        c.setPixel(bodyX - 2, tailBaseY - 2 + tailWag, BODY_DARK);
        c.setPixel(bodyX - 3, tailBaseY - 3 + tailWag, BODY_DARK);

        return c.toImage();
    }
}
