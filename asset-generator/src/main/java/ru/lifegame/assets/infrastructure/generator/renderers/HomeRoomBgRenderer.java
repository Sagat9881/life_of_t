package ru.lifegame.assets.infrastructure.generator.renderers;

import ru.lifegame.assets.domain.service.PixelArtRenderer;
import ru.lifegame.assets.infrastructure.generator.PixelCanvas;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Renders the home room background (320×192 px, 1 frame static).
 *
 * <p>Cozy room with: walls, floor, window with curtains.
 * The character sprites (Tanya, Sam, bed) are separate overlays —
 * this only draws the empty room with a window.</p>
 */
public final class HomeRoomBgRenderer implements PixelArtRenderer {

    private static final Color WALL          = new Color(0xF5, 0xE8, 0xD0); // warm cream
    private static final Color WALL_SHADOW   = new Color(0xE0, 0xD4, 0xBC);
    private static final Color FLOOR         = new Color(0xC8, 0x9E, 0x6E); // wooden floor
    private static final Color FLOOR_LINE    = new Color(0xB0, 0x88, 0x58);
    private static final Color FLOOR_LIGHT   = new Color(0xD8, 0xB0, 0x80);
    private static final Color BASEBOARD     = new Color(0xE8, 0xE0, 0xD0);
    private static final Color WINDOW_FRAME  = new Color(0xD0, 0xD0, 0xD0);
    private static final Color WINDOW_GLASS  = new Color(0xC0, 0xE0, 0xF0); // sky blue
    private static final Color WINDOW_LIGHT  = new Color(0xD0, 0xEC, 0xF8);
    private static final Color CURTAIN       = new Color(0xE8, 0xD0, 0xE0); // light lavender
    private static final Color CURTAIN_FOLD  = new Color(0xD0, 0xB0, 0xC8);
    private static final Color SKIRTING      = new Color(0xB0, 0x90, 0x68);
    private static final Color RUG           = new Color(0xE0, 0xBB, 0xE4); // soft purple
    private static final Color RUG_PATTERN   = new Color(0xC8, 0xA0, 0xC8);
    private static final Color RUG_FRINGE    = new Color(0xD0, 0xB0, 0xD0);
    private static final Color LIGHT_PATCH   = new Color(0xFF, 0xF8, 0xE0, 0x40); // sunlight

    @Override
    public String spriteId() {
        return "home_room_bg";
    }

    @Override
    public List<BufferedImage> renderFrames(int fw, int fh, int frameCount) {
        return List.of(renderRoom(fw, fh));
    }

    private BufferedImage renderRoom(int fw, int fh) {
        PixelCanvas c = new PixelCanvas(fw, fh);

        int wallHeight = fh * 3 / 5;   // ~115 px wall
        int floorStart = wallHeight;

        // --- Wall ---
        c.fillRect(0, 0, fw, wallHeight, WALL);
        // Wall shadow gradient at bottom
        c.fillRect(0, wallHeight - 4, fw, 4, WALL_SHADOW);

        // --- Floor ---
        drawFloor(c, fw, fh, floorStart);

        // --- Baseboard ---
        c.fillRect(0, floorStart, fw, 3, BASEBOARD);
        c.fillRect(0, floorStart + 3, fw, 1, SKIRTING);

        // --- Window (centred on wall) ---
        drawWindow(c, fw, wallHeight);

        // --- Rug on the floor ---
        drawRug(c, fw, fh, floorStart);

        // --- Sunlight patch from window ---
        drawSunlightPatch(c, fw, fh, floorStart);

        return c.toImage();
    }

