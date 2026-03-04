package ru.lifegame.assets.infrastructure.generator.renderers;

import ru.lifegame.assets.domain.service.PixelArtRenderer;
import ru.lifegame.assets.infrastructure.generator.PixelCanvas;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders Tanya idle animation (32×48 px, 6 frames).
 *
 * <p>Visual spec (from reference image-2.jpg):
 * dark-red/burgundy curly hair, green eyes, beige oversized cardigan,
 * mint/teal tank top, white pants, gold heart necklace, gray slippers.
 * Pixel-art style, simplified but recognisable as a young woman.</p>
 */
public final class TanyaIdleRenderer implements PixelArtRenderer {

    // Tanya color palette (matching reference image)
    private static final Color SKIN           = new Color(0xF0, 0xC0, 0x98);
    private static final Color SKIN_SHADOW    = new Color(0xD8, 0xA0, 0x78);
    private static final Color HAIR           = new Color(0x80, 0x18, 0x28); // dark red/burgundy
    private static final Color HAIR_HIGHLIGHT = new Color(0xA0, 0x28, 0x38);
    private static final Color EYE_GREEN      = new Color(0x30, 0xA0, 0x40);
    private static final Color EYE_BLACK      = new Color(0x20, 0x20, 0x20);
    private static final Color MOUTH          = new Color(0xD0, 0x70, 0x70);
    private static final Color CARDIGAN       = new Color(0xD8, 0xC8, 0xA0); // beige
    private static final Color CARDIGAN_SHADE = new Color(0xC0, 0xB0, 0x88);
    private static final Color MINT_TOP       = new Color(0x90, 0xD8, 0xC0); // mint/teal
    private static final Color MINT_SHADE     = new Color(0x70, 0xC0, 0xA8);
    private static final Color PANTS          = new Color(0xF0, 0xF0, 0xE8); // white pants
    private static final Color PANTS_SHADE    = new Color(0xD0, 0xD0, 0xC8);
    private static final Color NECKLACE_GOLD  = new Color(0xE0, 0xC0, 0x40);
    private static final Color SLIPPER        = new Color(0x80, 0x80, 0x80); // gray
    private static final Color SLIPPER_DARK   = new Color(0x60, 0x60, 0x60);
    private static final Color OUTLINE        = new Color(0x40, 0x20, 0x20);

    @Override
    public String spriteId() {
        return "tanya_idle";
    }

    @Override
    public List<BufferedImage> renderFrames(int fw, int fh, int frameCount) {
        List<BufferedImage> frames = new ArrayList<>();
        for (int i = 0; i < frameCount; i++) {
            // Breathing: frames 0-2 shift up 1px, frames 3-5 back down
            int breathOffset = (i < frameCount / 2) ? (i < 2 ? 0 : -1) : (i > 4 ? 0 : -1);
            frames.add(renderTanyaFrame(fw, fh, breathOffset, i));
        }
        return frames;
    }

    private BufferedImage renderTanyaFrame(int fw, int fh, int breathY, int frameIdx) {
        PixelCanvas c = new PixelCanvas(fw, fh);
        int cx = fw / 2; // centre x = 16

        // --- Slippers (bottom) ---
        drawSlippers(c, cx, fh, breathY);

        // --- Legs / white pants ---
        drawLegs(c, cx, fh, breathY);

        // --- Torso: mint top + beige cardigan ---
        drawTorso(c, cx, fh, breathY, frameIdx);

        // --- Necklace ---
        drawNecklace(c, cx, fh, breathY);

        // --- Head ---
        drawHead(c, cx, fh, breathY, frameIdx);

        // --- Hair (curly dark red, on top + sides) ---
        drawHair(c, cx, fh, breathY, frameIdx);

        return c.toImage();
    }

    private void drawSlippers(PixelCanvas c, int cx, int fh, int by) {
        int sy = fh - 2 + by; // slipper y
        // Left slipper
        c.fillRect(cx - 8, sy, 5, 2, SLIPPER);
        c.setPixel(cx - 8, sy + 1, SLIPPER_DARK);
        // Right slipper
        c.fillRect(cx + 3, sy, 5, 2, SLIPPER);
        c.setPixel(cx + 7, sy + 1, SLIPPER_DARK);
    }

    private void drawLegs(PixelCanvas c, int cx, int fh, int by) {
        int legTop = fh - 15 + by;
        // Left leg
        c.fillRect(cx - 7, legTop, 5, 13, PANTS);
        c.fillRect(cx - 7, legTop, 1, 13, PANTS_SHADE); // shadow
        // Right leg
        c.fillRect(cx + 2, legTop, 5, 13, PANTS);
        c.fillRect(cx + 6, legTop, 1, 13, PANTS_SHADE);
        // Waistband
        c.fillRect(cx - 7, legTop, 14, 1, PANTS_SHADE);
    }

