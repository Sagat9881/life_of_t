package ru.lifegame.assets.texture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Тесты CellGrid")
class CellGridTest {

    @Test
    void testCreation_correctDimensions() {
        CellGrid grid = new CellGrid(8, 6);
        assertThat(grid.cols()).isEqualTo(8);
        assertThat(grid.rows()).isEqualTo(6);
    }

    @Test
    void testCreation_allCellsInitiallyEmpty() {
        CellGrid grid = new CellGrid(4, 4);
        for (int col = 0; col < 4; col++) {
            for (int row = 0; row < 4; row++) {
                assertThat(grid.get(col, row)).isFalse();
            }
        }
    }

    @Test
    void testSetAndGet() {
        CellGrid grid = new CellGrid(5, 5);
        grid.set(2, 3, true);
        assertThat(grid.get(2, 3)).isTrue();
        assertThat(grid.get(2, 2)).isFalse();
    }

    @Test
    void testFillTrue() {
        CellGrid grid = new CellGrid(3, 3);
        grid.fill(true);
        for (int col = 0; col < 3; col++)
            for (int row = 0; row < 3; row++)
                assertThat(grid.get(col, row)).isTrue();
    }

    @Test
    void testFillFalse() {
        CellGrid grid = new CellGrid(3, 3);
        grid.fill(true);
        grid.fill(false);
        for (int col = 0; col < 3; col++)
            for (int row = 0; row < 3; row++)
                assertThat(grid.get(col, row)).isFalse();
    }

    @Test
    void testBoundsCheck() {
        CellGrid grid = new CellGrid(4, 4);
        assertThatThrownBy(() -> grid.get(-1, 0)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> grid.get(4, 0)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> grid.get(0, -1)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> grid.get(0, 4)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void testVerticalSymmetry_evenWidth() {
        CellGrid grid = new CellGrid(8, 4);
        grid.set(0, 0, true);
        grid.set(1, 1, true);
        grid.set(3, 2, true);
        grid.applyVerticalSymmetry();
        assertThat(grid.get(7, 0)).isTrue();
        assertThat(grid.get(6, 1)).isTrue();
        assertThat(grid.get(4, 2)).isTrue();
        assertThat(grid.get(0, 0)).isTrue();
        assertThat(grid.get(1, 1)).isTrue();
        assertThat(grid.get(3, 2)).isTrue();
    }

    @Test
    void testVerticalSymmetry_emptySourceStaysEmpty() {
        CellGrid grid = new CellGrid(6, 3);
        grid.set(2, 1, true);
        grid.applyVerticalSymmetry();
        assertThat(grid.get(3, 1)).isTrue();
        assertThat(grid.get(4, 1)).isFalse();
        assertThat(grid.get(5, 1)).isFalse();
    }

    @Test
    void testHorizontalSymmetry() {
        CellGrid grid = new CellGrid(4, 8);
        grid.set(1, 0, true);
        grid.set(2, 3, true);
        grid.applyHorizontalSymmetry();
        assertThat(grid.get(1, 7)).isTrue();
        assertThat(grid.get(2, 4)).isTrue();
    }

    @Test
    void testQuadSymmetry() {
        CellGrid grid = new CellGrid(8, 8);
        grid.set(1, 1, true);
        grid.set(2, 0, true);
        grid.applyQuadSymmetry();
        assertThat(grid.get(1, 1)).isTrue();
        assertThat(grid.get(2, 0)).isTrue();
        assertThat(grid.get(1, 6)).isTrue();
        assertThat(grid.get(2, 7)).isTrue();
        assertThat(grid.get(6, 1)).isTrue();
        assertThat(grid.get(5, 0)).isTrue();
        assertThat(grid.get(6, 6)).isTrue();
        assertThat(grid.get(5, 7)).isTrue();
    }

    @Test
    void testHasEmptyNeighbour_edgeCell() {
        CellGrid grid = new CellGrid(4, 4);
        grid.fill(true);
        assertThat(grid.hasEmptyNeighbour(0, 0)).isTrue();
        assertThat(grid.hasEmptyNeighbour(3, 3)).isTrue();
    }

    @Test
    void testHasEmptyNeighbour_centerFullGrid() {
        CellGrid grid = new CellGrid(5, 5);
        grid.fill(true);
        assertThat(grid.hasEmptyNeighbour(2, 2)).isFalse();
    }

    @Test
    void testCopyConstructor_independence() {
        CellGrid original = new CellGrid(4, 4);
        original.set(1, 1, true);
        CellGrid copy = new CellGrid(original);
        copy.set(2, 2, true);
        assertThat(original.get(2, 2)).isFalse();
        assertThat(copy.get(1, 1)).isTrue();
        assertThat(copy.get(2, 2)).isTrue();
    }
}
