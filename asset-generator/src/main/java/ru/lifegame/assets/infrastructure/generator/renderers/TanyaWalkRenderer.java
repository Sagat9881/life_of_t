package ru.lifegame.assets.infrastructure.generator.renderers;

import ru.lifegame.assets.domain.service.PixelArtRenderer;
import ru.lifegame.assets.infrastructure.generator.PixelCanvas;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders Tanya walk animation (32×48 px, 8 frames).
 *
 * <p>Same visual spec as idle — dark-red curly hair, green eyes, beige cardigan,
 * mint top, white pants, gray slippers — but with alternating leg positions
 * and arm swing. Uses staircase pixel diagonals only.</p>
 */
public final class TanyaWalkRenderer implements PixelArtRenderer {

    private static final Color SKIN           = new Color(0xF0, 0xC0, 0x98);
    private static final Color SKIN_SHADOW    = new Color(0xD8, 0xA0, 0x78);
    private static final Color HAIR           = new Color(0x80, 0x18, 0x28);
    private static final Color HAIR_HIGHLIGHT = new Color(0xA0, 0x28, 0x38);
    private static final Color EYE_GREEN      = new Color(0x30, 0xA0, 0x40);
    private static final Color EYE_BLACK      = new Color(0x20, 0x20, 0x20);
    private static final Color MOUTH          = new Color(0xD0, 0x70, 0x70);
    private static final Color CARDIGAN       = new Color(0xD8, 0xC8, 0xA0);
    private static final Color CARDIGAN_SHADE = new Color(0xC0, 0xB0, 0x88);
    private static final Color MINT_TOP       = new Color(0x90, 0xD8, 0xC0);
    private static final Color MINT_SHADE     = new Color(0x70, 0xC0, 0xA8);
    private static final Color PANTS          = new Color(0xF0, 0xF0, 0xE8);
    private static final Color PANTS_SHADE    = new Color(0xD0, 0xD0, 0xC8);
    private static final Color NECKLACE_GOLD  = new Color(0xE0, 0xC0, 0x40);
    private static final Color SLIPPER        = new Color(0x80, 0x80, 0x80);

    @Override
    public String spriteId() {
        return "tanya_walk";
    }

    @Override
    public List<BufferedImage> renderFrames(int fw, int fh, int frameCount) {
        List<BufferedImage> frames = new ArrayList<>();
        for (int i = 0; i < frameCount; i++) {
            // Walk cycle phase: 0..7 maps to leg positions
            // Leg offsets: alternate left/right stepping
            int phase = i % 8;
            int leftLegDx = walkLegOffset(phase, true);
            int rightLegDx = walkLegOffset(phase, false);
            int bobY = (phase == 2 || phase == 6) ? -1 : 0;
            frames.add(renderFrame(fw, fh, leftLegDx, rightLegDx, bobY, phase));
        }
        return frames;
    }

    private int walkLegOffset(int phase, boolean left) {
        // 8-frame walk: left forward 0-1, neutral 2-3, right forward 4-5, neutral 6-7
        int[] leftOff  = { 2, 3, 1, 0, -2, -3, -1, 0 };
        int[] rightOff = { -2, -3, -1, 0, 2, 3, 1, 0 };
        return left ? leftOff[phase] : rightOff[phase];
    }

    private BufferedImage renderFrame(int fw, int fh, int leftDx, int rightDx,
                                      int bobY, int phase) {
        PixelCanvas c = new PixelCanvas(fw, fh);
        int cx = fw / 2;
        int by = bobY;

        // Slippers
        c.fillRect(cx - 8 + leftDx, fh - 2 + by, 5, 2, SLIPPER);
        c.fillRect(cx + 3 + rightDx, fh - 2 + by, 5, 2, SLIPPER);

        // Left leg
        int legTop = fh - 15 + by;
        c.fillRect(cx - 7 + leftDx, legTop, 5, 13, PANTS);
        c.fillRect(cx - 7 + leftDx, legTop, 1, 13, PANTS_SHADE);
        // Right leg
        c.fillRect(cx + 2 + rightDx, legTop, 5, 13, PANTS);
        c.fillRect(cx + 6 + rightDx, legTop, 1, 13, PANTS_SHADE);

        // Torso (mint + cardigan)
        int torsoTop = fh - 28 + by;
        c.fillRect(cx - 5, torsoTop, 10, 13, MINT_TOP);
        c.fillRect(cx - 5, torsoTop, 1, 13, MINT_SHADE);
        c.fillRect(cx - 10, torsoTop + 1, 6, 12, CARDIGAN);
        c.fillRect(cx - 10, torsoTop + 1, 1, 12, CARDIGAN_SHADE);
        c.fillRect(cx + 4, torsoTop + 1, 6, 12, CARDIGAN);
        c.fillRect(cx + 9, torsoTop + 1, 1, 12, CARDIGAN_SHADE);
        c.fillRect(cx - 10, torsoTop, 4, 2, CARDIGAN);
        c.fillRect(cx + 6, torsoTop, 4, 2, CARDIGAN);

        // Arms swing opposite to legs
        int armSwing = -leftDx;
        int armY = torsoTop + 10;
        c.fillRect(cx - 10 + armSwing / 2, armY, 3, 4, SKIN);
        c.fillRect(cx + 7 - armSwing / 2, armY, 3, 4, SKIN);

        // Necklace
        int neckY = fh - 30 + by;
        c.setPixel(cx - 1, neckY + 1, NECKLACE_GOLD);
        c.setPixel(cx, neckY + 2, NECKLACE_GOLD);
        c.setPixel(cx + 1, neckY + 1, NECKLACE_GOLD);

        // Head
        int headTop = fh - 40 + by;
        c.fillRect(cx - 4, headTop, 8, 9, SKIN);
        c.setPixel(cx - 3, headTop + 5, new Color(0xE8, 0x98, 0x88));
        c.setPixel(cx + 3, headTop + 5, new Color(0xE8, 0x98, 0x88));
        // Eyes
        c.setPixel(cx - 2, headTop + 3, EYE_GREEN);
        c.setPixel(cx - 2, headTop + 4, EYE_GREEN);
        c.setPixel(cx - 3, headTop + 3, EYE_BLACK);
        c.setPixel(cx + 2, headTop + 3, EYE_GREEN);
        c.setPixel(cx + 2, headTop + 4, EYE_GREEN);
        c.setPixel(cx + 3, headTop + 3, EYE_BLACK);
        // Eyebrows
        c.hLine(cx - 3, headTop + 2, 2, HAIR);
        c.hLine(cx + 2, headTop + 2, 2, HAIR);
        // Mouth
        c.hLine(cx - 1, headTop + 7, 2, MOUTH);
        // Neck
        c.fillRect(cx - 2, headTop + 9, 4, 2, SKIN);

        // Hair
        c.fillRect(cx - 5, headTop - 3, 10, 4, HAIR);
        c.fillRect(cx - 2, headTop - 3, 4, 1, HAIR_HIGHLIGHT);
        c.fillRect(cx - 6, headTop - 1, 2, 10, HAIR);
        c.setPixel(cx - 7, headTop + 1, HAIR);
        c.setPixel(cx - 7, headTop + 3, HAIR_HIGHLIGHT);
        c.fillRect(cx + 4, headTop - 1, 2, 10, HAIR);
        c.setPixel(cx + 6, headTop + 1, HAIR);
        c.setPixel(cx + 6, headTop + 3, HAIR_HIGHLIGHT);

        return c.toImage();
    }
}
