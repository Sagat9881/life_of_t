package ru.lifegame.assets.infrastructure.generator;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Low-level pixel-art drawing canvas. Sets up correct rendering hints
 * (no anti-aliasing, nearest-neighbour) and provides pixel-level primitives.
 *
 * <p>All coordinates are in pixel space. No sub-pixel operations.</p>
 */
public final class PixelCanvas {

    private final BufferedImage image;
    private final Graphics2D g;
    private final int width;
    private final int height;

    public PixelCanvas(int width, int height) {
        this.width = width;
        this.height = height;
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        this.g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        // Clear to full transparency
        g.setBackground(new Color(0, 0, 0, 0));
        g.clearRect(0, 0, width, height);
    }

    /** Sets a single pixel. No-op if out of bounds. */
    public void setPixel(int x, int y, Color c) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            image.setRGB(x, y, c.getRGB());
        }
    }

    /** Fills a rectangle with the given color. */
    public void fillRect(int x, int y, int w, int h, Color c) {
        g.setColor(c);
        g.fillRect(x, y, w, h);
    }

    /** Draws a 1-pixel outline rectangle. */
    public void drawRect(int x, int y, int w, int h, Color c) {
        // Top edge
        fillRect(x, y, w, 1, c);
        // Bottom edge
        fillRect(x, y + h - 1, w, 1, c);
        // Left edge
        fillRect(x, y, 1, h, c);
        // Right edge
        fillRect(x + w - 1, y, 1, h, c);
    }

    /** Draws a horizontal line. */
    public void hLine(int x, int y, int length, Color c) {
        fillRect(x, y, length, 1, c);
    }

    /** Draws a vertical line. */
    public void vLine(int x, int y, int length, Color c) {
        fillRect(x, y, 1, length, c);
    }

    /** Returns the completed image and disposes graphics context. */
    public BufferedImage toImage() {
        g.dispose();
        return image;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
