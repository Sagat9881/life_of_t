package ru.lifegame.assets.infrastructure.generator.renderers;

import ru.lifegame.assets.domain.service.PixelArtRenderer;
import ru.lifegame.assets.infrastructure.generator.PixelCanvas;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Renders a bed sprite (48×32 px, 1 frame static).
 * Simple pixel-art bed with headboard, mattress, pillow, and blanket.
 */
public final class BedStaticRenderer implements PixelArtRenderer {

    private static final Color FRAME_WOOD   = new Color(0x8B, 0x5E, 0x3C);
    private static final Color FRAME_DARK   = new Color(0x6B, 0x44, 0x23);
    private static final Color MATTRESS     = new Color(0xF8, 0xF0, 0xE8);
    private static final Color SHEET        = new Color(0xE8, 0xE0, 0xD0);
    private static final Color PILLOW       = new Color(0xF0, 0xF0, 0xF0);
    private static final Color PILLOW_SHADE = new Color(0xD8, 0xD8, 0xE0);
    private static final Color BLANKET      = new Color(0xB5, 0xEA, 0xD7); // mint green
    private static final Color BLANKET_DARK = new Color(0x90, 0xC8, 0xB0);
    private static final Color BLANKET_FOLD = new Color(0xA0, 0xD8, 0xC0);

    @Override
    public String spriteId() {
        return "bed_static";
    }

    @Override
    public List<BufferedImage> renderFrames(int fw, int fh, int frameCount) {
        return List.of(renderBed(fw, fh));
    }

    private BufferedImage renderBed(int fw, int fh) {
        PixelCanvas c = new PixelCanvas(fw, fh);

        // Bed frame (isometric-ish side view)
        // Legs
        c.fillRect(2, fh - 4, 3, 4, FRAME_DARK);
        c.fillRect(fw - 5, fh - 4, 3, 4, FRAME_DARK);
        c.fillRect(2, fh - 6, 3, 2, FRAME_DARK);
        c.fillRect(fw - 5, fh - 6, 3, 2, FRAME_DARK);

        // Bed frame bottom rail
        c.fillRect(2, fh - 8, fw - 4, 3, FRAME_WOOD);
        c.fillRect(2, fh - 8, fw - 4, 1, FRAME_DARK);

        // Headboard (left side, taller)
        c.fillRect(0, fh - 22, 5, 18, FRAME_WOOD);
        c.fillRect(0, fh - 22, 5, 1, FRAME_DARK);
        c.fillRect(0, fh - 22, 1, 18, FRAME_DARK);
        c.fillRect(4, fh - 22, 1, 18, FRAME_DARK);
        // Headboard top decoration
        c.fillRect(1, fh - 24, 3, 3, FRAME_WOOD);
        c.fillRect(1, fh - 24, 3, 1, FRAME_DARK);

        // Mattress
        c.fillRect(5, fh - 14, fw - 9, 6, MATTRESS);
        c.fillRect(5, fh - 14, fw - 9, 1, SHEET);

        // Pillow (near headboard)
        c.fillRect(6, fh - 18, 10, 5, PILLOW);
        c.fillRect(6, fh - 18, 10, 1, PILLOW_SHADE);
        c.fillRect(6, fh - 18, 1, 5, PILLOW_SHADE);
        // Pillow indent
        c.fillRect(8, fh - 16, 6, 2, PILLOW_SHADE);

        // Blanket (covering most of the bed)
        c.fillRect(16, fh - 16, fw - 20, 8, BLANKET);
        c.fillRect(16, fh - 16, fw - 20, 1, BLANKET_DARK);
        // Blanket fold line
        c.fillRect(16, fh - 12, fw - 20, 1, BLANKET_FOLD);
        // Blanket drape at foot
        c.fillRect(fw - 8, fh - 14, 4, 6, BLANKET_DARK);

        // Footboard (right side, shorter)
        c.fillRect(fw - 5, fh - 16, 4, 12, FRAME_WOOD);
        c.fillRect(fw - 5, fh - 16, 4, 1, FRAME_DARK);
        c.fillRect(fw - 5, fh - 16, 1, 12, FRAME_DARK);

        return c.toImage();
    }
}
