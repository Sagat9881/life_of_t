package ru.lifegame.assets.texture;

import java.util.Arrays;

/**
 * A 2-dimensional boolean grid where {@code true} represents a filled cell
 * and {@code false} an empty cell.
 */
public class CellGrid {

    private final boolean[][] cells;
    private final int cols;
    private final int rows;

    public CellGrid(int cols, int rows) {
        if (cols <= 0) throw new IllegalArgumentException("cols must be positive, got " + cols);
        if (rows <= 0) throw new IllegalArgumentException("rows must be positive, got " + rows);
        this.cols  = cols;
        this.rows  = rows;
        this.cells = new boolean[cols][rows];
    }

    public CellGrid(CellGrid source) {
        this.cols  = source.cols;
        this.rows  = source.rows;
        this.cells = new boolean[cols][rows];
        for (int c = 0; c < cols; c++) {
            this.cells[c] = Arrays.copyOf(source.cells[c], rows);
        }
    }

    public int cols() { return cols; }
    public int rows() { return rows; }

    public boolean get(int col, int row) {
        checkBounds(col, row);
        return cells[col][row];
    }

    public void set(int col, int row, boolean value) {
        checkBounds(col, row);
        cells[col][row] = value;
    }

    public void fill(boolean value) {
        for (int c = 0; c < cols; c++) Arrays.fill(cells[c], value);
    }

    public void applyVerticalSymmetry() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols / 2; col++) {
                int mirrorCol = cols - 1 - col;
                cells[mirrorCol][row] = cells[col][row];
            }
        }
    }

    public void applyHorizontalSymmetry() {
        for (int col = 0; col < cols; col++) {
            for (int row = 0; row < rows / 2; row++) {
                int mirrorRow = rows - 1 - row;
                cells[col][mirrorRow] = cells[col][row];
            }
        }
    }

    public void applyQuadSymmetry() {
        applyVerticalSymmetry();
        applyHorizontalSymmetry();
    }

    public boolean hasEmptyNeighbour(int col, int row) {
        return isEmptyOrOutside(col - 1, row)
            || isEmptyOrOutside(col + 1, row)
            || isEmptyOrOutside(col, row - 1)
            || isEmptyOrOutside(col, row + 1);
    }

    public boolean isEmptyOrOutside(int col, int row) {
        if (col < 0 || col >= cols || row < 0 || row >= rows) return true;
        return !cells[col][row];
    }

    private void checkBounds(int col, int row) {
        if (col < 0 || col >= cols) throw new IndexOutOfBoundsException("col " + col + " out of [0," + cols + ")");
        if (row < 0 || row >= rows) throw new IndexOutOfBoundsException("row " + row + " out of [0," + rows + ")");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                sb.append(cells[col][row] ? '#' : '.');
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
