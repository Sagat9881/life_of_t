package ru.lifegame.assets.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PixelArtRendererRegistry — lookup and listing")
class PixelArtRendererRegistryTest {

    private final PixelArtRenderer testRenderer = new PixelArtRenderer() {
        @Override
        public String spriteId() {
            return "test_sprite";
        }

        @Override
        public List<BufferedImage> renderFrames(int fw, int fh, int fc) {
            return List.of(new BufferedImage(fw, fh, BufferedImage.TYPE_INT_ARGB));
        }
    };

    @Test
    @DisplayName("find returns renderer for registered id")
    void findReturnsRegisteredRenderer() {
        PixelArtRendererRegistry registry = new PixelArtRendererRegistry(List.of(testRenderer));
        assertThat(registry.find("test_sprite")).isPresent();
        assertThat(registry.find("test_sprite").orElseThrow().spriteId()).isEqualTo("test_sprite");
    }

    @Test
    @DisplayName("find returns empty for unknown id")
    void findReturnsEmptyForUnknown() {
        PixelArtRendererRegistry registry = new PixelArtRendererRegistry(List.of(testRenderer));
        assertThat(registry.find("nonexistent")).isEmpty();
    }

    @Test
    @DisplayName("allIds returns all registered sprite ids")
    void allIdsReturnsAll() {
        PixelArtRendererRegistry registry = new PixelArtRendererRegistry(List.of(testRenderer));
        assertThat(registry.allIds()).containsExactly("test_sprite");
    }

    @Test
    @DisplayName("empty registry returns empty list and empty optionals")
    void emptyRegistryWorks() {
        PixelArtRendererRegistry registry = new PixelArtRendererRegistry(List.of());
        assertThat(registry.allIds()).isEmpty();
        assertThat(registry.find("anything")).isEmpty();
    }
}
