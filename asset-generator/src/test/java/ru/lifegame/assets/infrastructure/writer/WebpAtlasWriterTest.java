package ru.lifegame.assets.infrastructure.writer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.lifegame.assets.domain.model.asset.AnimationSpec;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("WebpAtlasWriter — генерация horizontal-strip атласов")
class WebpAtlasWriterTest {

    private WebpAtlasWriter writer;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        writer = new WebpAtlasWriter();
    }

    @Test
    @DisplayName("Атлас из 24 кадров 64x64 → размер 1536x64")
    void atlasCorrectDimensions() {
        List<BufferedImage> frames = createFrames(24, 64, 64);
        BufferedImage atlas = writer.createAtlasImage(frames);

        assertThat(atlas.getWidth()).isEqualTo(24 * 64);
        assertThat(atlas.getHeight()).isEqualTo(64);
    }

    @Test
    @DisplayName("Атлас формата ARGB")
    void atlasIsArgb() {
        List<BufferedImage> frames = createFrames(10, 32, 32);
        BufferedImage atlas = writer.createAtlasImage(frames);

        assertThat(atlas.getType()).isEqualTo(BufferedImage.TYPE_INT_ARGB);
    }

    @Test
    @DisplayName("Кадры корректно размещены горизонтально")
    void framesPlacedHorizontally() {
        // Create frames with unique colors
        List<BufferedImage> frames = new ArrayList<>();
        int[] colors = {0xFFFF0000, 0xFF00FF00, 0xFF0000FF};
        for (int c : colors) {
            BufferedImage frame = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frame.createGraphics();
            g.setColor(new Color(c, true));
            g.fillRect(0, 0, 16, 16);
            g.dispose();
            frames.add(frame);
        }

        BufferedImage atlas = writer.createAtlasImage(frames);
        assertThat(atlas.getRGB(0, 0)).isEqualTo(0xFFFF0000);
        assertThat(atlas.getRGB(16, 0)).isEqualTo(0xFF00FF00);
        assertThat(atlas.getRGB(32, 0)).isEqualTo(0xFF0000FF);
    }

    @Test
    @DisplayName("writeAtlas создаёт файл на диске")
    void writeAtlasCreatesFile() throws Exception {
        List<BufferedImage> frames = createFrames(5, 32, 32);
        AnimationSpec spec = new AnimationSpec("test_anim", 5, 12, true, 32, 32);

        Path result = writer.writeAtlas(frames, spec, tempDir);
        assertThat(result.toFile()).exists();
        assertThat(result.getFileName().toString()).isEqualTo("test_anim_atlas.png");
    }

    @Test
    @DisplayName("Пустой список кадров выбрасывает исключение")
    void emptyFramesThrows() {
        assertThatThrownBy(() -> writer.createAtlasImage(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Кадры разного размера выбрасывают исключение")
    void inconsistentFrameSizeThrows() {
        List<BufferedImage> frames = List.of(
                new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB),
                new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB)
        );
        AnimationSpec spec = new AnimationSpec("bad", 2, 12, true, 32, 32);

        assertThatThrownBy(() -> writer.writeAtlas(frames, spec, tempDir))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private List<BufferedImage> createFrames(int count, int width, int height) {
        List<BufferedImage> frames = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            frames.add(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
        }
        return frames;
    }
}
