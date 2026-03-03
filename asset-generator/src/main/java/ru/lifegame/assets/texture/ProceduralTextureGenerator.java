package ru.lifegame.assets.texture;

import ru.lifegame.assets.AssetGenerator;
import ru.lifegame.assets.AssetRequest;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Generates pixel-art textures procedurally using a cell grid with
 * configurable symmetry.
 */
public class ProceduralTextureGenerator implements AssetGenerator {

    public static final int CELL_SIZE = 2;

    @Override
    public String name() {
        return "ProceduralTextureGenerator";
    }

    @Override
    public BufferedImage generate(AssetRequest request) {
        int width  = request.width();
        int height = request.height();

        SymmetryMode symmetry = parseSymmetry(request.param("symmetry", "quad"));
        TextureColorPalette palette = resolvePalette(request);
        double fillProbability = parseFillProbability(request.param("fill_probability", "0.4"));
        long seed = parseSeed(request.param("seed", null));

        int cellCols = (width  + CELL_SIZE - 1) / CELL_SIZE;
        int cellRows = (height + CELL_SIZE - 1) / CELL_SIZE;

        CellGrid grid = new CellGrid(cellCols, cellRows);

        Random rng = new Random(seed);
        fillWorkingRegion(grid, symmetry, fillProbability, rng);

        switch (symmetry) {
            case VERTICAL    -> grid.applyVerticalSymmetry();
            case HORIZONTAL  -> grid.applyHorizontalSymmetry();
            case QUAD        -> grid.applyQuadSymmetry();
            case NONE        -> {}
        }

        return renderGrid(grid, palette, width, height);
    }

    private void fillWorkingRegion(CellGrid grid, SymmetryMode symmetry,
                                   double fillProbability, Random rng) {
        int maxCol = grid.cols();
        int maxRow = grid.rows();

        if (symmetry == SymmetryMode.VERTICAL || symmetry == SymmetryMode.QUAD) {
            maxCol = (grid.cols() + 1) / 2;
        }
        if (symmetry == SymmetryMode.HORIZONTAL || symmetry == SymmetryMode.QUAD) {
            maxRow = (grid.rows() + 1) / 2;
        }

        for (int col = 0; col < maxCol; col++) {
            for (int row = 0; row < maxRow; row++) {
                grid.set(col, row, rng.nextDouble() < fillProbability);
            }
        }
    }

    private BufferedImage renderGrid(CellGrid grid, TextureColorPalette palette,
                                     int targetWidth, int targetHeight) {
        int imgWidth  = grid.cols() * CELL_SIZE;
        int imgHeight = grid.rows() * CELL_SIZE;

        BufferedImage image = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        for (int col = 0; col < grid.cols(); col++) {
            for (int row = 0; row < grid.rows(); row++) {
                Color color;
                if (grid.get(col, row)) {
                    color = grid.hasEmptyNeighbour(col, row) ? palette.outline() : palette.fg();
                } else {
                    color = palette.bg();
                }
                g.setColor(color);
                g.fillRect(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
        g.dispose();

        if (imgWidth == targetWidth && imgHeight == targetHeight) {
            return image;
        }
        return image.getSubimage(0, 0,
                Math.min(targetWidth,  imgWidth),
                Math.min(targetHeight, imgHeight));
    }

    private SymmetryMode parseSymmetry(String value) {
        if (value == null) return SymmetryMode.QUAD;
        return switch (value.toLowerCase()) {
            case "vertical"   -> SymmetryMode.VERTICAL;
            case "horizontal" -> SymmetryMode.HORIZONTAL;
            case "none"       -> SymmetryMode.NONE;
            default           -> SymmetryMode.QUAD;
        };
    }

    private TextureColorPalette resolvePalette(AssetRequest request) {
        String paletteName = request.param("palette", "wood");
        TextureColorPalette base = switch (paletteName.toLowerCase()) {
            case "stone"     -> TextureColorPalette.stone();
            case "fabric"    -> TextureColorPalette.fabric();
            case "metal"     -> TextureColorPalette.metal();
            case "wallpaper" -> TextureColorPalette.wallpaper();
            default          -> TextureColorPalette.wood();
        };

        String fgHex      = request.param("fg_color",      null);
        String bgHex      = request.param("bg_color",      null);
        String outlineHex = request.param("outline_color", null);

        Color fg      = (fgHex      != null) ? TextureColorPalette.hex(fgHex)      : base.fg();
        Color bg      = (bgHex      != null) ? TextureColorPalette.hex(bgHex)      : base.bg();
        Color outline = (outlineHex != null) ? TextureColorPalette.hex(outlineHex) : base.outline();

        return new TextureColorPalette(fg, bg, outline);
    }

    private double parseFillProbability(String value) {
        try {
            double v = Double.parseDouble(value);
            return Math.max(0.0, Math.min(1.0, v));
        } catch (NumberFormatException e) {
            return 0.4;
        }
    }

    private long parseSeed(String value) {
        if (value == null || value.isBlank()) return System.nanoTime();
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return value.hashCode();
        }
    }
}