    private void drawFloor(PixelCanvas c, int fw, int fh, int floorStart) {
        c.fillRect(0, floorStart, fw, fh - floorStart, FLOOR);

        // Floorboard lines (every 12 px)
        for (int y = floorStart + 4; y < fh; y += 12) {
            c.hLine(0, y, fw, FLOOR_LINE);
        }
        // Vertical plank seams
        for (int x = 20; x < fw; x += 40) {
            for (int y = floorStart + 4; y < fh; y++) {
                if ((y - floorStart) % 12 < 6) {
                    c.setPixel(x, y, FLOOR_LINE);
                }
            }
        }
        for (int x = 40; x < fw; x += 40) {
            for (int y = floorStart + 4; y < fh; y++) {
                if ((y - floorStart) % 12 >= 6) {
                    c.setPixel(x, y, FLOOR_LINE);
                }
            }
        }
        // Light highlight on some boards
        c.fillRect(80, floorStart + 6, 30, 1, FLOOR_LIGHT);
        c.fillRect(200, floorStart + 18, 30, 1, FLOOR_LIGHT);
    }

    private void drawWindow(PixelCanvas c, int fw, int wallHeight) {
        int winW = 64;
        int winH = 50;
        int winX = (fw - winW) / 2;
        int winY = 15;

        // Window frame
        c.fillRect(winX - 2, winY - 2, winW + 4, winH + 4, WINDOW_FRAME);
        // Glass
        c.fillRect(winX, winY, winW, winH, WINDOW_GLASS);
        // Light reflection
        c.fillRect(winX + 4, winY + 4, 20, 8, WINDOW_LIGHT);
        // Cross bar (vertical)
        c.fillRect(winX + winW / 2 - 1, winY, 2, winH, WINDOW_FRAME);
        // Cross bar (horizontal)
        c.fillRect(winX, winY + winH / 2 - 1, winW, 2, WINDOW_FRAME);

        // Window sill
        c.fillRect(winX - 4, winY + winH + 2, winW + 8, 4, WINDOW_FRAME);

        // Curtains (left)
        int curtainW = 14;
        for (int y = winY - 2; y < winY + winH + 2; y++) {
            int fold = (y % 8 < 4) ? 0 : 2;
            c.fillRect(winX - 2 - curtainW + fold, y, curtainW, 1, CURTAIN);
            if (y % 8 == 0) {
                c.hLine(winX - 2 - curtainW + fold, y, curtainW, CURTAIN_FOLD);
            }
        }
        // Curtains (right)
        for (int y = winY - 2; y < winY + winH + 2; y++) {
            int fold = (y % 8 < 4) ? 0 : -2;
            c.fillRect(winX + winW + 2 + fold, y, curtainW, 1, CURTAIN);
            if (y % 8 == 0) {
                c.hLine(winX + winW + 2 + fold, y, curtainW, CURTAIN_FOLD);
            }
        }
        // Curtain rod
        c.fillRect(winX - curtainW - 4, winY - 4, winW + curtainW * 2 + 8, 2, WINDOW_FRAME);
    }

    private void drawRug(PixelCanvas c, int fw, int fh, int floorStart) {
        int rugW = 100;
        int rugH = 40;
        int rugX = fw / 2 - 80;
        int rugY = floorStart + 20;

        // Rug body
        c.fillRect(rugX, rugY, rugW, rugH, RUG);
        // Border pattern
        c.drawRect(rugX + 2, rugY + 2, rugW - 4, rugH - 4, RUG_PATTERN);
        c.drawRect(rugX + 4, rugY + 4, rugW - 8, rugH - 8, RUG_PATTERN);
        // Fringes at short ends
        for (int x = rugX; x < rugX + rugW; x += 3) {
            c.vLine(x, rugY - 2, 2, RUG_FRINGE);
            c.vLine(x, rugY + rugH, 2, RUG_FRINGE);
        }
    }

    private void drawSunlightPatch(PixelCanvas c, int fw, int fh, int floorStart) {
        // Soft sunlight falling from window onto floor
        int patchX = fw / 2 - 20;
        int patchY = floorStart + 8;
        for (int dy = 0; dy < 30; dy++) {
            int patchW = 40 + dy;
            c.fillRect(patchX - dy / 2, patchY + dy, patchW, 1, LIGHT_PATCH);
        }
    }
}
