package ru.lifegame.assets.infrastructure.generator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PixelCanvas — pixel-art drawing primitives")
class PixelCanvasTest {

    @Test
    @DisplayName("Newly created canvas is fully transparent")
    void newCanvasIsTransparent() {
        PixelCanvas canvas = new PixelCanvas(16, 16);
        BufferedImage img = canvas.toImage();
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int alpha = (img.getRGB(x, y) >> 24) & 0xFF;
                assertThat(alpha).isZero();
            }
        }
    }

    @Test
    @DisplayName("setPixel writes correct colour")
    void setPixelWritesCorrectColor() {
        PixelCanvas canvas = new PixelCanvas(8, 8);
        Color red = new Color(0xFF, 0x00, 0x00);
        canvas.setPixel(3, 4, red);
        BufferedImage img = canvas.toImage();
        assertThat(img.getRGB(3, 4)).isEqualTo(red.getRGB());
    }

    @Test
    @DisplayName("setPixel ignores out-of-bounds coordinates")
    void setPixelOutOfBounds() {
        PixelCanvas canvas = new PixelCanvas(4, 4);
        // Should not throw
        canvas.setPixel(-1, 0, Color.RED);
        canvas.setPixel(0, -1, Color.RED);
        canvas.setPixel(4, 0, Color.RED);
        canvas.setPixel(0, 4, Color.RED);
        canvas.toImage(); // no exception
    }

    @Test
    @DisplayName("fillRect fills correct area")
    void fillRectFillsArea() {
        PixelCanvas canvas = new PixelCanvas(10, 10);
        Color blue = new Color(0x00, 0x00, 0xFF);
        canvas.fillRect(2, 3, 4, 5, blue);
        BufferedImage img = canvas.toImage();

        // Inside
        assertThat(img.getRGB(2, 3)).isEqualTo(blue.getRGB());
        assertThat(img.getRGB(5, 7)).isEqualTo(blue.getRGB());
        // Outside
        assertThat((img.getRGB(1, 3) >> 24) & 0xFF).isZero();
        assertThat((img.getRGB(6, 3) >> 24) & 0xFF).isZero();
    }

    @Test
    @DisplayName("drawRect creates 1-pixel outline")
    void drawRectCreatesOutline() {
        PixelCanvas canvas = new PixelCanvas(10, 10);
        Color green = new Color(0x00, 0xFF, 0x00);
        canvas.drawRect(1, 1, 5, 5, green);
        BufferedImage img = canvas.toImage();

        // Top-left corner
        assertThat(img.getRGB(1, 1)).isEqualTo(green.getRGB());
        // Bottom-right corner
        assertThat(img.getRGB(5, 5)).isEqualTo(green.getRGB());
        // Centre should be transparent
        assertThat((img.getRGB(3, 3) >> 24) & 0xFF).isZero();
    }

    @Test
    @DisplayName("hLine draws horizontal line")
    void hLineDrawsLine() {
        PixelCanvas canvas = new PixelCanvas(10, 10);
        Color c = Color.WHITE;
        canvas.hLine(2, 5, 4, c);
        BufferedImage img = canvas.toImage();

        for (int x = 2; x < 6; x++) {
            assertThat(img.getRGB(x, 5)).isEqualTo(c.getRGB());
        }
        // Adjacent row should be transparent
        assertThat((img.getRGB(2, 4) >> 24) & 0xFF).isZero();
    }

    @Test
    @DisplayName("vLine draws vertical line")
    void vLineDrawsLine() {
        PixelCanvas canvas = new PixelCanvas(10, 10);
        Color c = Color.MAGENTA;
        canvas.vLine(3, 2, 5, c);
        BufferedImage img = canvas.toImage();

        for (int y = 2; y < 7; y++) {
            assertThat(img.getRGB(3, y)).isEqualTo(c.getRGB());
        }
        assertThat((img.getRGB(2, 2) >> 24) & 0xFF).isZero();
    }

    @Test
    @DisplayName("Canvas image is TYPE_INT_ARGB (32-bit)")
    void canvasIsArgb() {
        PixelCanvas canvas = new PixelCanvas(4, 4);
        BufferedImage img = canvas.toImage();
        assertThat(img.getType()).isEqualTo(BufferedImage.TYPE_INT_ARGB);
    }

    @Test
    @DisplayName("getWidth and getHeight return correct dimensions")
    void dimensionAccessors() {
        PixelCanvas canvas = new PixelCanvas(32, 48);
        assertThat(canvas.getWidth()).isEqualTo(32);
        assertThat(canvas.getHeight()).isEqualTo(48);
    }
}