    private void drawTorso(PixelCanvas c, int cx, int fh, int by, int frame) {
        int torsoTop = fh - 28 + by;
        // Mint tank top (center)
        c.fillRect(cx - 5, torsoTop, 10, 13, MINT_TOP);
        c.fillRect(cx - 5, torsoTop, 1, 13, MINT_SHADE);
        c.fillRect(cx + 4, torsoTop, 1, 13, MINT_SHADE);

        // Beige cardigan (open, sides and shoulder)
        // Left cardigan side
        c.fillRect(cx - 10, torsoTop + 1, 6, 12, CARDIGAN);
        c.fillRect(cx - 10, torsoTop + 1, 1, 12, CARDIGAN_SHADE);
        // Right cardigan side
        c.fillRect(cx + 4, torsoTop + 1, 6, 12, CARDIGAN);
        c.fillRect(cx + 9, torsoTop + 1, 1, 12, CARDIGAN_SHADE);
        // Cardigan shoulders
        c.fillRect(cx - 10, torsoTop, 4, 2, CARDIGAN);
        c.fillRect(cx + 6, torsoTop, 4, 2, CARDIGAN);
        // Cardigan collar hints
        c.fillRect(cx - 5, torsoTop, 1, 3, CARDIGAN);
        c.fillRect(cx + 4, torsoTop, 1, 3, CARDIGAN);

        // Arms (skin visible at wrists, below cardigan)
        int armY = torsoTop + 10;
        c.fillRect(cx - 10, armY, 3, 4, SKIN);
        c.fillRect(cx - 10, armY + 3, 3, 1, SKIN_SHADOW);
        c.fillRect(cx + 7, armY, 3, 4, SKIN);
        c.fillRect(cx + 7, armY + 3, 3, 1, SKIN_SHADOW);
    }

    private void drawNecklace(PixelCanvas c, int cx, int fh, int by) {
        int neckY = fh - 30 + by;
        // Chain
        c.setPixel(cx - 2, neckY, NECKLACE_GOLD);
        c.setPixel(cx - 1, neckY + 1, NECKLACE_GOLD);
        c.setPixel(cx, neckY + 2, NECKLACE_GOLD);
        c.setPixel(cx + 1, neckY + 1, NECKLACE_GOLD);
        c.setPixel(cx + 2, neckY, NECKLACE_GOLD);
        // Heart pendant
        c.setPixel(cx, neckY + 3, NECKLACE_GOLD);
    }

    private void drawHead(PixelCanvas c, int cx, int fh, int by, int frame) {
        int headTop = fh - 40 + by;
        // Head oval (8×9 px, skin color)
        c.fillRect(cx - 4, headTop, 8, 9, SKIN);
        // Shadow on left cheek
        c.fillRect(cx - 4, headTop + 4, 1, 3, SKIN_SHADOW);
        // Rosy cheeks
        c.setPixel(cx - 3, headTop + 5, new Color(0xE8, 0x98, 0x88));
        c.setPixel(cx + 3, headTop + 5, new Color(0xE8, 0x98, 0x88));

        // Eyes (green with black pupil)
        // Left eye
        c.setPixel(cx - 2, headTop + 3, EYE_GREEN);
        c.setPixel(cx - 2, headTop + 4, EYE_GREEN);
        c.setPixel(cx - 3, headTop + 3, EYE_BLACK); // pupil
        // Right eye
        c.setPixel(cx + 2, headTop + 3, EYE_GREEN);
        c.setPixel(cx + 2, headTop + 4, EYE_GREEN);
        c.setPixel(cx + 3, headTop + 3, EYE_BLACK);

        // Eyebrows
        c.hLine(cx - 3, headTop + 2, 2, HAIR);
        c.hLine(cx + 2, headTop + 2, 2, HAIR);

        // Mouth (small smile)
        c.hLine(cx - 1, headTop + 7, 2, MOUTH);

        // Neck
        c.fillRect(cx - 2, headTop + 9, 4, 2, SKIN);
    }

    private void drawHair(PixelCanvas c, int cx, int fh, int by, int frame) {
        int headTop = fh - 40 + by;

        // Top hair (curly volume)
        c.fillRect(cx - 5, headTop - 3, 10, 4, HAIR);
        // Highlight on top
        c.fillRect(cx - 2, headTop - 3, 4, 1, HAIR_HIGHLIGHT);
        c.fillRect(cx - 3, headTop - 2, 6, 1, HAIR_HIGHLIGHT);

        // Side hair (left - falls below head)
        c.fillRect(cx - 6, headTop - 1, 2, 10, HAIR);
        c.setPixel(cx - 7, headTop + 1, HAIR); // curl
        c.setPixel(cx - 7, headTop + 3, HAIR_HIGHLIGHT);
        c.setPixel(cx - 6, headTop + 8, HAIR_HIGHLIGHT); // curl tip
        c.setPixel(cx - 7, headTop + 6, HAIR);

        // Side hair (right)
        c.fillRect(cx + 4, headTop - 1, 2, 10, HAIR);
        c.setPixel(cx + 6, headTop + 1, HAIR);
        c.setPixel(cx + 6, headTop + 3, HAIR_HIGHLIGHT);
        c.setPixel(cx + 5, headTop + 8, HAIR_HIGHLIGHT);
        c.setPixel(cx + 6, headTop + 6, HAIR);

        // Fringe curls over forehead
        c.setPixel(cx - 4, headTop, HAIR);
        c.setPixel(cx - 3, headTop - 1, HAIR);
        c.setPixel(cx + 3, headTop, HAIR);
        c.setPixel(cx + 4, headTop - 1, HAIR);
    }
}
