package ru.lifegame.assets.infrastructure.generator;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Simple procedural texture generator using cell grids with configurable symmetry.
 * <p>
 * This is the original ProceduralTextureGenerator v1.
 *
 * @deprecated Use {@link LayeredAssetGenerator} instead. This class will be removed
 * in a future release. The new generator supports XML-driven layered asset generation
 * with animation atlas support.
 */
@Deprecated(since = "2.0", forRemoval = true)
public class SimpleProceduralTextureGenerator {

    public static final int CELL_SIZE = 2;

    /**
     * Generates a procedural texture image.
     *
     * @param width           output image width
     * @param height          output image height
     * @param fillProbability probability of filling a cell (0.0 to 1.0)
     * @param seed            random seed for deterministic output
     * @return generated BufferedImage in ARGB format
     */
    public BufferedImage generate(int width, int height, double fillProbability, long seed) {
        int cellCols = (width + CELL_SIZE - 1) / CELL_SIZE;
        int cellRows = (height + CELL_SIZE - 1) / CELL_SIZE;

        boolean[][] cells = new boolean[cellCols][cellRows];
        Random rng = new Random(seed);

        int halfCols = (cellCols + 1) / 2;
        int halfRows = (cellRows + 1) / 2;
        for (int col = 0; col < halfCols; col++) {
            for (int row = 0; row < halfRows; row++) {
                cells[col][row] = rng.nextDouble() < fillProbability;
            }
        }

        // Apply quad symmetry
        for (int row = 0; row < cellRows; row++) {
            for (int col = 0; col < cellCols / 2; col++) {
                cells[cellCols - 1 - col][row] = cells[col][row];
            }
        }
        for (int col = 0; col < cellCols; col++) {
            for (int row = 0; row < cellRows / 2; row++) {
                cells[col][cellRows - 1 - row] = cells[col][row];
            }
        }

        int imgWidth = cellCols * CELL_SIZE;
        int imgHeight = cellRows * CELL_SIZE;
        BufferedImage image = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        Color fg = new Color(0x8B5E3C);
        Color bg = new Color(0x5C3A1E);

        for (int col = 0; col < cellCols; col++) {
            for (int row = 0; row < cellRows; row++) {
                g.setColor(cells[col][row] ? fg : bg);
                g.fillRect(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
        g.dispose();

        return image.getSubimage(0, 0,
                Math.min(width, imgWidth),
                Math.min(height, imgHeight));
    }
}
